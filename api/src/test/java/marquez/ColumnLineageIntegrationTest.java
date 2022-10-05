/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.ColumnLineageTestUtils.getDatasetC;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Optional;
import marquez.api.JdbiUtils;
import marquez.client.MarquezClient;
import marquez.client.models.Node;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLineageIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup(Jdbi jdbi) {
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);

    LineageEvent.JobFacet jobFacet =
        new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

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
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testColumnLineageEndpointByDataset() {
    MarquezClient.Lineage lineage = client.getColumnLineage("namespace", "dataset_b");

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointByDatasetField() {
    MarquezClient.Lineage lineage = client.getColumnLineage("namespace", "dataset_b", "col_c");

    assertThat(lineage.getGraph()).hasSize(3);
    assertThat(getNodeByFieldName(lineage, "col_a")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_b")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointWithDepthLimit() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage("namespace", "dataset_c", "col_d", 1, false);

    assertThat(lineage.getGraph()).hasSize(2);
    assertThat(getNodeByFieldName(lineage, "col_c")).isPresent();
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  @Test
  public void testColumnLineageEndpointWithDownstream() {
    MarquezClient.Lineage lineage =
        client.getColumnLineage("namespace", "dataset_b", "col_c", 10, true);

    assertThat(lineage.getGraph()).hasSize(4);
    assertThat(getNodeByFieldName(lineage, "col_d")).isPresent();
  }

  private Optional<Node> getNodeByFieldName(MarquezClient.Lineage lineage, String field) {
    return lineage.getGraph().stream()
        .filter(n -> n.getId().asDatasetFieldId().getField().equals(field))
        .findAny();
  }
}
