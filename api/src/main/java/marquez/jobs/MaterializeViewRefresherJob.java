/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import java.time.LocalTime;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/** A job that refreshes materialized views on a fixed schedule in Marquez. */
@Slf4j
public class MaterializeViewRefresherJob extends AbstractScheduledService implements Managed {

  private final int FREQUENCY = 60;
  private final Scheduler fixedRateScheduler;
  private final Jdbi jdbi;

  public MaterializeViewRefresherJob(@NonNull final Jdbi jdbi) {
    // Connection to database retention policy will be applied.
    this.jdbi = jdbi;

    // Define fixed schedule and delay until the hour strikes.
    int MINUTES_IN_HOUR = 60;
    LocalTime now = LocalTime.now();
    int minutesRemaining =
        MINUTES_IN_HOUR - now.getMinute(); // Get the remaining minutes in the hour
    Duration duration = Duration.ofMinutes(minutesRemaining);
    this.fixedRateScheduler =
        Scheduler.newFixedRateSchedule(duration, Duration.ofMinutes(FREQUENCY));
  }

  @Override
  protected Scheduler scheduler() {
    return fixedRateScheduler;
  }

  @Override
  public void start() throws Exception {
    startAsync().awaitRunning();
    log.info("Refreshing materialized views every '{}' mins.", FREQUENCY);
  }

  @Override
  protected void runOneIteration() {
    try {
      log.info("Refreshing materialized views...");
      jdbi.useHandle(
          handle -> {
            handle.execute("REFRESH MATERIALIZED VIEW lineage_events_by_type_hourly_view");
          });
      log.info("Materialized view `lineage_events_by_type_hourly_view` refreshed.");

    } catch (Exception error) {
      log.error("Failed to materialize views. Retrying on next run...", error);
    }
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping materialized views job...");
    stopAsync().awaitTerminated();
  }
}
