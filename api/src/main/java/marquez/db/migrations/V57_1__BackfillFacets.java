/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class V57_1__BackfillFacets implements JavaMigration {

  public static int DEFAULT_CHUNK_SIZE = 10000;

  private static int BASIC_MIGRATION_LIMIT = 100000;

  private static final String CREATE_TEMP_EVENT_RUNS_TABLE =
      """
      CREATE TEMP TABLE lineage_event_runs AS
      SELECT DISTINCT ON (run_uuid) run_uuid,
            COALESCE(created_at, event_time) AS created_at
      FROM lineage_events
    """;

  private static final String CREATE_INDEX_EVENT_RUNS_TABLE =
      """
      CREATE INDEX ON lineage_event_runs (created_at DESC) INCLUDE (run_uuid)
    """;

  private static final String COUNT_LINEAGE_EVENTS_SQL =
      """
        SELECT COUNT(*) FROM lineage_events;
    """;

  private static final String ESTIMATE_COUNT_LINEAGE_EVENTS_SQL =
      """
          SELECT reltuples AS cnt FROM pg_class WHERE relname = 'lineage_events';
      """;

  private String getBackFillFacetsSQL() {
    return String.format(
        """
          WITH queued_runs AS (
              SELECT created_at, run_uuid
              FROM lineage_event_runs
              ORDER BY created_at DESC, run_uuid
              LIMIT :chunkSize
          ),
          processed_runs AS (
            DELETE FROM lineage_event_runs
            USING queued_runs qe
            WHERE lineage_event_runs.run_uuid=qe.run_uuid
            RETURNING lineage_event_runs.run_uuid
          ),
          events_chunk AS (
            SELECT e.*
            FROM lineage_events e
            WHERE run_uuid IN (SELECT run_uuid FROM processed_runs)
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

    int estimatedEventsCount = estimateCountLineageEvents();

    if (estimatedEventsCount < 0) {
      // https://www.postgresql.org/docs/current/catalog-pg-class.html
      // -1 indicating that the row count is unknown.
      // This happens when lineage_events table is empty.
      log.info("Vacuuming lineage_events table");
      jdbi.withHandle(h -> h.execute("VACUUM lineage_events;"));
      log.info("Vacuuming lineage_events table finished");

      estimatedEventsCount = estimateCountLineageEvents();
    }

    log.info("Estimating {} events in lineage_events table", estimatedEventsCount);
    if (estimatedEventsCount == 0 && countLineageEvents() == 0) {
      // lineage_events table is empty -> no need to run migration
      // anyway. we need to create lock to mark that no data requires migration
      execute("INSERT INTO facet_migration_lock VALUES (NOW(), null)");

      createTargetViews();
      return;
    }
    if (!manual && estimatedEventsCount >= BASIC_MIGRATION_LIMIT) {
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

    jdbi.withHandle(
        h -> {
          h.createUpdate(CREATE_TEMP_EVENT_RUNS_TABLE).execute();
          h.createUpdate(CREATE_INDEX_EVENT_RUNS_TABLE).execute();
          log.info("Configured chunkSize is {}", getChunkSize());
          boolean doMigration = true;
          while (doMigration) {
            int results = backFillChunk(h);
            log.info("Migrating chunk finished processing {} records.", results);
            if (results < 1) {
              doMigration = false;
            }
          }
          return null;
        });

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

  private int backFillChunk(Handle h) {
    String backFillQuery = getBackFillFacetsSQL();
    return h.createUpdate(backFillQuery).bind("chunkSize", getChunkSize()).execute();
  }

  private int estimateCountLineageEvents() {
    return jdbi.withHandle(
        h -> h.createQuery(ESTIMATE_COUNT_LINEAGE_EVENTS_SQL).mapTo(Integer.class).one());
  }

  private int countLineageEvents() {
    return jdbi.withHandle(h -> h.createQuery(COUNT_LINEAGE_EVENTS_SQL).mapTo(Integer.class).one());
  }
}
