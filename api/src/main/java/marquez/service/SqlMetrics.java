/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

public class SqlMetrics {
  public static final CollectorRegistry registry = new io.prometheus.client.CollectorRegistry();

  public static final Histogram duration =
      Histogram.build()
          .namespace("marquez")
          .labelNames("object_name", "method_name", "endpoint_method", "endpoint_path")
          .name("sql_duration_seconds")
          .help("SQL execution duration in seconds")
          .register(registry);

  public static void emitSqlDurationMetrics(
      String objectName,
      String methodName,
      String endpointMethod,
      String endpointPath,
      double duration) {
    SqlMetrics.duration
        .labels(objectName, methodName, endpointMethod, endpointPath)
        .observe(duration);
  }
}
