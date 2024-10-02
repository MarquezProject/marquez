/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import marquez.db.mappers.LineageMetricRowMapper;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(LineageMetricRowMapper.class)
public interface StatsDao extends BaseDao {

  @SqlUpdate(
      """
      INSERT INTO current_hour_lineage_metrics(event_time, state)
      VALUES (:eventTime, :state);
      """)
  void createCurrentHourLineageMetric(
      @Bind("eventTime") Instant eventTime, @Bind("state") String state);

  @SqlUpdate(
      """
      INSERT INTO current_day_lineage_metrics(event_time, state)
      VALUES (:eventTime, :state);
      """)
  void createCurrentDayLineageMetric(
      @Bind("eventTime") Instant eventTime, @Bind("state") String state);

  @SqlQuery(
      """
                WITH hour_series AS (
                  SELECT generate_series(
                                                    DATE_TRUNC('hour', NOW() - INTERVAL '23 hours'), -- Start 23 hours ago + current hour
                                                    DATE_TRUNC('hour', NOW()),
                                                    '1 hour'
                                            ) AS start_interval)
                SELECT hs.start_interval,
                       hs.start_interval + INTERVAL '1 hour'                       AS end_interval,
                       COALESCE(current_hour.fail, hourly_metrics.fail, 0)         AS fail,
                       COALESCE(current_hour.start, hourly_metrics.start, 0)       AS start,
                       COALESCE(current_hour.complete, hourly_metrics.complete, 0) AS complete,
                       COALESCE(current_hour.abort, hourly_metrics.abort, 0)       AS abort
                FROM hour_series hs
                         LEFT JOIN
                     lineage_events_by_type_hourly_view hourly_metrics ON hs.start_interval = hourly_metrics.start_interval
                         LEFT JOIN (SELECT DATE_TRUNC('hour', NOW())                                        AS start_interval,
                                           DATE_TRUNC('hour', NOW()) + INTERVAL '1 hour'                    AS end_interval,
                                           COALESCE(SUM(CASE WHEN state = 'FAIL' THEN 1 ELSE 0 END), 0)     AS fail,
                                           COALESCE(SUM(CASE WHEN state = 'START' THEN 1 ELSE 0 END), 0)    AS start,
                                           COALESCE(SUM(CASE WHEN state = 'COMPLETE' THEN 1 ELSE 0 END), 0) AS complete,
                                           COALESCE(SUM(CASE WHEN state = 'ABORT' THEN 1 ELSE 0 END), 0)    AS abort
                                    FROM current_hour_lineage_metrics
                                    WHERE event_time >= DATE_TRUNC('hour', now())
                                      AND event_time < DATE_TRUNC('hour', now()) + INTERVAL '1 hour') current_hour
                                   ON current_hour.start_interval = hs.start_interval
                ORDER BY hs.start_interval;
              """)
  List<LineageMetric> getLastDayMetrics();

  @SqlQuery(
      """
        WITH local_now AS (
            SELECT (NOW() AT TIME ZONE :timezone) AS local_now
        ),
             day_series AS (
                 SELECT generate_series(
                                DATE_TRUNC('day', ln.local_now - INTERVAL '6 days'),
                                DATE_TRUNC('day', ln.local_now),
                                '1 day'
                        ) AS start_interval
                 FROM local_now ln
             )
        SELECT
            ds.start_interval,
            ds.start_interval + INTERVAL '1 day' AS end_interval,
            COALESCE(current_day.fail, mv.fail, 0) AS fail,
            COALESCE(current_day.start, mv.start, 0) AS start,
            COALESCE(current_day.complete, mv.complete, 0) AS complete,
            COALESCE(current_day.abort, mv.abort, 0) AS abort
        FROM day_series ds
                 LEFT JOIN lineage_events_by_type_daily_view mv
                           ON ds.start_interval = (mv.start_interval AT TIME ZONE 'UTC') AT TIME ZONE :timezone
                 LEFT JOIN (
            SELECT
                DATE_TRUNC('day', ln.local_now) AS start_interval,
                DATE_TRUNC('day', ln.local_now) + INTERVAL '1 day' AS end_interval,
                COALESCE(SUM(CASE WHEN state = 'FAIL' THEN 1 ELSE 0 END), 0) AS fail,
                COALESCE(SUM(CASE WHEN state = 'START' THEN 1 ELSE 0 END), 0) AS start,
                COALESCE(SUM(CASE WHEN state = 'COMPLETE' THEN 1 ELSE 0 END), 0) AS complete,
                COALESCE(SUM(CASE WHEN state = 'ABORT' THEN 1 ELSE 0 END), 0) AS abort
            FROM current_day_lineage_metrics
                     CROSS JOIN local_now ln
            WHERE (event_time AT TIME ZONE :timezone) >= DATE_TRUNC('day', ln.local_now)
              AND (event_time AT TIME ZONE :timezone) < DATE_TRUNC('day', ln.local_now) + INTERVAL '1 day'
            GROUP BY DATE_TRUNC('day', ln.local_now)
        ) current_day
                           ON ds.start_interval = current_day.start_interval
        ORDER BY ds.start_interval;
        """)
  List<LineageMetric> getLastWeekMetrics(String timezone);
}
