package marquez.db;

import static org.glassfish.jersey.internal.guava.Preconditions.checkArgument;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementException;

/** A periodic cleanup task for ... */
@Slf4j
public class DbDataRetention extends AbstractScheduledService implements Managed {
  private static final Duration NO_DELAY = Duration.ofMinutes(0);
  private static final Duration PERIOD_IN_MINUTES = Duration.ofMinutes(1);
  private static final int DATA_RETENTION_BATCH_SIZE = 1000;

  // ...
  private final int dataRetentionInDays;

  // ...
  private final Scheduler fixedRateScheduler;
  private final Jdbi jdbi;

  public DbDataRetention(final int dataRetentionInDays, @NonNull Jdbi jdbi) {
    checkArgument(dataRetentionInDays > 0, "'dataRetentionInDays' must be > 0");
    this.dataRetentionInDays = dataRetentionInDays;
    this.jdbi = jdbi;

    // ...
    this.fixedRateScheduler = Scheduler.newFixedRateSchedule(NO_DELAY, PERIOD_IN_MINUTES);
  }

  @Override
  protected Scheduler scheduler() {
    return fixedRateScheduler;
  }

  @Override
  public void start() throws Exception {
    log.info("Starting data retention task...");
    startAsync().awaitRunning();
  }

  @Override
  protected void runOneIteration() {
    // ...
    applyDataRetentionPolicyOn("datasets");
    applyDataRetentionPolicyOn("jobs");
    applyDataRetentionPolicyOn("lineage_events");
  }

  private void applyDataRetentionPolicyOn(@NonNull final String table) {
    log.info(
        "Applying data retention policy of '{}' days to '{}' table...", dataRetentionInDays, table);
    try {
      jdbi.useHandle(
          handle -> {
            int totalNumOfRowsDeleted = 0;
            boolean hasMoreRows = true;
            while (hasMoreRows) {
              final List<UUID> numOfRowsDeleted =
                  handle
                      .createQuery(
                          """
                          DELETE FROM %s
                            WHERE uuid IN (
                              SELECT uuid FROM %s
                              WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '%d days'
                              LIMIT %d
                          ) RETURNING uuid;
                          """
                              .formatted(
                                  table, table, dataRetentionInDays, DATA_RETENTION_BATCH_SIZE))
                      .mapTo(UUID.class)
                      .stream()
                      .toList();
              // ...
              totalNumOfRowsDeleted += numOfRowsDeleted.size();
              hasMoreRows = !numOfRowsDeleted.isEmpty();
            }
            log.info(
                "Successfully deleted '{}' rows from '{}' table.", totalNumOfRowsDeleted, table);
          });
    } catch (StatementException e) {
      log.error("Failed to run data retention query", e);
    }
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping data retention task...");
    stopAsync().awaitTerminated();
  }
}
