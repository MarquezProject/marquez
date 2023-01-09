/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.newDatasetFacet;
import static marquez.db.LineageTestUtils.writeDownstreamLineage;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import marquez.api.JdbiUtils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.JobDao;
import marquez.db.LineageDao;
import marquez.db.LineageTestUtils;
import marquez.db.LineageTestUtils.DatasetConsumerJob;
import marquez.db.LineageTestUtils.JobLineage;
import marquez.db.OpenLineageDao;
import marquez.db.models.JobData;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Edge;
import marquez.service.models.Lineage;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.SchemaField;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import marquez.service.models.NodeType;
import marquez.service.models.Run;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ObjectAssert;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class LineageServiceTest {

  private static LineageDao lineageDao;
  private static LineageService lineageService;
  private static OpenLineageDao openLineageDao;

  private static DatasetDao datasetDao;
  private static JobDao jobDao;

  private final Dataset dataset =
      new Dataset(
          NAMESPACE,
          "commonDataset",
          newDatasetFacet(
              new SchemaField("firstname", "string", "the first name"),
              new SchemaField("lastname", "string", "the last name"),
              new SchemaField("birthdate", "date", "the date of birth")));
  private final JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

  static Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    LineageServiceTest.jdbi = jdbi;
    lineageDao = jdbi.onDemand(LineageDao.class);
    lineageService = new LineageService(lineageDao, jdbi.onDemand(JobDao.class));
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    jobDao = jdbi.onDemand(JobDao.class);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testLineage() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("outputData")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.of("outputData2")),
                    new DatasetConsumerJob("finalConsumer", 1, Optional.empty()))),
            jobFacet,
            dataset);

    UpdateLineageRow secondRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    writeDownstreamLineage(
        openLineageDao,
        new LinkedList<>(
            Arrays.asList(
                new DatasetConsumerJob("newReadJob", 5, Optional.of("outputData3")),
                new DatasetConsumerJob("newDownstreamJob", 1, Optional.empty()))),
        jobFacet,
        dataset);
    String jobName = writeJob.getJob().getName();
    Lineage lineage =
        lineageService.lineage(
            NodeId.of(new NamespaceName(NAMESPACE), new JobName(jobName)), 2, true);

    // 1 writeJob           + 1 commonDataset
    // 20 readJob           + 20 outputData
    // 20 downstreamJob     + 20 outputData2
    // 5 newReadJob         + 5 outputData3
    // 5 newDownstreamJob   + 0
    assertThat(lineage.getGraph())
        .hasSize(97) // 51 jobs + 46 datasets
        .areExactly(51, new Condition<>(n -> n.getType().equals(NodeType.JOB), "job"))
        .areExactly(46, new Condition<>(n -> n.getType().equals(NodeType.DATASET), "dataset"))
        // finalConsumer job is out of the depth range
        .filteredOn(
            node ->
                node.getType().equals(NodeType.JOB)
                    && node.getId().asJobId().getName().getValue().contains("finalConsumer"))
        .isEmpty();

    // assert the second run of writeJob is returned
    AbstractObjectAssert<?, Run> runAssert =
        assertThat(lineage.getGraph())
            .filteredOn(
                node -> node.getType().equals(NodeType.JOB) && jobNameEquals(node, "writeJob"))
            .hasSize(1)
            .first()
            .extracting(
                n -> ((JobData) n.getData()).getLatestRun(),
                InstanceOfAssertFactories.optional(Run.class))
            .isPresent()
            .get();
    runAssert.extracting(r -> r.getId().getValue()).isEqualTo(secondRun.getRun().getUuid());
    runAssert
        .extracting(Run::getInputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(0);
    runAssert
        .extracting(Run::getOutputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(1);

    // check the output edges for the commonDataset node
    assertThat(lineage.getGraph())
        .filteredOn(
            node ->
                node.getType().equals(NodeType.DATASET)
                    && node.getId().asDatasetId().getName().getValue().equals("commonDataset"))
        .first()
        .extracting(Node::getOutEdges, InstanceOfAssertFactories.iterable(Edge.class))
        .hasSize(25)
        .extracting(e -> e.getDestination().asJobId().getName())
        .allMatch(n -> n.getValue().matches(".*eadJob\\d+<-commonDataset"));

    assertThat(lineage.getGraph())
        .filteredOn(
            n ->
                n.getType().equals(NodeType.JOB)
                    && jobNameEquals(n, "downstreamJob0<-outputData<-readJob0<-commonDataset"))
        .hasSize(1)
        .first()
        .extracting(Node::getInEdges, InstanceOfAssertFactories.iterable(Edge.class))
        .hasSize(1)
        .first()
        .extracting(Edge::getOrigin)
        .isEqualTo(
            NodeId.of(
                new NamespaceName(NAMESPACE),
                new DatasetName("outputData<-readJob0<-commonDataset")));
  }

  @Test
  public void testLineageWithDeletedDataset() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("outputData")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.of("outputData2")),
                    new DatasetConsumerJob("finalConsumer", 1, Optional.empty()))),
            jobFacet,
            dataset);
    UpdateLineageRow secondRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    writeDownstreamLineage(
        openLineageDao,
        new LinkedList<>(
            Arrays.asList(
                new DatasetConsumerJob("newReadJob", 5, Optional.of("outputData3")),
                new DatasetConsumerJob("newDownstreamJob", 1, Optional.empty()))),
        jobFacet,
        dataset);

    datasetDao.delete(NAMESPACE, "commonDataset");

    String jobName = writeJob.getJob().getName();
    Lineage lineage =
        lineageService.lineage(
            NodeId.of(new NamespaceName(NAMESPACE), new JobName(jobName)), 2, true);

    // 1 writeJob           + 0 commonDataset is hidden
    // 20 readJob           + 20 outputData
    // 20 downstreamJob     + 20 outputData2
    // 5 newReadJob         + 5 outputData3
    // 5 newDownstreamJob   + 0
    assertThat(lineage.getGraph())
        .hasSize(96) // 51 jobs + 45 datasets - one is hidden
        .areExactly(51, new Condition<>(n -> n.getType().equals(NodeType.JOB), "job"))
        .areExactly(45, new Condition<>(n -> n.getType().equals(NodeType.DATASET), "dataset"))
        // finalConsumer job is out of the depth range
        .filteredOn(
            node ->
                node.getType().equals(NodeType.JOB)
                    && node.getId().asJobId().getName().getValue().contains("finalConsumer"))
        .isEmpty();

    // assert the second run of writeJob is returned
    AbstractObjectAssert<?, Run> runAssert =
        assertThat(lineage.getGraph())
            .filteredOn(
                node -> node.getType().equals(NodeType.JOB) && jobNameEquals(node, "writeJob"))
            .hasSize(1)
            .first()
            .extracting(
                n -> ((JobData) n.getData()).getLatestRun(),
                InstanceOfAssertFactories.optional(Run.class))
            .isPresent()
            .get();
    runAssert.extracting(r -> r.getId().getValue()).isEqualTo(secondRun.getRun().getUuid());
    runAssert
        .extracting(Run::getInputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(0);
    runAssert
        .extracting(Run::getOutputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(1);

    // check the output edges for the commonDataset node
    assertThat(lineage.getGraph())
        .filteredOn(
            node ->
                node.getType().equals(NodeType.DATASET)
                    && node.getId().asDatasetId().getName().getValue().equals("commonDataset"))
        .isEmpty();

    jobDao.delete(NAMESPACE, "downstreamJob0<-outputData<-readJob0<-commonDataset");

    lineage =
        lineageService.lineage(
            NodeId.of(new NamespaceName(NAMESPACE), new JobName(jobName)), 2, true);

    // 1 writeJob           + 0 commonDataset is hidden
    // 20 readJob           + 20 outputData
    // 20 downstreamJob     + 20 outputData2
    // 5 newReadJob         + 5 outputData3
    // 5 newDownstreamJob   + 0
    assertThat(lineage.getGraph())
        .hasSize(
            94) // 51 jobs + 45 datasets - one dataset is hidden + one job that produces dataset is
        // hidden
        .areExactly(50, new Condition<>(n -> n.getType().equals(NodeType.JOB), "job"))
        .areExactly(44, new Condition<>(n -> n.getType().equals(NodeType.DATASET), "dataset"));

    // assert that readJob is hidden
    assertThat(lineage.getGraph())
        .filteredOn(
            n ->
                n.getType().equals(NodeType.JOB)
                    && jobNameEquals(n, "downstreamJob0<-outputData<-readJob0<-commonDataset"))
        .isEmpty();
  }

  @Test
  public void testLineageWithNoDatasets() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());
    Lineage lineage =
        lineageService.lineage(
            NodeId.of(new NamespaceName(NAMESPACE), new JobName(writeJob.getJob().getName())),
            5,
            true);
    assertThat(lineage.getGraph())
        .hasSize(1)
        .first()
        .satisfies(n -> n.getId().asJobId().getName().getValue().equals("writeJob"));
  }

  @Test
  public void testLineageWithWithCycle() {
    Dataset intermediateDataset =
        new Dataset(
            NAMESPACE,
            "intermediateDataset",
            newDatasetFacet(
                new SchemaField("firstname", "string", "the first name"),
                new SchemaField("birthdate", "date", "the date of birth")));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "writeJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset),
        Arrays.asList(intermediateDataset));

    Dataset finalDataset =
        new Dataset(
            NAMESPACE,
            "finalDataset",
            newDatasetFacet(
                new SchemaField("firstname", "string", "the first name"),
                new SchemaField("lastname", "string", "the last name")));
    UpdateLineageRow intermediateJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "intermediateJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(intermediateDataset),
            Arrays.asList(finalDataset));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "cycleJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(finalDataset),
        Arrays.asList(dataset));
    Lineage lineage =
        lineageService.lineage(
            NodeId.of(
                new NamespaceName(NAMESPACE), new JobName(intermediateJob.getJob().getName())),
            5,
            true);
    assertThat(lineage.getGraph()).extracting(Node::getId).hasSize(6);
    ObjectAssert<Node> datasetNode =
        assertThat(lineage.getGraph())
            .filteredOn(
                n1 ->
                    n1.getId().isDatasetType()
                        && n1.getId().asDatasetId().getName().getValue().equals("commonDataset"))
            .hasSize(1)
            .first();
    datasetNode
        .extracting(Node::getInEdges, InstanceOfAssertFactories.iterable(Edge.class))
        .hasSize(1)
        .first()
        .extracting(Edge::getOrigin)
        .matches(n -> n.isJobType() && n.asJobId().getName().getValue().equals("cycleJob"));

    datasetNode
        .extracting(Node::getOutEdges, InstanceOfAssertFactories.iterable(Edge.class))
        .hasSize(1)
        .first()
        .extracting(Edge::getDestination)
        .matches(n -> n.isJobType() && n.asJobId().getName().getValue().equals("writeJob"));
  }

  @Test
  public void testLineageForOrphanedDataset() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));

    NodeId datasetNodeId =
        NodeId.of(new NamespaceName(dataset.getNamespace()), new DatasetName(dataset.getName()));
    Lineage lineage = lineageService.lineage(datasetNodeId, 2, false);
    assertThat(lineage.getGraph())
        .hasSize(2)
        .extracting(Node::getId)
        .containsExactlyInAnyOrder(
            NodeId.of(new JobId(new NamespaceName(NAMESPACE), new JobName("writeJob"))),
            datasetNodeId);

    UpdateLineageRow updatedWriteJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());

    lineage = lineageService.lineage(datasetNodeId, 2, false);
    assertThat(lineage.getGraph())
        .hasSize(1)
        .extracting(Node::getId)
        .containsExactlyInAnyOrder(datasetNodeId);
  }

  private boolean jobNameEquals(Node node, String writeJob) {
    return node.getId().asJobId().getName().getValue().equals(writeJob);
  }
}
