/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import marquez.api.JdbiUtils;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class DatasetFacetsDaoTest {

  private static DatasetFacetsDao datasetFacetsDao;

  private static OpenLineageDao openLineageDao;

  private Jdbi jdbi;

  private Instant lineageEventTime = Instant.now();

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    datasetFacetsDao = jdbi.onDemand(DatasetFacetsDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @BeforeEach
  public void setup(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testInsertDatasetFacetsForDocumentationFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder()
                .documentation(
                    new LineageEvent.DocumentationDatasetFacet(
                        PRODUCER_URL, SCHEMA_URL, "some-doc")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "documentation");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue(
            "datasetVersionUuid",
            lineageRow.getInputs().get().get(0).getDatasetVersionRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString())
        .isEqualTo(
            "{\"documentation\": {\"_producer\": \"http://test.producer/\", "
                + "\"_schemaURL\": \"http://test.schema/\", \"description\": \"some-doc\"}}");
  }

  @Test
  public void testInsertDatasetFacetsForSchemaFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder()
                .schema(
                    new LineageEvent.SchemaDatasetFacet(
                        PRODUCER_URL, SCHEMA_URL, Collections.emptyList())));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "schema");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString())
        .isEqualTo(
            "{\"schema\": {\"fields\": [], \"_producer\": \"http://test.producer/\", "
                + "\"_schemaURL\": \"http://test.schema/\"}}");
  }

  @Test
  public void testInsertDatasetFacetsForDatasourceFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder()
                .dataSource(
                    new LineageEvent.DatasourceDatasetFacet(
                        PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "dataSource");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString())
        .isEqualTo(
            "{\"dataSource\": {\"uri\": \"http://thesource.com\", \"name\": \"the source\", "
                + "\"_producer\": \"http://test.producer/\", \"_schemaURL\": \"http://test.schema/\"}}");
  }

  @Test
  public void testInsertDatasetFacetsForDescriptionFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder().description("some-description"));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "description");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString()).isEqualTo("{\"description\": \"some-description\"}");
  }

  @Test
  public void testInsertDatasetFacetsForLifecycleStateChangeFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder()
                .lifecycleStateChange(new LineageEvent.LifecycleStateChangeFacet()));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "lifecycleStateChange");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString()).isEqualTo("{\"lifecycleStateChange\": {}}");
  }

  @Test
  public void testInsertDatasetFacetsForVersionFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("version", "some-version")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "version");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString()).isEqualTo("{\"version\": \"some-version\"}");
  }

  @Test
  public void testInsertDatasetFacetsForColumnLineageFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder()
                .columnLineage(new LineageEvent.ColumnLineageDatasetFacet()));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "columnLineage");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString()).isEqualTo("{\"columnLineage\": {}}");
  }

  @Test
  public void testInsertDatasetFacetsForOwnershipFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("ownership", "some-owner")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "ownership");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.DATASET);

    assertThat(facet.facet().toString()).isEqualTo("{\"ownership\": \"some-owner\"}");
  }

  @Test
  public void testInsertDatasetFacetsForDataQualityMetricsFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("dataQualityMetrics", "m1")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "dataQualityMetrics");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.INPUT);

    assertThat(facet.facet().toString()).isEqualTo("{\"dataQualityMetrics\": \"m1\"}");
  }

  @Test
  public void testInsertDatasetFacetsForDataQualityAssertionsFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithInputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("dataQualityAssertions", "m2")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "dataQualityAssertions");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.INPUT);

    assertThat(facet.facet().toString()).isEqualTo("{\"dataQualityAssertions\": \"m2\"}");
  }

  @Test
  public void testInsertDatasetFacetsForOutputStatisticsFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithOutputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("outputStatistics", "m3")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "outputStatistics");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getOutputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.OUTPUT);

    assertThat(facet.facet().toString()).isEqualTo("{\"outputStatistics\": \"m3\"}");
  }

  @Test
  public void testInsertDatasetFacetsForUnknownTypeFacet() {
    UpdateLineageRow lineageRow =
        createLineageRowWithOutputDataset(
            LineageEvent.DatasetFacets.builder().additional(Map.of("custom-output", "{whatever}")));

    DatasetFacetsDao.DatasetFacetRow facet = getDatasetFacet(lineageRow, "custom-output");

    assertThat(facet)
        .hasFieldOrPropertyWithValue(
            "datasetUuid", lineageRow.getOutputs().get().get(0).getDatasetRow().getUuid())
        .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
        .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
        .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
        .hasFieldOrPropertyWithValue("type", DatasetFacetsDao.Type.UNKNOWN);

    assertThat(facet.facet().toString()).isEqualTo("{\"custom-output\": \"{whatever}\"}");
  }

  private UpdateLineageRow createLineageRowWithInputDataset(
      LineageEvent.DatasetFacets.DatasetFacetsBuilder inputDatasetFacetsbuilder) {
    LineageEvent.JobFacet jobFacet =
        new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    return LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        jobFacet,
        Arrays.asList(
            new LineageEvent.Dataset(
                "namespace", "dataset_input", inputDatasetFacetsbuilder.build())),
        Collections.emptyList(),
        null);
  }

  private UpdateLineageRow createLineageRowWithOutputDataset(
      LineageEvent.DatasetFacets.DatasetFacetsBuilder outputDatasetFacetsbuilder) {
    LineageEvent.JobFacet jobFacet =
        new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    return LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Arrays.asList(
            new LineageEvent.Dataset(
                "namespace", "dataset_output", outputDatasetFacetsbuilder.build())),
        null);
  }

  private DatasetFacetsDao.DatasetFacetRow getDatasetFacet(
      UpdateLineageRow lineageRow, String facetName) {
    return jdbi.withHandle(
        h ->
            h.createQuery("SELECT * FROM dataset_facets WHERE name = :facetName")
                .bind("facetName", facetName)
                .map(
                    rv ->
                        new DatasetFacetsDao.DatasetFacetRow(
                            rv.getColumn("created_at", Instant.class),
                            rv.getColumn("dataset_uuid", UUID.class),
                            rv.getColumn("dataset_version_uuid", UUID.class),
                            rv.getColumn("run_uuid", UUID.class),
                            rv.getColumn("lineage_event_time", Instant.class),
                            rv.getColumn("lineage_event_type", String.class),
                            rv.getColumn("type", DatasetFacetsDao.Type.class),
                            rv.getColumn("name", String.class),
                            rv.getColumn("facet", PGobject.class)))
                .one());
  }
}
