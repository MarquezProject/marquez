/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.ColumnLineageTestUtils.getDatasetC;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import marquez.api.JdbiUtils;
import marquez.client.MarquezClient;
import marquez.client.models.DatasetFieldId;
import marquez.client.models.DatasetId;
import marquez.client.models.JobId;
import marquez.client.models.Node;
import marquez.client.models.NodeId;
import marquez.db.ColumnLineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.UpdateLineageRow;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class ColumnLineageIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    // Use the static Jdbi instance provided by MarquezApp
    Jdbi staticAppJdbi = MarquezApp.getJdbiInstanceForTesting();
    OpenLineageDao openLineageDao = staticAppJdbi.onDemand(OpenLineageDao.class);

    // Create namespace first
    createNamespace("namespace");
    System.out.println("DEBUG: Created namespace 'namespace'");

    // Use datasets that include schema + column lineage facets
    LineageEvent.Dataset datasetA = getDatasetA();
    LineageEvent.Dataset datasetB = getDatasetB();
    LineageEvent.Dataset datasetC = getDatasetC();
    System.out.println("DEBUG: Created test datasets:");
    System.out.println(
        "DEBUG: Dataset A: "
            + datasetA.getName()
            + " with fields: "
            + datasetA.getFacets().getSchema().getFields());
    System.out.println(
        "DEBUG: Dataset B: "
            + datasetB.getName()
            + " with fields: "
            + datasetB.getFacets().getSchema().getFields());
    System.out.println(
        "DEBUG: Dataset C: "
            + datasetC.getName()
            + " with fields: "
            + datasetC.getFacets().getSchema().getFields());

    // Use helper that sets column lineage correctly
    UpdateLineageRow lineage1 =
        ColumnLineageTestUtils.createLineage(
            openLineageDao, "job1", "COMPLETE", datasetA, datasetB);
    System.out.println(
        "DEBUG: Created lineage 1: job1 connecting "
            + datasetA.getName()
            + " -> "
            + datasetB.getName());
    System.out.println("DEBUG: Lineage 1 job: " + lineage1.getJob().getName());

    UpdateLineageRow lineage2 =
        ColumnLineageTestUtils.createLineage(
            openLineageDao, "job2", "COMPLETE", datasetB, datasetC);
    System.out.println(
        "DEBUG: Created lineage 2: job2 connecting "
            + datasetB.getName()
            + " -> "
            + datasetC.getName());
    System.out.println("DEBUG: Lineage 2 job: " + lineage2.getJob().getName());

    // Verify data in database
    System.out.println("DEBUG: Verifying data in database...");
    System.out.println("DEBUG: Checking datasets table...");
    staticAppJdbi.useHandle(
        handle -> {
          handle
              .createQuery("SELECT * FROM datasets_view WHERE namespace_name = 'namespace'")
              .mapToMap()
              .forEach(row -> System.out.println("DEBUG: Found dataset: " + row));
        });

    System.out.println("DEBUG: Checking column_lineage table...");
    staticAppJdbi.useHandle(
        handle -> {
          handle
              .createQuery("SELECT * FROM column_lineage")
              .mapToMap()
              .forEach(row -> System.out.println("DEBUG: Found column lineage: " + row));
        });
  }

  @AfterEach
  public void tearDown() {
    // Use the static Jdbi instance provided by MarquezApp for cleanup
    Jdbi staticAppJdbi = MarquezApp.getJdbiInstanceForTesting();
    JdbiUtils.cleanDatabase(staticAppJdbi);
  }

  @Test
  public void testColumnLineageEndpointByDataset() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new DatasetId("namespace", "dataset_b")));

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointByDatasetField() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new DatasetFieldId("namespace", "dataset_b", "col_c")));

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointWithDepthLimit() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(
            NodeId.of(new DatasetFieldId("namespace", "dataset_c", "col_d")), 1, false);

    assertThat(lineage.getGraph()).hasSize(2);
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointWithDownstream() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new JobId("namespace", "job1")), 10, true);

    assertThat(lineage.getGraph()).hasSize(4);
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointByJob() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new JobId("namespace", "job1")), 1, false);

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  private Optional<Node> getNodeByFieldName(MarquezClient.Lineage lineage, String field) {
    return lineage.getGraph().stream()
        .filter(n -> n.getId().asDatasetFieldId().getField().equals(field))
        .findAny();
  }
}
