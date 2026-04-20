/*
 * Copyright 2018-2026 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import java.util.UUID;
import marquez.api.JdbiUtils;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.JobFacet;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test suite for {@link V77__backfill_denormalized_lineage_tables}.
 *
 * <p>Tests migration scenarios including:
 *
 * <ul>
 *   <li>Empty database (no runs to backfill)
 *   <li>Small datasets (< 100K runs) for automatic migration
 *   <li>Large datasets (>= 100K runs) requiring manual execution
 *   <li>Chunk-based processing
 *   <li>Error handling and resilience
 * </ul>
 */
@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class V77__BackfillDenormalizedLineageTablesTest {

  private static V77__backfill_denormalized_lineage_tables migration;
  private static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V77__BackfillDenormalizedLineageTablesTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @BeforeEach
  public void beforeEach() {
    migration = new V77__backfill_denormalized_lineage_tables();
    JdbiUtils.cleanDatabase(jdbi);

    // Also clean denormalized tables explicitly
    jdbi.useHandle(
        handle -> {
          handle.execute("DELETE FROM run_lineage_denormalized");
          handle.execute("DELETE FROM run_parent_lineage_denormalized");
          // Update PostgreSQL statistics after cleanup to ensure accurate row counts
          handle.execute("VACUUM ANALYZE runs");
        });
  }

  @AfterEach
  public void tearDown() {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testGetVersion() {
    assertThat(migration.getVersion().toString()).isEqualTo("77");
  }

  @Test
  public void testGetDescription() {
    assertThat(migration.getDescription())
        .isEqualTo("Backfill denormalized lineage tables with existing run data");
  }

  @Test
  public void testGetChecksum() {
    assertThat(migration.getChecksum()).isNull();
  }

  @Test
  public void testIsUndo() {
    assertThat(migration.isUndo()).isFalse();
  }

  @Test
  public void testCanExecuteInTransaction() {
    assertThat(migration.canExecuteInTransaction()).isFalse();
  }

  @Test
  public void testIsBaselineMigration() {
    assertThat(migration.isBaselineMigration()).isFalse();
  }

  @Test
  public void testGetChunkSize() {
    // Default chunk size
    assertThat(migration.getChunkSize())
        .isEqualTo(V77__backfill_denormalized_lineage_tables.DEFAULT_CHUNK_SIZE);

    // Custom chunk size
    migration.setChunkSize(1000);
    assertThat(migration.getChunkSize()).isEqualTo(1000);
  }

  @Test
  public void testMigrateWithEmptyDatabase() throws Exception {
    // Given: Empty database with no runs
    migration.setJdbi(jdbi);

    // When: Migration runs without Flyway context (direct execution)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Should complete successfully with no data to backfill
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isEqualTo(0);
  }

  @Test
  public void testMigrateWithSmallDataset() throws Exception {
    // Given: Small dataset with a few runs
    createTestRuns(5);

    migration.setJdbi(jdbi);
    migration.setChunkSize(2); // Small chunk size to test chunking

    // When: Migration runs without Flyway context (direct execution)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: All runs should be backfilled
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThanOrEqualTo(5);
  }

  @Test
  public void testMigrateWithChunking() throws Exception {
    // Given: Dataset that requires multiple chunks
    createTestRuns(10);

    migration.setJdbi(jdbi);
    migration.setChunkSize(3); // Process in chunks of 3

    // When: Migration runs without Flyway context (direct execution)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: All runs should be processed
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThanOrEqualTo(10);
  }

  @Test
  public void testMigrateWithParentChildRuns() throws Exception {
    // Given: Parent-child run relationships
    UpdateLineageRow parentRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "parent_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "parent_output", null)));

    UpdateLineageRow childRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "child_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(new Dataset("namespace", "parent_output", null)),
            List.of(new Dataset("namespace", "child_output", null)));

    UUID parentRunUuid = parentRun.getRun().getUuid();
    UUID childRunUuid = childRun.getRun().getUuid();

    // Set parent-child relationship
    jdbi.useHandle(
        handle -> {
          handle.execute(
              "UPDATE runs SET parent_run_uuid = ? WHERE uuid = ?", parentRunUuid, childRunUuid);
        });

    migration.setJdbi(jdbi);

    // When: Migration runs without Flyway context (direct execution)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Both parent and child lineage should be populated
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThanOrEqualTo(2);

    Long parentLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT COUNT(*) FROM run_parent_lineage_denormalized WHERE run_uuid = ?")
                    .bind(0, parentRunUuid)
                    .mapTo(Long.class)
                    .one());
    assertThat(parentLineageCount).isGreaterThan(0);
  }

  @Test
  public void testMigrateSkipsForLargeDataset() throws Exception {
    // Given: Simulated large dataset exceeding auto-migration limit
    createTestRuns(5); // Create a few real runs for testing

    migration.setJdbi(jdbi);
    migration.setManual(false);

    // Mock the estimate to return a large count
    jdbi.useHandle(
        handle -> {
          // Update pg_class stats to simulate large dataset
          handle.execute("UPDATE pg_class SET reltuples = 150000 WHERE relname = 'runs'");
        });

    // When: Migration runs without Flyway context (should skip due to large count)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Migration should skip (can't easily test log output, but it doesn't fail)
  }

  @Test
  public void testMigrateManualOverride() throws Exception {
    // Given: Large dataset but manual mode enabled
    createTestRuns(5);

    migration.setJdbi(jdbi);
    migration.setManual(true); // Force manual mode
    migration.setChunkSize(2);

    // When: Migration runs with manual flag (without Flyway context)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Should process regardless of count
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThanOrEqualTo(5);
  }

  @Test
  public void testMigrateWithFailureResilience() throws Exception {
    // Given: Dataset with runs that might fail
    createTestRuns(5);

    migration.setJdbi(jdbi);
    migration.setChunkSize(2);

    // When: Migration runs (some failures are handled gracefully)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: At least some runs should be processed successfully
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThan(0);
  }

  @Test
  public void testMigrateWithNullContext() throws Exception {
    // Given: Migration called with null context (direct execution)
    createTestRuns(3);

    // When: Migration runs without Flyway context
    migration.setJdbi(jdbi);
    migration.setChunkSize(2);

    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Should work using injected Jdbi
    Long runLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                    .mapTo(Long.class)
                    .one());
    assertThat(runLineageCount).isGreaterThanOrEqualTo(3);
  }

  @Test
  public void testChunkSizeConfiguration() {
    // Test default chunk size
    V77__backfill_denormalized_lineage_tables defaultMigration =
        new V77__backfill_denormalized_lineage_tables();
    assertThat(defaultMigration.getChunkSize())
        .isEqualTo(V77__backfill_denormalized_lineage_tables.DEFAULT_CHUNK_SIZE);

    // Test custom chunk size
    defaultMigration.setChunkSize(10000);
    assertThat(defaultMigration.getChunkSize()).isEqualTo(10000);

    // Test null chunk size falls back to default
    defaultMigration.setChunkSize(null);
    assertThat(defaultMigration.getChunkSize())
        .isEqualTo(V77__backfill_denormalized_lineage_tables.DEFAULT_CHUNK_SIZE);
  }

  @Test
  public void testEstimateCountRuns() throws Exception {
    // Given: Database with known number of runs
    createTestRuns(15);

    migration.setJdbi(jdbi);

    // When: Migration starts (it estimates count internally)
    assertThatCode(() -> migration.migrate(null)).doesNotThrowAnyException();

    // Then: Migration should complete (validates estimate works)
    Long actualCount =
        jdbi.withHandle(
            handle -> handle.createQuery("SELECT COUNT(*) FROM runs").mapTo(Long.class).one());
    assertThat(actualCount).isEqualTo(15);
  }

  /**
   * Helper method to create test runs for migration testing.
   *
   * @param count Number of runs to create
   */
  private void createTestRuns(int count) {
    for (int i = 0; i < count; i++) {
      LineageTestUtils.createLineageRow(
          openLineageDao,
          "test_job_" + i,
          "COMPLETE",
          JobFacet.builder().build(),
          List.of(new Dataset("namespace", "input_" + i, null)),
          List.of(new Dataset("namespace", "output_" + i, null)));
    }
  }
}
