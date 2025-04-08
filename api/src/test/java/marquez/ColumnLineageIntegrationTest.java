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
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLineageIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup(Jdbi jdbi) {
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);

    // Use datasets that include schema + column lineage facets
    LineageEvent.Dataset datasetA = getDatasetA();
    LineageEvent.Dataset datasetB = getDatasetB();
    LineageEvent.Dataset datasetC = getDatasetC();

    // Use helper that sets column lineage correctly
    ColumnLineageTestUtils.createLineage(openLineageDao, "job1", "COMPLETE", datasetA, datasetB);
    ColumnLineageTestUtils.createLineage(openLineageDao, "job2", "COMPLETE", datasetB, datasetC);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  @Disabled
  public void testColumnLineageEndpointByDataset() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new DatasetId("namespace", "dataset_b")));

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  @Disabled
  public void testColumnLineageEndpointByDatasetField() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new DatasetFieldId("namespace", "dataset_b", "col_c")));

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  @Disabled
  public void testColumnLineageEndpointWithDepthLimit() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(
            NodeId.of(new DatasetFieldId("namespace", "dataset_c", "col_d")), 1, false);

    assertThat(lineage.getGraph()).hasSize(2);
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  @Test
  @Disabled
  public void testColumnLineageEndpointWithDownstream() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage(NodeId.of(new JobId("namespace", "job1")), 10, true);

    assertThat(lineage.getGraph()).hasSize(4);
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  @Test
  @Disabled
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
