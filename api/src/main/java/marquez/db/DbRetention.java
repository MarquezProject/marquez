/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementException;

@Slf4j
public final class DbRetention {
  private DbRetention() {}

  /* Default retention days. */
  public static final int DEFAULT_RETENTION_DAYS = 7;

  /* Default chunk size. */
  public static final int DEFAULT_CHUNK_SIZE = 1000;

  /** ... */
  public static void retentionOnDbOrError(
      @NonNull Jdbi jdbi, final int chunkSize, final int retentionDays)
      throws DbRetentionException {
    // (1) ...
    retentionOnDatasets(jdbi, chunkSize, retentionDays);
    // (2) ...
    retentionOnJobs(jdbi, chunkSize, retentionDays);
    // (3) ...
    retentionOnLineageEvents(jdbi, chunkSize, retentionDays);
  }

  /** ... */
  private static void retentionOnDatasets(
      @NonNull Jdbi jdbi, final int chunkSize, final int retentionDays)
      throws DbRetentionException {
    // (1) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                """
                DO $$
                DECLARE
                  chunk_size INT := %d;
                  rows_deleted INT;
                BEGIN
                  CREATE TEMPORARY TABLE used_input_datasets_in_x_days AS (
                    SELECT dataset_uuid
                      FROM job_versions_io_mapping AS jvio INNER JOIN job_versions AS jv
                        ON jvio.job_version_uuid = jv.uuid
                     WHERE jv.created_at >= CURRENT_TIMESTAMP - INTERVAL '%d days'
                       AND jvio.io_type = 'INPUT'
                  );
                  LOOP
                    WITH deleted_rows AS (
                      DELETE FROM datasets AS d
                        WHERE d.updated_at < CURRENT_TIMESTAMP - INTERVAL '%d days'
                          AND NOT EXISTS (
                            SELECT 1
                              FROM used_input_datasets_in_x_days AS uid
                             WHERE d.uuid = uid.dataset_uuid
                          )
                          RETURNING uuid
                    )
                    SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                    EXIT WHEN rows_deleted = 0;
                  END LOOP;
                  DROP TABLE used_input_datasets_in_x_days;
                END $$;
                """
                    .formatted(chunkSize, retentionDays, retentionDays)));
    // (2) ...
    jdbi.useHandle(
        handle ->
            handle.execute(
                """
                DO $$
                DECLARE
                  chunk_size INT := %d;
                  rows_deleted INT;
                BEGIN
                  CREATE TEMPORARY TABLE used_input_dataset_versions_in_x_days AS (
                    SELECT dataset_version_uuid
                      FROM runs_input_mapping AS ri INNER JOIN runs AS r
                        ON ri.run_uuid = r.uuid
                     WHERE r.created_at >= CURRENT_TIMESTAMP - INTERVAL '%d days'
                  );
                  LOOP
                    WITH deleted_rows AS (
                      DELETE FROM dataset_versions AS dv
                        WHERE dv.created_at < CURRENT_TIMESTAMP - INTERVAL '%d days'
                          AND NOT EXISTS (
                            SELECT 1
                              FROM used_input_dataset_versions_in_x_days AS uidv
                             WHERE dv.uuid = uidv.dataset_version_uuid
                          )
                          RETURNING uuid
                    )
                    SELECT COUNT(*) INTO rows_deleted FROM deleted_rows;
                    EXIT WHEN rows_deleted = 0;
                  END LOOP;
                  DROP TABLE used_input_dataset_versions_in_x_days;
                END $$;"""
                    .formatted(chunkSize, retentionDays, retentionDays)));
  }

  /** ... */
  private static void retentionOnJobs(
      @NonNull Jdbi jdbi, final int chunkSize, final int retentionDays)
      throws DbRetentionException {
    final int totalNumOfJobRowsDeleted =
        executeDbRetentionQuery(
            jdbi,
            """
            DELETE FROM jobs
              WHERE uuid IN (
                SELECT uuid FROM jobs
                 WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '%d days'
                 LIMIT %d
            ) RETURNING uuid;
            """
                .formatted(retentionDays, chunkSize));
    // ..
    if (totalNumOfJobRowsDeleted == 0) {
      final int totalNumOfVersionRowsDeleted =
          executeDbRetentionQuery(
              jdbi,
              """
              DELETE FROM job_versions
                WHERE uuid IN (
                  SELECT uuid FROM job_versions
                   WHERE updated_at < CURRENT_TIMESTAMP - INTERVAL '%d days'
                   LIMIT %d
              ) RETURNING uuid;
              """
                  .formatted(retentionDays, chunkSize));
    }
  }

  private static void retentionOnLineageEvents(
      @NonNull Jdbi jdbi, final int chunkSize, final int retentionDays)
      throws DbRetentionException {}

  /** Returns the number of {@code row}s deleted by the provided retention {@code query}. */
  private static int executeDbRetentionQuery(
      @NonNull Jdbi jdbi, @NonNull final String retentionQuery) throws DbRetentionException {
    try {
      return jdbi.withHandle(
          handle -> {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            int totalNumOfRowsDeleted = 0; // Keep count of rows deleted.
            boolean hasMoreRows = true;
            while (hasMoreRows) {
              // Apply batch deletes (for improved performance) on rows in table with a creation
              // time > date allowed in retention query.
              final List<UUID> rowsDeleted =
                  handle.createQuery(retentionQuery).mapTo(UUID.class).stream().toList();
              final int numOfRowsDeleted = rowsDeleted.size();
              totalNumOfRowsDeleted += numOfRowsDeleted;
              hasMoreRows = !rowsDeleted.isEmpty();
            }
            // Measure the time elapsed to execute retention query, then return number of rows
            // deleted.
            stopwatch.stop();
            log.debug(
                "Deleted '{}' rows in '{}' secs using retention policy: {}",
                totalNumOfRowsDeleted,
                stopwatch.elapsed().toSeconds(),
                retentionQuery);
            return totalNumOfRowsDeleted;
          });
    } catch (StatementException errorOnDbRetentionQuery) {
      // Propagate throwable up the stack.
      throw new DbRetentionException(
          "Failed to apply retention query: " + retentionQuery, errorOnDbRetentionQuery.getCause());
    }
  }
}
