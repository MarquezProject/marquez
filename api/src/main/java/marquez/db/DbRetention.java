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

/** ... */
@Slf4j
public final class DbRetention {
  private DbRetention() {}

  /* ... */
  private static final int DB_RETENTION_BATCH_SIZE = 1000;

  /* ... */
  private static final String DATASETS = "datasets";
  private static final String JOBS = "jobs";
  private static final String LINEAGE_EVENTS = "lineage_events";

  /* ... */
  private static final Set<String> DB_TABLES = Set.of(DATASETS, JOBS, LINEAGE_EVENTS);

  /** ... */
  public static void retentionOnDbOrError(@NonNull Jdbi jdbi, final int dbRetentionInDays)
      throws DbRetentionException {
    for (final String dbTable : DB_TABLES) {
      log.info(
          "Applying db retention policy of '{}' days to '{}' table...", dbRetentionInDays, dbTable);
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
                  dbTable, dbRetentionInDays));
      if (totalNumOfRowsDeleted == 0) {
        log.info("No rows older than '{}' days in '{}' table.", dbRetentionInDays, dbTable);
        continue;
      }
      log.info("Successfully deleted '{}' rows from '{}' table.", totalNumOfRowsDeleted, dbTable);
    }
  }

  /** ... */
  private static int executeDbRetentionQuery(
      @NonNull Jdbi jdbi, @NonNull final String dbRetentionQuery) throws DbRetentionException {
    try {
      return jdbi.withHandle(
          handle -> {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            // ...
            int totalNumOfRowsDeleted = 0;
            boolean hasMoreRows = true;
            while (hasMoreRows) {
              final List<UUID> rowsDeleted =
                  handle.createQuery(dbRetentionQuery).mapTo(UUID.class).stream().toList();
              final int numOfRowsDeleted = rowsDeleted.size();
              totalNumOfRowsDeleted += numOfRowsDeleted;

              // ...
              hasMoreRows = !rowsDeleted.isEmpty();
            }
            // ...
            stopwatch.stop();
            log.debug(
                "Deleted '{}' rows in '{}' secs using db retention policy: {}",
                totalNumOfRowsDeleted,
                stopwatch.elapsed().toSeconds(),
                dbRetentionQuery);
            return totalNumOfRowsDeleted;
          });
    } catch (StatementException errorOnDbRetentionQuery) {
      log.error(
          "Failed to apply db retention policy: {}", dbRetentionQuery, errorOnDbRetentionQuery);
      throw new DbRetentionException("");
    }
  }

  /** ... */
  private static String buildQuery(
      @NonNull String queryTemplate, @NonNull String dbTable, final int dbRetentionInDays) {
    final String pk = pkInQueryFor(dbTable);
    final String timestamp = timestampInQueryFor(dbTable);
    return queryTemplate.formatted(
        dbTable, pk, pk, dbTable, timestamp, dbRetentionInDays, DB_RETENTION_BATCH_SIZE, pk);
  }

  /** ... */
  private static String pkInQueryFor(@NonNull final String dbTable) {
    return LINEAGE_EVENTS.equals(dbTable) ? Columns.RUN_UUID : Columns.ROW_UUID;
  }

  /** ... */
  private static String timestampInQueryFor(@NonNull final String dbTable) {
    return LINEAGE_EVENTS.equals(dbTable) ? Columns.EVENT_TIME : Columns.CREATED_AT;
  }
}
