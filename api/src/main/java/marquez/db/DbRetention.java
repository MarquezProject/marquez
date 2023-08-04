/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.google.common.base.Stopwatch;
import java.sql.Types;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.OutParameters;
import org.jdbi.v3.core.statement.Script;

/**
 * Apply retention policy directly to source, dataset, and job metadata collected by Marquez. When
 * invoking {@link DbRetention#retentionOnDbOrError(Jdbi, int, int)}, retention is applied by
 * invoking the following methods in an order of precedence from first-to-last:
 *
 * <ul>
 *   <li>{@code retentionOnJobs()}
 *   <li>{@code retentionOnJobVersions()}
 *   <li>{@code retentionOnRuns()}
 *   <li>{@code retentionOnDatasets()}
 *   <li>{@code retentionOnDatasetVersions()}
 *   <li>{@code retentionOnLineageEvents()}
 * </ul>
 *
 * <p>Applying retention is not reversible, but can be applied many times. For this to perform well,
 * we delete rows in batches; this divides the deletion process into smaller chunks; the number of
 * rows to delete per batch is configurable. You may also apply retention as a dry run by invoking
 * {@link DbRetention#retentionOnDbOrError(Jdbi, int, int, boolean)}. By default, dry runs are
 * disable.
 *
 * <p>When retention is configured, the following operations will be applied:
 *
 * <ul>
 *   <li>Delete jobs from {@code jobs} table if {@code jobs.updated_at} older than retention days.
 *   <li>Delete job versions from {@code job_versions} table if {@code job_versions.updated_at}
 *       older than retention days; a job version will not be deleted if the job version is the
 *       {@code current} version of a given job.
 *   <li>Delete runs from {@code runs} table if {@code uns.updated_at} older than retention days; a
 *       run will not be deleted if the run is the {@code current} run of a given job version.
 *   <li>Delete dataset from datasets table if {@code datasets.updated_at} older than retention
 *       days; a dataset will not be deleted if the dataset is an input / output of a given job
 *       version.
 *   <li>Delete dataset versions from {@code dataset_versions} table if {@code
 *       dataset_versions.created_at} older than retention days; a dataset version will not be
 *       deleted if the dataset version is the {@code current} version of a given dataset version,
 *       or the input of a run.
 *   <li>Delete lineage events from {@code lineage_events} table if {@code
 *       lineage_events.event_time} older than retentionDays.
 * </ul>
 */
@Slf4j
public final class DbRetention {
  private DbRetention() {}

  /* Default retention days. */
  public static final int DEFAULT_RETENTION_DAYS = 7;

  /* Default number of rows deleted per batch. */
  public static final int DEFAULT_NUMBER_OF_ROWS_PER_BATCH = 1000;

  /* Disable retention dry run by default. */
  public static final boolean DEFAULT_DRY_RUN = false;

  /** Applies the retention policy to database. */
  public static void retentionOnDbOrError(
      @NonNull final Jdbi jdbi, final int numberOfRowsPerBatch, final int retentionDays)
      throws DbRetentionException {
    retentionOnDbOrError(jdbi, numberOfRowsPerBatch, retentionDays, DEFAULT_DRY_RUN);
  }

