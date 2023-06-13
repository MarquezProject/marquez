/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;

/** */
@Slf4j
public final class DbRetention {
  private DbRetention() {}

  /* Default retention days. */
  public static final int DEFAULT_RETENTION_DAYS = 7;

  /* Default number of rows deleted per batch. */
  public static final int DEFAULT_NUMBER_OF_ROWS_PER_BATCH = 1000;

  /** ... */
  public static void retentionOnDbOrError(
      @NonNull Jdbi jdbi, final int numberOfRowsPerBatch, final int retentionDays)
      throws DbRetentionException {
    // (1) ...
    retentionOnDatasets(jdbi, numberOfRowsPerBatch, retentionDays);
    // (2) ...
    retentionOnJobs(jdbi, numberOfRowsPerBatch, retentionDays);
    // (3) ...
    retentionOnLineageEvents(jdbi, numberOfRowsPerBatch, retentionDays);
  }

  /** ... */
  private static void retentionOnDatasets(
      @NonNull Jdbi jdbi, final int numberOfRowsPerBatch, final int retentionDays) {
    log.info("Applying retention policy of '{}' days to dataset metadata...", retentionDays);
    // (1) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                sql(
                    """
                    DO $$
                    DECLARE
                      number_of_rows_per_batch INT := ${numberOfRowsPerBatch};
                      rows_deleted INT;
                    BEGIN
                      CREATE TEMPORARY TABLE used_datasets_as_input_in_x_days AS (
                        SELECT dataset_uuid
                          FROM job_versions_io_mapping AS jvio INNER JOIN job_versions AS jv
                            ON jvio.job_version_uuid = jv.uuid
                         WHERE jv.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                           AND jvio.io_type = 'INPUT'
                      );
                      LOOP
                        WITH deleted_rows AS (
                          DELETE FROM datasets AS d
                            WHERE d.uuid IN (
                              SELECT uuid
                                FROM datasets
                               WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                 FOR UPDATE SKIP LOCKED
                               LIMIT number_of_rows_per_batch
                            ) AND NOT EXISTS (
                                SELECT 1
                                  FROM used_datasets_as_input_in_x_days AS udai
                                 WHERE d.uuid = udai.dataset_uuid
                            ) RETURNING uuid
                        )
                        SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                        EXIT WHEN rows_deleted = 0;
                        PERFORM pg_sleep(0.1);
                      END LOOP;
                      DROP TABLE used_datasets_as_input_in_x_days;
                    END $$;
                    """,
                    numberOfRowsPerBatch,
                    retentionDays)));
    // (2) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                sql(
                    """
                    DO $$
                    DECLARE
                      number_of_rows_per_batch INT := ${numberOfRowsPerBatch};
                      rows_deleted INT;
                    BEGIN
                      CREATE TEMPORARY TABLE used_dataset_versions_as_input_in_x_days AS (
                        SELECT dataset_version_uuid
                          FROM runs_input_mapping AS ri INNER JOIN runs AS r
                            ON ri.run_uuid = r.uuid
                         WHERE r.created_at >= CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                      );
                      LOOP
                        WITH deleted_rows AS (
                          DELETE FROM dataset_versions AS dv
                           WHERE dv.uuid IN (
                             SELECT uuid
                               FROM dataset_versions
                              WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                FOR UPDATE SKIP LOCKED
                              LIMIT number_of_rows_per_batch
                           ) AND NOT EXISTS (
                               SELECT 1
                                 FROM used_dataset_versions_as_input_in_x_days AS uidv
                                WHERE dv.uuid = uidv.dataset_version_uuid
                           ) RETURNING uuid
                        )
                        SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                        EXIT WHEN rows_deleted = 0;
                        PERFORM pg_sleep(0.1);
                      END LOOP;
                      DROP TABLE used_dataset_versions_as_input_in_x_days;
                    END $$;""",
                    numberOfRowsPerBatch,
                    retentionDays)));
  }

  abstract class A {
    abstract void d();
  }

  abstract class B extends A {
    abstract void x();
  }

  /** ... */
  private static void retentionOnJobs(
      @NonNull Jdbi jdbi, final int numberOfRowsPerBatch, final int retentionDays) {
    log.info("Applying retention policy of '{}' days to job metadata...", retentionDays);
    // (1) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                sql(
                    """
                    DO $$
                    DECLARE
                      number_of_rows_per_batch INT := ${numberOfRowsPerBatch};
                      rows_deleted INT;
                    BEGIN
                      LOOP
                        WITH deleted_rows AS (
                          DELETE FROM jobs
                           WHERE uuid IN (
                             SELECT uuid
                               FROM jobs
                              WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                FOR UPDATE SKIP LOCKED
                              LIMIT number_of_rows_per_batch
                           ) RETURNING uuid
                        )
                        SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                        EXIT WHEN rows_deleted = 0;
                        PERFORM pg_sleep(0.1);
                      END LOOP;
                    END $$;""",
                    numberOfRowsPerBatch,
                    retentionDays)));
    // (2) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                sql(
                    """
                    DO $$
                    DECLARE
                      number_of_rows_per_batch INT := ${numberOfRowsPerBatch};
                      rows_deleted INT;
                    BEGIN
                      LOOP
                        WITH deleted_rows AS (
                          DELETE FROM job_versions
                           WHERE uuid IN (
                             SELECT uuid
                               FROM job_versions
                              WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                FOR UPDATE SKIP LOCKED
                              LIMIT number_of_rows_per_batch
                           ) RETURNING uuid
                        )
                        SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                        EXIT WHEN rows_deleted = 0;
                        PERFORM pg_sleep(0.1);
                      END LOOP;
                    END $$;""",
                    numberOfRowsPerBatch,
                    retentionDays)));
  }

  private static void retentionOnLineageEvents(
      @NonNull Jdbi jdbi, final int numberOfRowsPerBatch, final int retentionDays) {
    log.info("Applying retention policy of '{}' days to lineage events...", retentionDays);
    // (1) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                sql(
                    """
                    DO $$
                    DECLARE
                      number_of_rows_per_batch INT := ${numberOfRowsPerBatch};
                      rows_deleted INT;
                    BEGIN
                      LOOP
                        WITH deleted_rows AS (
                          DELETE FROM lineage_events
                           WHERE run_uuid IN (
                             SELECT run_uuid
                               FROM lineage_events
                              WHERE event_time < CURRENT_TIMESTAMP - INTERVAL '${retentionDays} days'
                                FOR UPDATE SKIP LOCKED
                              LIMIT number_of_rows_per_batch
                           ) RETURNING run_uuid
                        )
                        SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                        EXIT WHEN rows_deleted = 0;
                        PERFORM pg_sleep(0.1);
                      END LOOP;
                    END $$;""",
                    numberOfRowsPerBatch,
                    retentionDays)));
  }

  /** Return {@code sql} for the provided {@code numberOfRowsPerBatch} and {@code retentionDays}. */
  private static String sql(
      @NonNull final String sqlTemplate, final int numberOfRowsPerBatch, final int retentionDays) {
    return sqlTemplate
        .replace("${numberOfRowsPerBatch}", String.valueOf(numberOfRowsPerBatch))
        .replace("${retentionDays}", String.valueOf(retentionDays));
  }
}
