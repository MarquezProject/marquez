/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementException;

@Slf4j
public final class DbRetention {
  private DbRetention() {}

  /* Bulk delete batch size. */
  private static final int BULK_DELETE_BATCH_SIZE = 1000;

  private static final String DATASETS = "datasets";
  private static final String JOBS = "jobs";
  private static final String LINEAGE_EVENTS = "lineage_events";

  /* Tables to apply retention policy on (with cascade delete constraint). */
  private static final Set<String> TABLES = Set.of(DATASETS, JOBS, LINEAGE_EVENTS);

  public static void retentionOnDbOrError(@NonNull Jdbi jdbi, final int retentionDays)
      throws DbRetentionException {
    for (final String table : TABLES) {
      log.info("Applying retention policy of '{}' days to '{}' table...", retentionDays, table);
      // Build, then execute retention policy.
      final int totalNumOfRowsDeleted =
          executeDbRetentionQuery(
              jdbi,
              buildQuery(
                  """
                  DELETE FROM %s
                    WHERE %s IN (
                      SELECT %s FROM %s
                       WHERE %s < CURRENT_TIMESTAMP - INTERVAL '%d days'
                       LIMIT %d
                   ) RETURNING %s;
                  """,
                  table, retentionDays));
      // Determine whether any rows met the retention policy.
      if (totalNumOfRowsDeleted == 0) {
        log.info("No rows older than '{}' days in '{}' table.", retentionDays, table);
        continue;
      }
      log.info("Successfully deleted '{}' rows from '{}' table.", totalNumOfRowsDeleted, table);
    }
  }

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
      log.error("Failed to apply retention query: {}", retentionQuery);
      // Propagate throwable up the stack.
      throw new DbRetentionException(errorOnDbRetentionQuery.getCause());
    }
  }

  /** Build retention {@code query} for the provided {@code table} and {@code retentionDays}. */
  private static String buildQuery(
      @NonNull String queryTemplate, @NonNull String table, final int retentionDays) {
    final String pk = pkInQueryFor(table);
    final String timestamp = timestampInQueryFor(table);
    return queryTemplate.formatted(
        table, pk, pk, table, timestamp, retentionDays, BULK_DELETE_BATCH_SIZE, pk);
  }

  /** Returns {@code pk} for the provided {@code table}. */
  private static String pkInQueryFor(@NonNull final String table) {
    return LINEAGE_EVENTS.equals(table) ? Columns.RUN_UUID : Columns.ROW_UUID;
  }

  /** Returns {@code timestamp} for the provided {@code table}. */
  private static String timestampInQueryFor(@NonNull final String table) {
    return LINEAGE_EVENTS.equals(table) ? Columns.EVENT_TIME : Columns.CREATED_AT;
  }
}
