/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;

public class DatabaseMetrics {
  public static final CollectorRegistry registry = new io.prometheus.client.CollectorRegistry();

  public static final Histogram dbDurationSeconds =
      Histogram.build()
          .namespace("marquez")
          .labelNames("sql_class", "sql_method", "http_method", "http_path")
          .name("db_duration_seconds_by_http_call")
          .help("The time to make the DB call for a given HTTP endpoint.")
          .register(registry);

  public static void recordDbDuration(
      String sqlClass, String sqlMethod, String httpMethod, String httpPath, double duration) {
    dbDurationSeconds.labels(sqlClass, sqlMethod, httpMethod, httpPath).observe(duration);
  }
}
