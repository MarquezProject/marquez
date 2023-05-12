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

/**
 * A job that applies a data retention policy on a fixed schedule to source, dataset, and job
 * metadata. Use {@code frequencyMins} in {@link DbRetentionConfig} to override the default job run
 * frequency interval of {@code 15} mins. You can also use {@code retentionDays} to override the
 * default retention policy of {@code 7} days; metadata with a collection date {@code >
 * retentionDays} will be deleted.
 */
@Slf4j
public class DbRetentionJob extends AbstractScheduledService implements Managed {
  private static final Duration NO_DELAY = Duration.ofMinutes(0);

  /* The retention policy (in days). */
  private final int retentionDays;

  private final Scheduler fixedRateScheduler;
  private final Jdbi jdbi;

  /**
   * Constructs a {@code DbRetentionJob} with a run frequency {@code frequencyMins} and retention
   * policy of {@code retentionDays}.
   */
  public DbRetentionJob(
      @NonNull final Jdbi jdbi, final int frequencyMins, final int retentionDays) {
    checkArgument(frequencyMins > 0, "'frequencyMins' must be > 0");
    checkArgument(retentionDays > 0, "'retentionDays' must be > 0");
    this.retentionDays = retentionDays;
    this.jdbi = jdbi;

    // Define fixed schedule with no delay.
    this.fixedRateScheduler =
        Scheduler.newFixedRateSchedule(NO_DELAY, Duration.ofMinutes(frequencyMins));
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
      // Attempt to apply a database retention policy. An exception is thrown on failed retention
      // policy attempts requiring we handle the throwable and log the error.
      DbRetention.retentionOnDbOrError(jdbi, retentionDays);
    } catch (DbRetentionException errorOnDbRetention) {
      log.error("Failed to apply db retention policy!", errorOnDbRetention);
    }
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping db retention job...");
    stopAsync().awaitTerminated();
  }
}
