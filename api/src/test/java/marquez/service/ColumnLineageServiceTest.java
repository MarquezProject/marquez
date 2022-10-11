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
import marquez.common.models.NamespaceName;
import marquez.db.ColumnLineageDao;
import marquez.db.ColumnLineageTestUtils;
import marquez.db.DatasetFieldDao;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
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
  private static ColumnLineageService lineageService;
  private static LineageEvent.JobFacet jobFacet;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(ColumnLineageDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    fieldDao = jdbi.onDemand(DatasetFieldDao.class);
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
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")), 20, Instant.now());

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
            NodeId.of(DatasetFieldId.of("namespace", "dataset_b", "col_c")), 20, Instant.now());

    Lineage lineageByDataset =
        lineageService.lineage(
            NodeId.of(new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_b"))),
            20,
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
                Instant.now()));

    assertThrows(
        NodeIdNotFoundException.class,
        () ->
            lineageService.lineage(
                NodeId.of(
                    new DatasetId(NamespaceName.of("namespace"), DatasetName.of("dataset_d"))),
                20,
                Instant.now()));

    assertThat(
            lineageService
                .lineage(
                    NodeId.of(DatasetFieldId.of("namespace", "dataset_a", "col_a")),
                    20,
                    Instant.now())
                .getGraph())
        .hasSize(0);
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
