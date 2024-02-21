/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.db.ColumnLineageTestUtils.createLineage;
import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.ColumnLineageTestUtils.getDatasetC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import marquez.common.models.DatasetFieldId;
import marquez.common.models.DatasetFieldVersionId;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.FieldName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.db.ColumnLineageDao;
import marquez.db.ColumnLineageTestUtils;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.ColumnLineageInputField;
import marquez.service.models.Dataset;
import marquez.service.models.Lineage;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.JobFacet;
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

  private LineageEvent.Dataset dataset_A = getDatasetA();
  private LineageEvent.Dataset dataset_B = getDatasetB();
  private LineageEvent.Dataset dataset_C = getDatasetC();

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(ColumnLineageDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    fieldDao = jdbi.onDemand(DatasetFieldDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    lineageService = new ColumnLineageService(dao, fieldDao);
    jobFacet = JobFacet.builder().build();
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    ColumnLineageTestUtils.tearDown(jdbi);
  }

  @Test
  public void testLineageByDatasetFieldId() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    Lineage lineage =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")), 20, false);

    assertThat(lineage.getGraph()).hasSize(3);

    // check dataset_B node
    Node col_c = getNode(lineage, "dataset_b", "col_c").get();
    List<InputFieldNodeData> inputFields =
        ((ColumnLineageNodeData) col_c.getData()).getInputFields();
    assertEquals("description1", inputFields.get(0).getTransformationDescription());
    assertEquals("type1", inputFields.get(0).getTransformationType());
    assertEquals("STRING", ((ColumnLineageNodeData) col_c.getData()).getFieldType());
    assertThat(inputFields).hasSize(2);
    assertEquals("dataset_a", inputFields.get(0).getDataset());

    // check dataset_A node
    Node col_a = getNode(lineage, "dataset_a", "col_b").get();
    ColumnLineageNodeData col_a_data = (ColumnLineageNodeData) col_a.getData();
    assertThat(col_a_data.getInputFields()).hasSize(0);
    assertEquals("dataset_a", col_a_data.getDataset());
    assertEquals("", col_a_data.getFieldType());

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
    assertThat(
            lineage.getGraph().stream()
                .filter(node -> node.getId().isDatasetFieldVersionType())
                .findAny())
        .isEmpty(); // none of the graph nodes contains version
  }

  @Test
  public void testLineageByDatasetId() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    Lineage lineageByField =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")), 20, false);

    Lineage lineageByDataset =
        lineageService.lineage(
            NodeId.of(new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b"))),
            20,
            false);

    // lineage of dataset and column should be equal
    assertThat(lineageByField).isEqualTo(lineageByDataset);
    assertThat(
            lineageByDataset.getGraph().stream()
                .filter(node -> node.getId().isDatasetFieldVersionType())
                .findAny())
        .isEmpty(); // none of the graph nodes contains version
  }

  @Test
  public void testLineageWhenLineageEmpty() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    assertThrows(
        NodeIdNotFoundException.class,
        () ->
            lineageService.lineage(
                NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_d")), 20, false));

    assertThrows(
        NodeIdNotFoundException.class,
        () ->
            lineageService.lineage(
                NodeId.of(
                    new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_d"))),
                20,
                false));

    assertThat(
            lineageService
                .lineage(NodeId.of(DatasetFieldId.of("namespace", "dataset_a", "col_a")), 20, false)
                .getGraph())
        .hasSize(0);
  }

  @Test
  public void testEnrichDatasets() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    Dataset dataset_b = datasetDao.findDatasetByName("namespace", "dataset_b").get();
    Dataset dataset_c = datasetDao.findDatasetByName("namespace", "dataset_c").get();
    lineageService.enrichWithColumnLineage(Arrays.asList(dataset_b, dataset_c));

    assertThat(dataset_b.getColumnLineage()).hasSize(1);
    assertThat(dataset_b.getColumnLineage().get(0).getName()).isEqualTo("col_c");

    List<ColumnLineageInputField> inputFields_b =
        dataset_b.getColumnLineage().get(0).getInputFields();
    assertThat(inputFields_b)
        .hasSize(2)
        .contains(
            new ColumnLineageInputField("namespace", "dataset_a", "col_a", "description1", "type1"))
        .contains(
            new ColumnLineageInputField(
                "namespace", "dataset_a", "col_b", "description1", "type1"));

    assertThat(dataset_c.getColumnLineage()).hasSize(1);
    assertThat(dataset_c.getColumnLineage().get(0).getName()).isEqualTo("col_d");

    List<ColumnLineageInputField> inputFields_c =
        dataset_c.getColumnLineage().get(0).getInputFields();
    assertThat(inputFields_c)
        .hasSize(1)
        .contains(
            new ColumnLineageInputField(
                "namespace", "dataset_b", "col_c", "description2", "type2"));
  }

  @Test
  public void testGetLineageWithDownstream() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    Lineage lineage =
        lineageService.lineage(
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")), 20, true);

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
    createLineage(openLineageDao, dataset_A, dataset_B);
    createLineage(openLineageDao, dataset_B, dataset_C);

    Dataset dataset_b = datasetDao.findDatasetByName("namespace", "dataset_b").get();
    lineageService.enrichWithColumnLineage(Arrays.asList(dataset_b));
    assertThat(dataset_b.getColumnLineage()).hasSize(1);
  }

  @Test
  public void testGetLineageByJob() {
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
                NodeId.of(JobId.of(NamespaceName.of("namespace"), JobName.of("job1"))), 20, true))
        .isEqualTo(
            lineageService.lineage(
                NodeId.of(
                    new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b"))),
                20,
                true));
  }

  @Test
  public void testGetLineagePointInTime() {
    createLineage(openLineageDao, dataset_A, dataset_B);
    UpdateLineageRow lineageRow =
        createLineage(openLineageDao, dataset_A, dataset_B); // we will obtain this version
    createLineage(openLineageDao, dataset_A, dataset_B);

    Lineage lineage =
        lineageService.lineage(
            NodeId.of(
                new DatasetVersionId(
                    NamespaceName.of("namespace"),
                    DatasetName.of("dataset_b"),
                    lineageRow.getOutputs().get().get(0).getDatasetVersionRow().getUuid())),
            20,
            false);

    assertThat(lineage.getGraph().size()).isEqualTo(3); // col_a, col_b and col_c
    assertThat(
            getNode(lineage, "dataset_a", "col_b")
                .get()
                .getId()
                .asDatasetFieldVersionId()
                .getVersion())
        .isEqualTo(lineageRow.getInputs().get().get(0).getDatasetVersionRow().getUuid());
    assertThat(
            getNode(lineage, "dataset_b", "col_c")
                .get()
                .getId()
                .asDatasetFieldVersionId()
                .getVersion())
        .isEqualTo(lineageRow.getOutputs().get().get(0).getDatasetVersionRow().getUuid());

    // assert lineage by field version and by job are the same
    assertThat(lineage)
        .isEqualTo(
            lineageService.lineage(
                NodeId.of(
                    new DatasetFieldVersionId(
                        new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b")),
                        FieldName.of("col_c"),
                        lineageRow.getOutputs().get().get(0).getDatasetVersionRow().getUuid())),
                20,
                false));

    assertThat(lineage)
        .isEqualTo(
            lineageService.lineage(
                NodeId.of(
                    JobVersionId.of(
                        NamespaceName.of("namespace"),
                        JobName.of("job1"),
                        lineageRow.getJobVersionBag().getJobVersionRow().getUuid())),
                20,
                true));
  }

  private Optional<Node> getNode(Lineage lineage, String datasetName, String fieldName) {
    return lineage.getGraph().stream()
        .filter(
            n ->
                n.getId().isDatasetFieldVersionType()
                        && n.getId()
                            .asDatasetFieldVersionId()
                            .getFieldName()
                            .getValue()
                            .equals(fieldName)
                    || n.getId().asDatasetFieldId().getFieldName().getValue().equals(fieldName))
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
