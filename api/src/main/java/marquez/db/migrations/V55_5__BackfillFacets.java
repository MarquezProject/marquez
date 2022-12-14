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
public class V55_5__BackfillFacets implements JavaMigration {

  public static int DEFAULT_CHUNK_SIZE = 10000;

  private static int BASIC_MIGRATION_LIMIT = 100000;

  private static final String GET_CURRENT_LOCK_SQL =
      """
        SELECT * FROM v55_facet_migration_lock
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

  private static final String DATASET_FACETS_DEFINITION =
      """
          WITH dataset_names as (
              SELECT
                  d.uuid,
                  array_agg(CAST((namespaces.name, symlinks.name) AS DATASET_NAME)) AS dataset_symlinks
              FROM datasets d
              JOIN dataset_symlinks symlinks ON d.uuid = symlinks.dataset_uuid
              INNER JOIN namespaces ON symlinks.namespace_uuid = namespaces.uuid
              WHERE d.is_hidden IS FALSE
              GROUP BY d.uuid
          ),
          lineage_datasets AS (
              SELECT
                  jsonb_array_elements(coalesce(le.event -> 'inputs', '[]'::jsonb) || coalesce(le.event -> 'outputs', '[]'::jsonb)) AS dataset,
                  le.*
              FROM %s le
          ),
          dataset_facets AS (
              SELECT
                  jsonb_object_keys as facet_name,
                  json_build_object(jsonb_object_keys, dataset -> 'facets' -> jsonb_object_keys)::jsonb as facet,
                  dataset ->> 'name' as dataset_name,
                  dataset ->> 'namespace' as dataset_namespace,
                  ld.*
              FROM lineage_datasets ld, jsonb_object_keys(coalesce(dataset -> 'facets', '{}'::jsonb))
          )
          SELECT
              uuid_generate_v4() AS uuid,
              COALESCE(df.created_at, df.event_time) AS created_at,
              dn.uuid AS dataset_uuid,
              df.run_uuid AS run_uuid,
              df.event_time AS lineage_event_time,
              df.event_type::VARCHAR(64) AS lineage_event_type,
              (
                  CASE
                  WHEN lower(facet_name) IN ('documentation', 'schema', 'datasource', 'description', 'lifecyclestatechange', 'version', 'columnlineage', 'ownership') then 'DATASET'
                  WHEN lower(facet_name) IN ('dataqualitymetrics', 'dataqualityassertions') then 'INPUT'
                  WHEN lower(facet_name) = 'outputstatistics' then 'OUTPUT'
                  ELSE 'UNKNOWN'
                  END
              )::VARCHAR(64) AS type,
              df.facet_name::VARCHAR(255) AS name,
              df.facet AS facet
          FROM dataset_facets df
          JOIN dataset_names dn ON CAST((dataset_namespace, dataset_name) AS DATASET_NAME) = ANY(dn.dataset_symlinks)
      """;
  private static final String CREATE_DATASET_FACETS_VIEW =
      "CREATE OR REPLACE VIEW dataset_facets_view AS"
          + String.format(DATASET_FACETS_DEFINITION, "lineage_events");
  ;

  private static final String RUN_FACETS_DEFINITION =
      """
          SELECT
              uuid_generate_v4() AS uuid,
              COALESCE(le.created_at, le.event_time) AS created_at,
              le.run_uuid AS run_uuid,
              le.event_time AS lineage_event_time,
              le.event_type::VARCHAR(64) AS lineage_event_type,
              jsonb_object_keys::VARCHAR(255) as name,
              json_build_object(jsonb_object_keys, event -> 'run' -> 'facets' -> jsonb_object_keys)::jsonb as facet
          FROM %s le, jsonb_object_keys(coalesce(event -> 'run' -> 'facets', '{}'::jsonb))
          WHERE lower(jsonb_object_keys) != 'spark_unknown'
      """;
  private static final String CREATE_RUN_FACETS_VIEW =
      "CREATE OR REPLACE VIEW run_facets_view AS "
          + String.format(RUN_FACETS_DEFINITION, "lineage_events");

  private static final String JOB_FACETS_DEFINITION =
      """
           SELECT
              uuid_generate_v4() AS uuid,
              COALESCE(le.created_at, le.event_time) AS created_at,
              r.job_uuid AS job_uuid,
              le.run_uuid AS run_uuid,
              le.event_time AS lineage_event_time,
              le.event_type::VARCHAR(64) AS lineage_event_type,
              jsonb_object_keys::VARCHAR(255) as name,
              json_build_object(jsonb_object_keys, event -> 'job' -> 'facets' -> jsonb_object_keys)::jsonb as facet
          FROM %s le, runs r, jsonb_object_keys(coalesce(event -> 'job' -> 'facets', '{}'::jsonb))
          WHERE r.uuid = le.run_uuid
      """;
  private static final String CREATE_JOB_FACETS_VIEW =
      "CREATE OR REPLACE VIEW job_facets_view AS"
          + String.format(JOB_FACETS_DEFINITION, "lineage_events");

  private static final String BACK_FILL_FACETS_SQL =
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
              INSERT INTO dataset_facets
              """
          + String.format(DATASET_FACETS_DEFINITION, "events_chunk")
          + """
          ),
          insert_runs AS (
              INSERT INTO run_facets
              """
          + String.format(RUN_FACETS_DEFINITION, "events_chunk")
          + """
          ),
          insert_jobs AS (
              INSERT INTO job_facets
              """
          + String.format(JOB_FACETS_DEFINITION, "events_chunk")
          + """
          )
          INSERT INTO v55_facet_migration_lock
          SELECT events_chunk.created_at, events_chunk.run_uuid
          FROM events_chunk
          ORDER BY
              COALESCE(events_chunk.created_at, events_chunk.event_time) ASC,
              events_chunk.run_uuid ASC
          LIMIT 1
          RETURNING created_at, run_uuid;
      """;

  @Setter private Integer chunkSize = null;

  @Setter private boolean triggeredByCommand = false;

  @Setter private Jdbi jdbi;

  public int getChunkSize() {
    return chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE;
  }

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("55.5");
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

    /**
     * Workaround to register uuid_generate_v4 function to generate uuids. gen_random_uuid() is
     * available since Postgres 13
     */
    jdbi.withHandle(h -> h.createCall("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"").invoke());

    if (getLock(GET_INITIAL_LOCK_SQL).isEmpty()) {
      // lineage_events table is empty -> no need to run migration
      // anyway. we need to create lock to mark that no data requires migration
      execute("INSERT INTO v55_facet_migration_lock VALUES (NOW(), uuid_generate_v4())");

      createTargetViews();
      return;
    }
    Optional<MigrationLock> lastExpectedLock = getLock(GET_FINISHING_LOCK_SQL);

    // create facet views
    execute(CREATE_RUN_FACETS_VIEW);
    execute(CREATE_DATASET_FACETS_VIEW);
    execute(CREATE_JOB_FACETS_VIEW);

    if (!triggeredByCommand && countLineageEvents() >= BASIC_MIGRATION_LIMIT) {
      log.warn(
          """
              ==================================================
              ==================================================
              ==================================================
              MARQUEZ INSTANCE TOO BIG TO RUN AUTO UPGRADE.
              YOU NEED TO RUN v55_migrate COMMAND MANUALLY.
              FOR MORE DETAILS, PLEASE REFER TO:
              https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V55__readme.md
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
    return jdbi.withHandle(
        h ->
            h.createQuery(BACK_FILL_FACETS_SQL)
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
