/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static marquez.db.DatasetFacetsDao.Type.DATASET;
import static marquez.db.DatasetFacetsDao.Type.INPUT;
import static marquez.db.DatasetFacetsDao.Type.OUTPUT;
import static marquez.db.DatasetFacetsDao.Type.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.api.JdbiUtils;
import marquez.db.Columns;
import marquez.db.DatasetFacetsDao;
import marquez.db.FacetTestUtils;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.flywaydb.core.api.migration.Context;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.postgresql.util.PGobject;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class V57_1__BackfillFacetsTest {

  private static V57_1__BackfillFacets subject = new V57_1__BackfillFacets();
  private static Jdbi jdbi;

  private static OpenLineageDao openLineageDao;

  UpdateLineageRow lineageRow;
  Context flywayContext = mock(Context.class);
  Connection connection = mock(Connection.class);

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V57_1__BackfillFacetsTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @BeforeEach
  public void beforeEach() {
    when(flywayContext.getConnection()).thenReturn(connection);
    subject.setChunkSize(100);
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testDatasetFacet() throws Exception {
    lineageRow = FacetTestUtils.createLineageWithFacets(openLineageDao);
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      List<DatasetFacet> expectedInputDatasetFacets =
          getDatasetFacetsFor(lineageRow.getRun().getUuid(), "namespace", "dataset_input");
      List<DatasetFacet> expectedOutputDatasetFacets =
          getDatasetFacetsFor(lineageRow.getRun().getUuid(), "namespace", "dataset_output");

      // clear dataset_facets table
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM dataset_facets"));
      subject.migrate(flywayContext);

      List<DatasetFacet> inputDatasetFacets =
          getDatasetFacetsFor(lineageRow.getRun().getUuid(), "namespace", "dataset_input");
      List<DatasetFacet> outputDatasetFacets =
          getDatasetFacetsFor(lineageRow.getRun().getUuid(), "namespace", "dataset_output");

      assertThat(inputDatasetFacets).hasSize(10);
      assertThat(inputDatasetFacets)
          .containsExactlyInAnyOrder(expectedInputDatasetFacets.toArray(new DatasetFacet[0]));

      assertThat(outputDatasetFacets).hasSize(5);
      assertThat(outputDatasetFacets)
          .containsExactlyInAnyOrder(expectedOutputDatasetFacets.toArray(new DatasetFacet[0]));

      assertThat(inputDatasetFacets).hasSize(10);
      assertThat(getDatasetFacetType(inputDatasetFacets, "documentation")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "schema")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "dataSource")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "description")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "lifecycleStateChange"))
          .isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "version")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "ownership")).isEqualTo(DATASET);
      assertThat(getDatasetFacetType(inputDatasetFacets, "dataQualityMetrics")).isEqualTo(INPUT);
      assertThat(getDatasetFacetType(inputDatasetFacets, "dataQualityAssertions")).isEqualTo(INPUT);
      assertThat(getDatasetFacetType(inputDatasetFacets, "custom-input")).isEqualTo(UNKNOWN);

      assertThat(
              inputDatasetFacets.stream()
                  .filter(df -> df.name.equals("dataSource"))
                  .findFirst()
                  .get())
          .hasFieldOrPropertyWithValue("runUuid", lineageRow.getRun().getUuid())
          .hasFieldOrPropertyWithValue(
              "datasetUuid", lineageRow.getInputs().get().get(0).getDatasetRow().getUuid())
          .hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE")
          .hasFieldOrPropertyWithValue("lineageEventTime", lineageRow.getRun().getCreatedAt())
          .hasFieldOrPropertyWithValue("type", DATASET)
          .hasFieldOrPropertyWithValue("name", "dataSource");

      assertThat(
              inputDatasetFacets.stream()
                  .filter(df -> df.name.equals("dataSource"))
                  .findFirst()
                  .get()
                  .facet
                  .toString())
          .isEqualTo(
              "{\"dataSource\": {\"uri\": \"http://thesource.com\", \"name\": \"the source\", \"_producer\": \"http://test.producer/\", \"_schemaURL\": \"http://test.schema/\"}}");

      assertThat(outputDatasetFacets).hasSize(5);
      assertThat(outputDatasetFacets.stream().map(df -> df.name).collect(Collectors.toList()))
          .contains("dataSource", "schema", "custom-output");
      assertThat(getDatasetFacetType(outputDatasetFacets, "outputStatistics")).isEqualTo(OUTPUT);
      assertThat(getDatasetFacetType(outputDatasetFacets, "columnLineage")).isEqualTo(DATASET);
    }
  }

  @Test
  public void testMigrateForMultipleChunks() throws Exception {
    lineageRow = FacetTestUtils.createLineageWithFacets(openLineageDao);
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);
      subject.setChunkSize(1);

      int datasetsFacetsBefore = countDatasetFacets(jdbi);
      FacetTestUtils.createLineageWithFacets(openLineageDao);
      lineageRow =
          FacetTestUtils.createLineageWithFacets(
              openLineageDao); // inserted three lineage event rows

      // clear migration lock and dataset_facets table
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM dataset_facets"));
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM facet_migration_lock"));
      subject.migrate(flywayContext);

      int datasetsFacetsAfter = countDatasetFacets(jdbi);
      assertThat(datasetsFacetsAfter).isEqualTo(3 * datasetsFacetsBefore);
    }
  }

  @Test
  public void testMigrateForLineageWithNoDatasets() throws Exception {
    LineageEvent.JobFacet jobFacet =
        new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.emptyList());
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      subject.migrate(flywayContext);

      int datasetsFacetsAfter = countDatasetFacets(jdbi);

      assertThat(datasetsFacetsAfter).isEqualTo(0);
    }
  }

  @Test
  public void testRunFacet() throws Exception {
    lineageRow = FacetTestUtils.createLineageWithFacets(openLineageDao);
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      List<RunFacet> runFacetsBefore = getRunFacetsFor(lineageRow.getRun().getUuid());
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM dataset_facets"));
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM run_facets"));
      subject.migrate(flywayContext);

      List<RunFacet> runFacets = getRunFacetsFor(lineageRow.getRun().getUuid());

      assertThat(runFacets).hasSize(5);
      assertThat(runFacetsBefore).containsExactlyInAnyOrder(runFacets.toArray(new RunFacet[0]));
      assertThat(runFacets.get(0)).hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE");
      assertThat(runFacets.stream().map(rf -> rf.name).collect(Collectors.toList()))
          .containsExactlyInAnyOrder(
              "parent", "custom-run-facet", "spark.logicalPlan", "errorMessage", "nominalTime");

      assertThat(
              runFacets.stream()
                  .filter(rf -> rf.name().equalsIgnoreCase("parent"))
                  .map(rf -> rf.facet().toString())
                  .findFirst()
                  .get())
          .isEqualTo(
              String.format(
                  "{\"parent\": {\"job\": {\"name\": \"name\", \"namespace\": \"namespace\"}, "
                      + "\"run\": {\"runId\": \"%s\"}, "
                      + "\"_producer\": \"http://test.producer/\", "
                      + "\"_schemaURL\": \"http://test.schema/\""
                      + "}}",
                  lineageRow.getRun().getParentRunUuid().get()));
    }
  }

  @Test
  public void testJobFacet() throws Exception {
    lineageRow = FacetTestUtils.createLineageWithFacets(openLineageDao);
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      List<JobFacet> jobFacetsBefore = getJobFacetsFor(lineageRow.getRun().getJobUuid());
      jdbi.inTransaction(handle -> handle.execute("DELETE FROM job_facets"));
      subject.migrate(flywayContext);

      List<JobFacet> jobFacets = getJobFacetsFor(lineageRow.getRun().getJobUuid());

      assertThat(jobFacets).hasSize(5);
      assertThat(jobFacetsBefore).containsExactlyInAnyOrder(jobFacets.toArray(new JobFacet[0]));

      assertThat(jobFacets.get(0)).hasFieldOrPropertyWithValue("lineageEventType", "COMPLETE");
      assertThat(jobFacets.stream().map(rf -> rf.name).collect(Collectors.toList()))
          .containsExactlyInAnyOrder(
              "sourceCodeLocation", "sourceCode", "documentation", "sql", "ownership");

      assertThat(
              jobFacets.stream()
                  .filter(rf -> rf.name().equalsIgnoreCase("sourceCodeLocation"))
                  .map(rf -> rf.facet().toString())
                  .findFirst()
                  .get())
          .isEqualTo(
              "{\"sourceCodeLocation\": {\"url\": \"git@github.com:OpenLineage/OpenLineage.git\", "
                  + "\"type\": \"git\", \"_producer\": \"http://test.producer/\", "
                  + "\"_schemaURL\": \"http://test.schema/\"}}");
    }
  }

  @Test
  public void testMigrationLockIsInsertedWhenNoDataToMigrate() throws Exception {
    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      // should be no data to import
      subject.migrate(flywayContext);

      Instant lockCreatedAt =
          jdbi.withHandle(
              h ->
                  h.createQuery("SELECT created_at FROM facet_migration_lock")
                      .map(rs -> rs.getColumn("created_at", Instant.class))
                      .one());

      // verify migration lock exists
      assertThat(lockCreatedAt).isBefore(Instant.now());
    }
  }

  @Test
  public void testMigrationForLineageEventsWithNullCreatedAtField() throws Exception {
    FacetTestUtils.createLineageWithFacets(openLineageDao);
    FacetTestUtils.createLineageWithFacets(openLineageDao);
    jdbi.inTransaction(h -> h.execute("UPDATE lineage_events SET created_at = NULL"));

    try (MockedStatic<Jdbi> jdbiMockedStatic = Mockito.mockStatic(Jdbi.class)) {
      when(Jdbi.create(connection)).thenReturn(jdbi);

      jdbi.inTransaction(handle -> handle.execute("DELETE FROM facet_migration_lock"));
      subject.migrate(flywayContext);
    }
  }

  private int countDatasetFacets(Jdbi jdbi) {
    return jdbi.withHandle(
        h ->
            h.createQuery("SELECT count(*) as cnt FROM dataset_facets")
                .map(rs -> rs.getColumn("cnt", Integer.class))
                .one());
  }

  private DatasetFacetsDao.Type getDatasetFacetType(List<DatasetFacet> facets, String facetName) {
    return facets.stream()
        .filter(df -> df.name.equalsIgnoreCase(facetName))
        .findFirst()
        .map(df -> df.type)
        .orElse(null);
  }

  private List<DatasetFacet> getDatasetFacetsFor(
      UUID runUuid, String datasetNamespace, String datasetName) {
    return jdbi.withHandle(
        h ->
            h
                .createQuery(
                    """
                                SELECT df.* FROM dataset_facets df
                                JOIN datasets_view d ON d.uuid = df.dataset_uuid
                                WHERE df.run_uuid = :runUuid AND d.name = :datasetName
                                AND d.namespace_name = :datasetNamespace
                            """)
                .bind("runUuid", runUuid)
                .bind("datasetNamespace", datasetNamespace)
                .bind("datasetName", datasetName)
                .map(
                    rs ->
                        new DatasetFacet(
                            // rs.getColumn("uuid", UUID.class), omit uuid field
                            // rs.getColumn(Columns.CREATED_AT, Instant.class), created_at field can
                            // differ
                            rs.getColumn(Columns.DATASET_UUID, UUID.class),
                            rs.getColumn(Columns.RUN_UUID, UUID.class),
                            rs.getColumn("lineage_event_time", Instant.class),
                            rs.getColumn("lineage_event_type", String.class),
                            rs.getColumn(Columns.TYPE, DatasetFacetsDao.Type.class),
                            rs.getColumn(Columns.NAME, String.class),
                            rs.getColumn("facet", PGobject.class)))
                .stream()
                .toList());
  }

  private List<RunFacet> getRunFacetsFor(UUID runUuid) {
    return jdbi.withHandle(
        h ->
            h
                .createQuery("SELECT * FROM run_facets WHERE run_uuid = :runUuid")
                .bind("runUuid", runUuid)
                .map(
                    rs ->
                        new RunFacet(
                            // rs.getColumn("uuid", UUID.class), omit uuid field
                            // rs.getColumn(Columns.CREATED_AT, Instant.class), created_at field can
                            // differ
                            rs.getColumn(Columns.RUN_UUID, UUID.class),
                            rs.getColumn("lineage_event_time", Instant.class),
                            rs.getColumn("lineage_event_type", String.class),
                            rs.getColumn(Columns.NAME, String.class),
                            rs.getColumn("facet", PGobject.class)))
                .stream()
                .toList());
  }

  private List<JobFacet> getJobFacetsFor(UUID jobUuid) {
    return jdbi.withHandle(
        h ->
            h
                .createQuery("SELECT * FROM job_facets WHERE job_uuid = :jobUuid")
                .bind("jobUuid", jobUuid)
                .map(
                    rs ->
                        new JobFacet(
                            rs.getColumn(Columns.JOB_UUID, UUID.class),
                            rs.getColumn(Columns.RUN_UUID, UUID.class),
                            rs.getColumn("lineage_event_time", Instant.class),
                            rs.getColumn("lineage_event_type", String.class),
                            rs.getColumn(Columns.NAME, String.class),
                            rs.getColumn("facet", PGobject.class)))
                .stream()
                .toList());
  }

  record JobFacet(
      // UUID uuid, omit uuid field
      // Instant createdAt, createdAt field can differ
      UUID jobUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet) {}

  record DatasetFacet(
      // UUID uuid, omit uuid field
      // Instant createdAt, createdAt field can differ
      UUID datasetUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      DatasetFacetsDao.Type type,
      String name,
      PGobject facet) {}

  record RunFacet(
      // UUID uuid, omit uuid field
      // Instant createdAt, createdAt field can differ
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet) {}
}
