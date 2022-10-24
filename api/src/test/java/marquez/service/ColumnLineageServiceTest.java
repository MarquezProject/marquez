/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.ColumnLineageTestUtils.getDatasetC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import marquez.common.models.DatasetFieldId;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.ColumnLineageDao;
import marquez.db.ColumnLineageTestUtils;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.ColumnLineageInputField;
import marquez.service.models.Dataset;
import marquez.service.models.Lineage;
import marquez.service.models.LineageEvent;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLineageServiceTest {

  private static ColumnLineageDao dao;
  private static OpenLineageDao openLineageDao;
  private static DatasetFieldDao fieldDao;
  private static DatasetDao datasetDao;
  private static ColumnLineageService lineageService;
  private static LineageEvent.JobFacet jobFacet;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(ColumnLineageDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    fieldDao = jdbi.onDemand(DatasetFieldDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    lineageService = new ColumnLineageService(dao, fieldDao);
    jobFacet = new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    ColumnLineageTestUtils.tearDown(jdbi);
  }

  @Test
  public void testLineageByDatasetFieldId() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    Lineage lineage =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")),
            20,
            false,
            Instant.now());

    assertThat(lineage.getGraph()).hasSize(3);

    // check dataset_B node
    Node col_c = getNode(lineage, "dataset_b", "col_c").get();
    List<InputFieldNodeData> inputFields =
        ((ColumnLineageNodeData) col_c.getData()).getInputFields();
    assertEquals(
        "description1", ((ColumnLineageNodeData) col_c.getData()).getTransformationDescription());
    assertEquals("type1", ((ColumnLineageNodeData) col_c.getData()).getTransformationType());
    assertEquals("STRING", ((ColumnLineageNodeData) col_c.getData()).getFieldType());
    assertThat(inputFields).hasSize(2);
    assertEquals("dataset_a", inputFields.get(0).getDataset());

    // check dataset_A node
    Node col_a = getNode(lineage, "dataset_a", "col_b").get();
    assertNull((ColumnLineageNodeData) col_a.getData());

    // verify edges
    // assert dataset_B (col_c) -> dataset_A (col_a)
    assertThat(col_c.getOutEdges()).isEmpty();
    assertThat(
            col_c.getInEdges().stream()
                .map(edge -> edge.getDestination().asDatasetFieldId())
                .filter(field -> field.getFieldName().getValue().equals("col_a"))
                .filter(field -> field.getDatasetId().getName().getValue().equals("dataset_a"))
                .findAny())
        .isPresent();

    assertThat(col_a.getInEdges()).isEmpty();
    assertThat(
            col_a.getOutEdges().stream()
                .map(edge -> edge.getDestination().asDatasetFieldId())
                .filter(field -> field.getFieldName().getValue().equals("col_c"))
                .filter(field -> field.getDatasetId().getName().getValue().equals("dataset_b"))
                .findAny())
        .isPresent();

    // verify dataset_C not present in the graph
    assertThat(getNode(lineage, "dataset_c", "col_d")).isEmpty();
  }

  @Test
  public void testLineageByDatasetId() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    Lineage lineageByField =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")),
            20,
            false,
            Instant.now());

    Lineage lineageByDataset =
        lineageService.lineage(
            NodeId.of(new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b"))),
            20,
            false,
            Instant.now());

    // lineage of dataset and column should be equal
    assertThat(lineageByField).isEqualTo(lineageByDataset);
  }

  @Test
  public void testLineageWhenLineageEmpty() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    assertThrows(
        NodeIdNotFoundException.class,
        () ->
            lineageService.lineage(
                NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_d")),
                20,
                false,
                Instant.now()));

    assertThrows(
        NodeIdNotFoundException.class,
        () ->
            lineageService.lineage(
                NodeId.of(
                    new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_d"))),
                20,
                false,
                Instant.now()));

    assertThat(
            lineageService
                .lineage(
                    NodeId.of(DatasetFieldId.of("namespace", "dataset_a", "col_a")),
                    20,
                    false,
                    Instant.now())
                .getGraph())
        .hasSize(0);
  }

  @Test
  public void testEnrichDatasets() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    Dataset dataset_b = datasetDao.findDatasetByName("namespace", "dataset_b").get();
    Dataset dataset_c = datasetDao.findDatasetByName("namespace", "dataset_c").get();
    lineageService.enrichWithColumnLineage(Arrays.asList(dataset_b, dataset_c));

    assertThat(dataset_b.getColumnLineage()).hasSize(1);
    assertThat(dataset_b.getColumnLineage().get(0).getName()).isEqualTo("col_c");
    assertThat(dataset_b.getColumnLineage().get(0).getTransformationType()).isEqualTo("type1");
    assertThat(dataset_b.getColumnLineage().get(0).getTransformationDescription())
        .isEqualTo("description1");

    List<ColumnLineageInputField> inputFields_b =
        dataset_b.getColumnLineage().get(0).getInputFields();
    assertThat(inputFields_b)
        .hasSize(2)
        .contains(new ColumnLineageInputField("namespace", "dataset_a", "col_a"))
        .contains(new ColumnLineageInputField("namespace", "dataset_a", "col_b"));

    assertThat(dataset_c.getColumnLineage()).hasSize(1);
    assertThat(dataset_c.getColumnLineage().get(0).getName()).isEqualTo("col_d");
    assertThat(dataset_c.getColumnLineage().get(0).getTransformationType()).isEqualTo("type2");
    assertThat(dataset_c.getColumnLineage().get(0).getTransformationDescription())
        .isEqualTo("description2");

    List<ColumnLineageInputField> inputFields_c =
        dataset_c.getColumnLineage().get(0).getInputFields();
    assertThat(inputFields_c)
        .hasSize(1)
        .contains(new ColumnLineageInputField("namespace", "dataset_b", "col_c"));
  }

  @Test
  public void testGetLineageWithDownstream() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    Lineage lineage =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")),
            20,
            true,
            Instant.now());

    // assert that get lineage of dataset_B should co also return dataset_A and dataset_C
    assertThat(
            lineage.getGraph().stream()
                .filter(c -> c.getId().asDatasetFieldId().getFieldName().getValue().equals("col_a"))
                .findAny())
        .isPresent();
    assertThat(
            lineage.getGraph().stream()
                .filter(c -> c.getId().asDatasetFieldId().getFieldName().getValue().equals("col_d"))
                .findAny())
        .isPresent();

    ColumnLineageNodeData nodeData_C =
        (ColumnLineageNodeData)
            lineage.getGraph().stream()
                .filter(c -> c.getId().asDatasetFieldId().getFieldName().getValue().equals("col_c"))
                .findAny()
                .get()
                .getData();
    assertThat(nodeData_C.getInputFields()).hasSize(2);
  }

  @Test
  public void testEnrichDatasetsHasNoDuplicates() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();

    // run job twice
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    Dataset dataset_b = datasetDao.findDatasetByName("namespace", "dataset_b").get();
    lineageService.enrichWithColumnLineage(Arrays.asList(dataset_b));
    assertThat(dataset_b.getColumnLineage()).hasSize(1);
  }

  @Test
  public void testGetLineageByJob() {
    LineageEvent.Dataset dataset_A = getDatasetA();
    LineageEvent.Dataset dataset_B = getDatasetB();
    LineageEvent.Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    // getting lineage by job_1 should be the same as getting it by dataset_B
    assertThat(
            lineageService.lineage(
                NodeId.of(JobId.of(NamespaceName.of("namespace"), JobName.of("job1"))),
                20,
                true,
                Instant.now()))
        .isEqualTo(
            lineageService.lineage(
                NodeId.of(
                    new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b"))),
                20,
                true,
                Instant.now()));
  }

  private Optional<Node> getNode(Lineage lineage, String datasetName, String fieldName) {
    return lineage.getGraph().stream()
        .filter(n -> n.getId().asDatasetFieldId().getFieldName().getValue().equals(fieldName))
        .filter(
            n ->
                n.getId()
                    .asDatasetFieldId()
                    .getDatasetId()
                    .getName()
                    .getValue()
                    .equals(datasetName))
        .findAny();
  }
}
