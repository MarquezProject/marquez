/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.DbRetention;
import marquez.db.exceptions.DbRetentionException;
import org.jdbi.v3.core.Jdbi;

/**
 * A job that applies a retention policy on a fixed schedule to source, dataset, and job metadata in
 * Marquez. Use {@code frequencyMins} in {@link DbRetentionConfig} to override the default job run
 * frequency interval of {@code 15} mins. You can also use {@code retentionDays} to override the
 * default retention policy of {@code 7} days; metadata with a collection date {@code >
 * retentionDays} will be deleted. To limit the number of metadata purged per retention execution
 * and reduce impact on the database, we recommend adjusting {@code numberOfRowsPerBatch}.
 */
@Slf4j
public class DbRetentionJob extends AbstractScheduledService implements Managed {
  private static final Duration NO_DELAY = Duration.ofMinutes(0);

  /* The retention policy frequency. */
  private final int frequencyMins;

  /* The number of rows deleted per batch. */
  private final int numberOfRowsPerBatch;

  /* The retention days. */
  private final int retentionDays;

  private final Scheduler fixedRateScheduler;
  private final Jdbi jdbi;

  /**
   * Constructs a {@code DbRetentionJob} with a run frequency {@code frequencyMins}, chunk size of
   * {@code numberOfRowsPerBatch} that can be deleted per retention job execution and retention days
   * of {@code retentionDays}.
   */
  public DbRetentionJob(
      @NonNull final Jdbi jdbi, @NonNull final DbRetentionConfig dbRetentionConfig) {
    this.frequencyMins = dbRetentionConfig.getFrequencyMins();
    this.numberOfRowsPerBatch = dbRetentionConfig.getNumberOfRowsPerBatch();
    this.retentionDays = dbRetentionConfig.getRetentionDays();

    // Connection to database retention policy will be applied.
    this.jdbi = jdbi;

    // Define fixed schedule with no delay.
    this.fixedRateScheduler =
        Scheduler.newFixedRateSchedule(
            NO_DELAY, Duration.ofMinutes(dbRetentionConfig.getFrequencyMins()));
  }

  @Override
  protected Scheduler scheduler() {
    return fixedRateScheduler;
  }

  @Override
  public void start() throws Exception {
    startAsync().awaitRunning();
    log.info(
        "Started db retention job with retention policy of '{}' days, "
            + "scheduled to be applied every '{}' mins.",
        retentionDays,
        frequencyMins);
  }

  @Override
  protected void runOneIteration() {
    try {
      // Attempt to apply a database retention policy. An exception is thrown on failed retention
      // policy attempts requiring we handle the throwable and log the error.
      DbRetention.retentionOnDbOrError(jdbi, numberOfRowsPerBatch, retentionDays);
    } catch (DbRetentionException errorOnDbRetention) {
      log.error(
          "Failed to apply retention policy of '{}' days to database!",
          retentionDays,
          errorOnDbRetention);
    }
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping db retention job...");
    stopAsync().awaitTerminated();
  }
}
