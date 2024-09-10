/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.util.List;
import marquez.db.mappers.LineageMetricRowMapper;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(LineageMetricRowMapper.class)
public interface StatsDao extends BaseDao {

  @SqlQuery(
      """
            WITH hour_series AS (
                SELECT
                    generate_series(
                            DATE_TRUNC('hour', NOW() - INTERVAL '1 day'),
                            DATE_TRUNC('hour', NOW() - INTERVAL '1 hour'),
                            '1 hour'
                    ) AS start_interval
            )
            SELECT
                hs.start_interval,
                hs.start_interval + INTERVAL '1 hour' AS end_interval,
                COALESCE(hourly_metrics.fail, 0) AS fail,
                COALESCE(hourly_metrics.start, 0) AS start,
                COALESCE(hourly_metrics.complete, 0) AS complete,
                COALESCE(hourly_metrics.abort, 0) AS abort
            FROM
                hour_series hs
                    LEFT JOIN
                lineage_events_by_type_hourly_view hourly_metrics
                ON
                    hs.start_interval = hourly_metrics.start_interval
            ORDER BY
                hs.start_interval;
            """)
  List<LineageMetric> getLastDayMetrics();

  @SqlQuery(
      """
          WITH day_series AS (
              SELECT
                  generate_series(
                          DATE_TRUNC('day', NOW() - INTERVAL '7 days'),  -- Start 7 days ago
                          DATE_TRUNC('day', NOW()) - INTERVAL '1 day',   -- End at the start of today
                          '1 day'
                  ) AS start_interval
          )
          SELECT
              ds.start_interval,
              ds.start_interval + INTERVAL '1 day' AS end_interval,
              COALESCE(mv.fail, 0) AS fail,
              COALESCE(mv.start, 0) AS start,
              COALESCE(mv.complete, 0) AS complete,
              COALESCE(mv.abort, 0) AS abort
          FROM
              day_series ds
                  LEFT JOIN
              lineage_events_by_type_daily_view mv
              ON
                  ds.start_interval = mv.start_interval
          ORDER BY
              ds.start_interval;
          """)
  List<LineageMetric> getLastWeekMetrics();
}
