/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import marquez.db.Columns;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class V57_1__BackfillFacets implements JavaMigration {

  public static int DEFAULT_CHUNK_SIZE = 10000;

  private static int BASIC_MIGRATION_LIMIT = 100000;

  private static final String GET_CURRENT_LOCK_SQL =
      """
        SELECT * FROM facet_migration_lock
        ORDER BY created_at ASC, run_uuid ASC
        LIMIT 1
      """;

  private static final String GET_FINISHING_LOCK_SQL =
      """
         SELECT run_uuid, created_at FROM lineage_events
         ORDER BY
             COALESCE(created_at, event_time) ASC,
             run_uuid ASC
         LIMIT 1
      """;

  private static final String GET_INITIAL_LOCK_SQL =
      """
          SELECT
              run_uuid,
              COALESCE(created_at, event_time, NOW()) + INTERVAL '1 MILLISECONDS' as created_at
          FROM lineage_events ORDER BY COALESCE(created_at, event_time) DESC, run_uuid DESC LIMIT 1
      """;

  private static final String COUNT_LINEAGE_EVENTS_SQL =
      """
          SELECT count(*) as cnt FROM lineage_events
      """;

  private static final String COUNT_LINEAGE_EVENTS_TO_PROCESS_SQL =
      """
          SELECT count(*) as cnt FROM lineage_events e
          WHERE
              COALESCE(e.created_at, e.event_time) < :createdAt
              OR (COALESCE(e.created_at, e.event_time) = :createdAt AND e.run_uuid < :runUuid)
      """;

  private String getBackFillFacetsSQL() {
    return String.format(
        """
        WITH events_chunk AS (
            SELECT e.* FROM lineage_events e
            WHERE
                COALESCE(e.created_at, e.event_time) < :createdAt
                OR (COALESCE(e.created_at, e.event_time) = :createdAt AND e.run_uuid < :runUuid)
            ORDER BY COALESCE(e.created_at, e.event_time) DESC, e.run_uuid DESC
            LIMIT :chunkSize
        ),
        insert_datasets AS (
            INSERT INTO dataset_facets %s
        ),
        insert_runs AS (
            INSERT INTO run_facets %s
        ),
        insert_jobs AS (
            INSERT INTO job_facets %s
        )
        INSERT INTO facet_migration_lock
        SELECT events_chunk.created_at, events_chunk.run_uuid
        FROM events_chunk
        ORDER BY
            COALESCE(events_chunk.created_at, events_chunk.event_time) ASC,
            events_chunk.run_uuid ASC
        LIMIT 1
        RETURNING created_at, run_uuid;
        """,
        V56_1__FacetViews.getDatasetFacetsDefinitionSQL("events_chunk"),
        V56_1__FacetViews.getRunFacetsDefinitionSQL("events_chunk"),
        V56_1__FacetViews.getJobFacetsDefinitionSQL("events_chunk"));
  }

  @Setter private Integer chunkSize = null;

  @Setter private boolean manual = false;

  @Setter private Jdbi jdbi;

  public int getChunkSize() {
    return chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE;
  }

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("57.2");
  }

  @Override
  public String getDescription() {
    return "BackFillFacets";
  }

  @Override
  public Integer getChecksum() {
    return null;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public boolean isBaselineMigration() {
    return false;
  }

  @Override
  public boolean canExecuteInTransaction() {
    return false;
  }

  @Override
  public void migrate(Context context) throws Exception {
    if (context != null) {
      jdbi = Jdbi.create(context.getConnection());
    }

    if (getLock(GET_INITIAL_LOCK_SQL).isEmpty()) {
      // lineage_events table is empty -> no need to run migration
      // anyway. we need to create lock to mark that no data requires migration
      execute("INSERT INTO facet_migration_lock VALUES (NOW(), null)");

      createTargetViews();
      return;
    }
    Optional<MigrationLock> lastExpectedLock = getLock(GET_FINISHING_LOCK_SQL);

    if (!manual && countLineageEvents() >= BASIC_MIGRATION_LIMIT) {
      log.warn(
          """
              ==================================================
              ==================================================
              ==================================================
              MARQUEZ INSTANCE TOO BIG TO RUN AUTO UPGRADE.
              YOU NEED TO RUN v55_migrate COMMAND MANUALLY.
              FOR MORE DETAILS, PLEASE REFER TO:
              https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V57__readme.md
              ==================================================
              ==================================================
              ==================================================
              """);
      // We end migration successfully although no data has been migrated to facet tables
      return;
    }

    log.info("Configured chunkSize is {}", getChunkSize());
    MigrationLock lock = getLock(GET_CURRENT_LOCK_SQL).orElse(getLock(GET_INITIAL_LOCK_SQL).get());
    while (!lock.equals(lastExpectedLock.get())) {
      lock = backFillChunk(lock);
      log.info(
          "Migrating chunk finished. Still having {} records to migrate.",
          countLineageEventsToProcess(lock));
    }

    createTargetViews();
    log.info("All records migrated");
  }

  private void createTargetViews() {
    // replace facet views with tables
    execute("DROP VIEW IF EXISTS run_facets_view");
    execute("DROP VIEW IF EXISTS job_facets_view");
    execute("DROP VIEW IF EXISTS dataset_facets_view");
    execute("CREATE OR REPLACE VIEW run_facets_view AS SELECT * FROM run_facets");
    execute("CREATE OR REPLACE VIEW job_facets_view AS SELECT * FROM job_facets");
    execute("CREATE OR REPLACE VIEW dataset_facets_view AS SELECT * FROM dataset_facets");
  }

  private void execute(String sql) {
    jdbi.inTransaction(handle -> handle.execute(sql));
  }

  private MigrationLock backFillChunk(MigrationLock lock) {
    String backFillQuery = getBackFillFacetsSQL();
    return jdbi.withHandle(
        h ->
            h.createQuery(backFillQuery)
                .bind("chunkSize", getChunkSize())
                .bind("createdAt", lock.created_at)
                .bind("runUuid", lock.run_uuid)
                .map(
                    rs ->
                        new MigrationLock(
                            rs.getColumn(Columns.RUN_UUID, UUID.class),
                            rs.getColumn(Columns.CREATED_AT, Instant.class)))
                .one());
  }

  private Optional<MigrationLock> getLock(String sql) {
    return jdbi.withHandle(
        h ->
            h.createQuery(sql)
                .map(
                    rs ->
                        new MigrationLock(
                            rs.getColumn(Columns.RUN_UUID, UUID.class),
                            rs.getColumn(Columns.CREATED_AT, Instant.class)))
                .findFirst());
  }

  private int countLineageEvents() {
    return jdbi.withHandle(
        h ->
            h.createQuery(COUNT_LINEAGE_EVENTS_SQL)
                .map(rs -> rs.getColumn("cnt", Integer.class))
                .one());
  }

  private int countLineageEventsToProcess(MigrationLock lock) {
    return jdbi.withHandle(
        h ->
            h.createQuery(COUNT_LINEAGE_EVENTS_TO_PROCESS_SQL)
                .bind("createdAt", lock.created_at)
                .bind("runUuid", lock.run_uuid)
                .map(rs -> rs.getColumn("cnt", Integer.class))
                .one());
  }

  private record MigrationLock(UUID run_uuid, Instant created_at) {}
}
