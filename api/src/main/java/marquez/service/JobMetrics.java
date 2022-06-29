/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import lombok.NonNull;
import marquez.common.models.RunState;

public class JobMetrics {
  public static final Counter jobs =
      Counter.build()
          .namespace("marquez")
          .name("job_total")
          .labelNames("namespace_name", "job_type")
          .help("Total number of jobs.")
          .register();
  public static final Counter versions =
      Counter.build()
          .namespace("marquez")
          .name("job_versions_total")
          .labelNames("namespace_name", "job_type", "job_name")
          .help("Total number of job versions.")
          .register();
  public static final Gauge runsActive =
      Gauge.build()
          .namespace("marquez")
          .name("job_runs_active")
          .help("Total number of active job runs.")
          .register();
  public static final Gauge runsCompleted =
      Gauge.build()
          .namespace("marquez")
          .name("job_runs_completed")
          .help("Total number of completed job runs.")
          .register();

  public static void emitJobCreationMetric(String namespaceName, String jobMetaType) {
    JobMetrics.jobs.labels(namespaceName, jobMetaType).inc();
  }

  /** Determines whether to increment or decrement run counters given {@link RunState}. */
  public static void emitRunStateCounterMetric(@NonNull RunState runState) {
    switch (runState) {
      case RUNNING:
        runsActive.inc();
        break;
      case COMPLETED:
        runsActive.dec();
        runsCompleted.inc();
        break;
      case ABORTED:
      case FAILED:
        runsActive.dec();
        break;
      default:
        break;
    }
  }
}