  /** Applies the retention policy to database; optionally as a dry run if specified. */
  public static void retentionOnDbOrError(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun)
      throws DbRetentionException {
    if (dryRun) {
      // On a dry run, add function(s) to return estimate of rows deleted (if not present).
      jdbi.useHandle(
          handle ->
              handle.execute(CREATE_OR_REPLACE_FUNCTION_ESTIMATE_NUMBER_OF_ROWS_OLDER_THAN_X_DAYS));
    }
    // Apply retention policy jobs, job versions, runs, datasets, and dataset versions.
    retentionOnJobs(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
    retentionOnJobVersions(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
    retentionOnRuns(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
    retentionOnDatasets(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
    retentionOnDatasetVersions(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);

    // Finally, apply retention policy to lineage events.
    retentionOnLineageEvents(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
  }

  /** Apply retention policy on {@code jobs}. */
  private static void retentionOnJobs(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_JOBS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' jobs",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      return;
    }
    log.info("Applying retention policy of '{}' days to jobs...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_jobs_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM jobs
                              WHERE uuid IN (
                                SELECT uuid
                                  FROM jobs
                                 WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                   FOR UPDATE SKIP LOCKED
                                 LIMIT rows_per_batch
                              ) RETURNING uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_jobs_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info("Deleted '{}' jobs in '{}' ms!", rowsDeleted, rowsDeleteTime.elapsed().toMillis());
  }

  /** Apply retention policy on {@code job versions}. */
  private static void retentionOnJobVersions(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_JOB_VERSIONS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' job versions",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      return;
    }
    log.info("Applying retention policy of '{}' days to job versions...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_job_versions_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        CREATE TEMPORARY TABLE used_job_versions_as_current_in_x_days AS (
                          SELECT current_version_uuid
                            FROM jobs
                           WHERE updated_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                        );
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM job_versions AS jv
                              WHERE uuid IN (
                                SELECT uuid
                                  FROM job_versions
                                 WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                   FOR UPDATE SKIP LOCKED
                                 LIMIT rows_per_batch
                              ) AND NOT EXISTS (
                                SELECT 1
                                  FROM used_job_versions_as_current_in_x_days AS ujvc
                                 WHERE jv.uuid = ujvc.current_version_uuid
                              ) RETURNING uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        DROP TABLE used_job_versions_as_current_in_x_days;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_job_versions_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info(
        "Deleted '{}' job versions in '{}' ms!", rowsDeleted, rowsDeleteTime.elapsed().toMillis());
  }

  /** Apply retention policy on {@code runs}. */
  private static void retentionOnRuns(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_RUNS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' runs",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      return;
    }
    log.info("Applying retention policy of '{}' days to runs...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_runs_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM runs
                              WHERE uuid IN (
                                SELECT uuid
                                  FROM runs
                                 WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                   FOR UPDATE SKIP LOCKED
                                 LIMIT rows_per_batch
                              ) RETURNING uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_runs_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info("Deleted '{}' runs in '{}' ms!", rowsDeleted, rowsDeleteTime.elapsed().toMillis());
  }

  /** Apply retention policy on {@code datasets}. */
  private static void retentionOnDatasets(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // On a dry run, add function(s) to return estimate of rows deleted.
      jdbi.useHandle(
          handle -> {
            try (final Script script =
                handle.createScript(sql(DRY_RUN_CREATE_TEMP_TABLES_FOR_DATASETS, retentionDays))) {
              script.execute();
            }
          });
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_DATASETS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' datasets",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      // Drop function(s) used to return estimate of rows deleted.
      jdbi.useHandle(
          handle -> {
            try (final Script script = handle.createScript(DRY_RUN_DROP_TEMP_TABLES_FOR_DATASETS)) {
              script.execute();
            }
          });
      return;
    }
    log.info("Applying retention policy of '{}' days to datasets...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_datasets_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        CREATE TEMPORARY TABLE used_datasets_as_io_in_x_days AS (
                          SELECT dataset_uuid
                            FROM job_versions_io_mapping AS jvio INNER JOIN job_versions AS jv
                              ON jvio.job_version_uuid = jv.uuid
                           WHERE jv.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                        );
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM datasets AS d
                              WHERE d.uuid IN (
                                SELECT uuid
                                  FROM datasets
                                 WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                   FOR UPDATE SKIP LOCKED
                                 LIMIT rows_per_batch
                              ) AND NOT EXISTS (
                                  SELECT 1
                                    FROM used_datasets_as_io_in_x_days AS udaio
                                   WHERE d.uuid = udaio.dataset_uuid
                              ) RETURNING uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        DROP TABLE used_datasets_as_io_in_x_days;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_datasets_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info("Deleted '{}' datasets in '{}' ms!", rowsDeleted, rowsDeleteTime.elapsed().toMillis());
  }

  /** Apply retention policy on {@code dataset versions}. */
  private static void retentionOnDatasetVersions(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // On a dry run, add function(s) to return estimate of rows deleted.
      jdbi.useHandle(
          handle -> {
            try (final Script script =
                handle.createScript(
                    sql(DRY_RUN_CREATE_TEMP_TABLES_FOR_DATASET_VERSIONS, retentionDays))) {
              script.execute();
            }
          });
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_DATASET_VERSIONS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' dataset versions",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      // Drop function(s) used to return estimate of rows deleted.
      jdbi.useHandle(
          handle -> {
            try (final Script script =
                handle.createScript(DRY_RUN_DROP_TEMP_TABLES_FOR_DATASET_VERSIONS)) {
              script.execute();
            }
          });
      return;
    }
    log.info("Applying retention policy of '{}' days to dataset versions...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_dataset_versions_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        CREATE TEMPORARY TABLE used_dataset_versions_as_input_in_x_days AS (
                          SELECT dataset_version_uuid
                            FROM runs_input_mapping AS ri INNER JOIN runs AS r
                              ON ri.run_uuid = r.uuid
                           WHERE r.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                        );
                        CREATE TEMPORARY TABLE used_dataset_versions_as_current_in_x_days AS (
                          SELECT current_version_uuid
                            FROM datasets
                           WHERE updated_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                        );
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM dataset_versions AS dv
                              WHERE dv.uuid IN (
                                SELECT uuid
                                  FROM dataset_versions
                                 WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                   FOR UPDATE SKIP LOCKED
                                 LIMIT rows_per_batch
                              ) AND NOT EXISTS (
                                SELECT 1
                                  FROM used_dataset_versions_as_input_in_x_days AS udvi
                                 WHERE dv.uuid = udvi.dataset_version_uuid
                              ) AND NOT EXISTS (
                                SELECT 1
                                  FROM used_dataset_versions_as_current_in_x_days AS udvc
                                 WHERE dv.uuid = udvc.current_version_uuid
                              ) RETURNING uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        DROP TABLE used_dataset_versions_as_input_in_x_days;
                        DROP TABLE used_dataset_versions_as_current_in_x_days;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_dataset_versions_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info(
        "Deleted '{}' dataset versions in '{}' ms!",
        rowsDeleted,
        rowsDeleteTime.elapsed().toMillis());
  }

  private static void retentionOnLineageEvents(
      @NonNull final Jdbi jdbi,
      final int numberOfRowsPerBatch,
      final int retentionDays,
      final boolean dryRun) {
    if (dryRun) {
      // Get estimate of rows older than X days, then log to console.
      final int rowsOlderThanXDaysEstimated =
          estimateOfRowsOlderThanXDays(
              jdbi, sql(DRY_RUN_DELETE_FROM_LINEAGE_EVENTS_OLDER_THAN_X_DAYS, retentionDays));
      log.info(
          "A retention policy of '{}' days will delete (estimated): '{}' lineage events",
          retentionDays,
          rowsOlderThanXDaysEstimated);
      return;
    }
    log.info("Applying retention policy of '{}' days to lineage events...", retentionDays);
    final Stopwatch rowsDeleteTime = Stopwatch.createStarted();
    final int rowsDeleted =
        jdbi.withHandle(
            handle -> {
              handle.execute(
                  sql(
                      """
                      CREATE OR REPLACE FUNCTION delete_lineage_events_older_than_x_days()
                        RETURNS INT AS $$
                      DECLARE
                        rows_per_batch INT := ${numberOfRowsPerBatch};
                        rows_deleted INT;
                        rows_deleted_total INT := 0;
                      BEGIN
                        LOOP
                          WITH deleted_rows AS (
                            DELETE FROM lineage_events
                              WHERE run_uuid IN (
                               SELECT run_uuid
                                 FROM lineage_events
                                WHERE event_time < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                  FOR UPDATE SKIP LOCKED
                                LIMIT rows_per_batch
                              ) RETURNING run_uuid
                          )
                          SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                          rows_deleted_total := rows_deleted_total + rows_deleted;
                          EXIT WHEN rows_deleted = 0;
                          PERFORM pg_sleep(0.1);
                        END LOOP;
                        RETURN rows_deleted_total;
                      END;
                      $$ LANGUAGE plpgsql;""",
                      numberOfRowsPerBatch,
                      retentionDays));
              return callWith(handle, "delete_lineage_events_older_than_x_days()");
            });
    rowsDeleteTime.stop();
    log.info(
        "Deleted '{}' lineage events in '{}' ms!",
        rowsDeleted,
        rowsDeleteTime.elapsed().toMillis());
  }

  /**
   * Returns generated {@code sql} using the {@code sqlTemplate} and the provided values for {@code
   * numberOfRowsPerBatch} and {@code retentionDays}.
   */
  private static String sql(
      @NonNull final String sqlTemplate, final int numberOfRowsPerBatch, final int retentionDays) {
    return checkNotBlank(sqlTemplate)
        .replace("${numberOfRowsPerBatch}", String.valueOf(numberOfRowsPerBatch))
        .replace("${retentionDays}", String.valueOf(retentionDays));
  }

  /**
   * Returns {@code sql} using the {@code sqlTemplate} and the provided value for {@code
   * retentionDays}.
   */
  private static String sql(@NonNull final String sqlTemplate, final int retentionDays) {
    return checkNotBlank(sqlTemplate).replace("${retentionDays}", String.valueOf(retentionDays));
  }

  /** Returns estimate of rows older than X days. */
  private static int estimateOfRowsOlderThanXDays(
      @NonNull final Jdbi jdbi, @NonNull final String retentionQuery) {
    return jdbi.withHandle(
        handle -> {
          final OutParameters result =
              handle
                  .createCall(
                      "{:estimate = call estimate_number_of_rows_older_than_x_days(:retentionQuery)}")
                  .bind("retentionQuery", retentionQuery)
                  .registerOutParameter("estimate", Types.INTEGER)
                  .invoke();
          return result.getInt("estimate");
        });
  }

  /** Call function with the specified {@code handle}. */
  private static int callWith(@NonNull final Handle handle, @NonNull String functionName) {
    final OutParameters result =
        handle
            .createCall(
                """
                {:rows_deleted_total = call ${functionName}}
                """
                    .replace("${functionName}", functionName))
            .registerOutParameter("rows_deleted_total", Types.INTEGER)
            .invoke();
    return result.getInt("rows_deleted_total");
  }

  /** Create {@code estimate_number_of_rows_older_than_x_days()}. */
  private static final String CREATE_OR_REPLACE_FUNCTION_ESTIMATE_NUMBER_OF_ROWS_OLDER_THAN_X_DAYS =
      """
      CREATE OR REPLACE FUNCTION estimate_number_of_rows_older_than_x_days(retention_query TEXT)
        RETURNS INT AS $$
      DECLARE
        query_plan_as_json JSON;
      BEGIN
        EXECUTE 'EXPLAIN (FORMAT JSON) ' || retention_query INTO query_plan_as_json;
        RETURN (query_plan_as_json -> 0 -> 'Plan' ->> 'Plan Rows')::INT;
      END;
      $$ LANGUAGE plpgsql;
      """;

  private static final String DRY_RUN_DELETE_FROM_JOBS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM jobs
        WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      """;

  private static final String DRY_RUN_DELETE_FROM_JOB_VERSIONS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM job_versions
        WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      """;

  private static final String DRY_RUN_DELETE_FROM_RUNS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM runs
        WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      """;

  /** Create {@code used_datasets_as_input_in_x_days()}. */
  private static final String DRY_RUN_CREATE_TEMP_TABLES_FOR_DATASETS =
      """
      CREATE TEMPORARY TABLE used_datasets_as_input_in_x_days AS (
        SELECT dataset_uuid
          FROM job_versions_io_mapping AS jvio INNER JOIN job_versions AS jv
            ON jvio.job_version_uuid = jv.uuid
         WHERE jv.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
           AND jvio.io_type = 'INPUT'
      );
      """;

  private static final String DRY_RUN_DELETE_FROM_DATASETS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM datasets AS d
        WHERE d.uuid IN (
          SELECT uuid
            FROM datasets
           WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
        ) AND NOT EXISTS (
          SELECT 1
            FROM used_datasets_as_input_in_x_days AS udi
           WHERE d.uuid = udi.dataset_uuid
        )
      """;

  /** Drop {@code used_datasets_as_input_in_x_days()} */
  private static final String DRY_RUN_DROP_TEMP_TABLES_FOR_DATASETS =
      """
      DROP TABLE used_datasets_as_input_in_x_days;
      """;

  /**
   * Create {@code used_dataset_versions_as_input_in_x_days()} and {@code
   * used_dataset_versions_as_current_in_x_days()}.
   */
  private static final String DRY_RUN_CREATE_TEMP_TABLES_FOR_DATASET_VERSIONS =
      """
      CREATE TEMPORARY TABLE used_dataset_versions_as_input_in_x_days AS (
        SELECT dataset_version_uuid
          FROM runs_input_mapping AS ri INNER JOIN runs AS r
            ON ri.run_uuid = r.uuid
         WHERE r.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      );
      CREATE TEMPORARY TABLE used_dataset_versions_as_current_in_x_days AS (
        SELECT current_version_uuid
          FROM datasets
         WHERE updated_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      );
      """;

  private static final String DRY_RUN_DELETE_FROM_DATASET_VERSIONS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM dataset_versions AS dv
        WHERE dv.uuid IN (
          SELECT uuid
            FROM dataset_versions
           WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
        ) AND NOT EXISTS (
          SELECT 1
            FROM used_dataset_versions_as_input_in_x_days AS udvi
           WHERE dv.uuid = udvi.dataset_version_uuid
        ) OR NOT EXISTS (
          SELECT 1
            FROM used_dataset_versions_as_current_in_x_days AS udvc
           WHERE dv.uuid = udvc.current_version_uuid
        ) RETURNING uuid
      """;

  /**
   * Drop {@code used_dataset_versions_as_input_in_x_days()} and {@code
   * used_dataset_versions_as_current_in_x_days()}.
   */
  private static final String DRY_RUN_DROP_TEMP_TABLES_FOR_DATASET_VERSIONS =
      """
      DROP TABLE used_dataset_versions_as_input_in_x_days;
      DROP TABLE used_dataset_versions_as_current_in_x_days;
      """;

  private static final String DRY_RUN_DELETE_FROM_LINEAGE_EVENTS_OLDER_THAN_X_DAYS =
      """
      DELETE FROM lineage_events
        WHERE event_time < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
      """;
}
