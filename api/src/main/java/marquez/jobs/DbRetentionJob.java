/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.DbRetention;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;

/** ... */
@Slf4j
public class DbRetentionJob extends AbstractScheduledService implements Managed {
  private static final Duration NO_DELAY = Duration.ofMinutes(0);
  private static final Duration PERIOD_IN_MINUTES = Duration.ofMinutes(60);

  // ...
  private final int dbRetentionDays;

  // ...
  private final Scheduler fixedRateScheduler;
  private final Jdbi jdbi;

  /** ... */
  public DbRetentionJob(
      @NonNull final Jdbi jdbi, final int frequencyMins, final int dbRetentionDays) {
    checkArgument(frequencyMins > 0, "'frequencyMins' must be > 0");
    checkArgument(dbRetentionDays > 0, "'dbRetentionDays' must be > 0");
    this.dbRetentionDays = dbRetentionDays;
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
    log.info("Starting db retention job...");
    startAsync().awaitRunning();
  }

  @Override
  protected void runOneIteration() {
    try {
      DbRetention.retentionOnDbOrError(jdbi, dbRetentionDays);
    } catch (DbRetentionException errorOnDbRetention) {
      log.error("Failed to apply retention to database.", errorOnDbRetention);
    }
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping db retention job...");
    stopAsync().awaitTerminated();
  }
}
