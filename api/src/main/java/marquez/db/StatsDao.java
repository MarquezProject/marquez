/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.util.List;
import marquez.db.mappers.IntervalMetricRowMapper;
import marquez.db.mappers.LineageMetricRowMapper;
import marquez.db.models.IntervalMetric;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(LineageMetricRowMapper.class)
@RegisterRowMapper(IntervalMetricRowMapper.class)
public interface StatsDao extends BaseDao {

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
                                   COALESCE(SUM(CASE WHEN event_type = 'FAIL' THEN 1 ELSE 0 END), 0)     AS fail,
                                   COALESCE(SUM(CASE WHEN event_type = 'START' THEN 1 ELSE 0 END), 0)    AS start,
                                   COALESCE(SUM(CASE WHEN event_type = 'COMPLETE' THEN 1 ELSE 0 END), 0) AS complete,
                                   COALESCE(SUM(CASE WHEN event_type = 'ABORT' THEN 1 ELSE 0 END), 0)    AS abort
                            FROM lineage_events
                            WHERE event_time >= DATE_TRUNC('hour', now())
                              AND event_time < DATE_TRUNC('hour', now()) + INTERVAL '1 hour') current_hour
                           ON current_hour.start_interval = hs.start_interval
        ORDER BY hs.start_interval;
        """)
  List<LineageMetric> getLastDayMetrics();

  @SqlQuery(
      """
              WITH date_bounds AS (SELECT DATE_TRUNC('day', NOW() AT TIME ZONE :timezone) - INTERVAL '6 days' AS start_date,
                                          DATE_TRUNC('day', NOW() AT TIME ZONE :timezone) + INTERVAL '1 day'  AS end_date),
                   day_series AS (SELECT start_date + INTERVAL '1 day' * n AS day
                                  FROM date_bounds,
                                       generate_series(0, 6) AS n),
                   metrics AS (SELECT DATE_TRUNC('day', mv.start_interval AT TIME ZONE :timezone) AS day,
                                      SUM(mv.fail)                                                AS fail,
                                      SUM(mv.start)                                               AS start,
                                      SUM(mv.complete)                                            AS complete,
                                      SUM(mv.abort)                                               AS abort
                               FROM lineage_events_by_type_hourly_view AS mv,
                                    date_bounds
                               WHERE mv.start_interval >= (start_date AT TIME ZONE :timezone)
                                 AND mv.start_interval < (end_date AT TIME ZONE :timezone)
                               GROUP BY day)
              SELECT ds.day                                  AS start_interval,
                     ds.day + INTERVAL '1 day'               AS end_interval,
                     COALESCE(today.fail, m.fail, 0)         AS fail,
                     COALESCE(today.start, m.start, 0)       AS start,
                     COALESCE(today.complete, m.complete, 0) AS complete,
                     COALESCE(today.abort, m.abort, 0)       AS abort
              FROM day_series ds
                       LEFT JOIN metrics m ON m.day = ds.day
                       left outer join (WITH local_now AS (SELECT DATE_TRUNC('day', now() at time zone :timezone) AS time)
                                        SELECT local_now.time                                                 AS start_interval,
                                               local_now.time + INTERVAL '1 day'                              AS end_interval,
                                               COALESCE(SUM(CASE WHEN event_type = 'FAIL' THEN 1 END), 0)     AS fail,
                                               COALESCE(SUM(CASE WHEN event_type = 'START' THEN 1 END), 0)    AS start,
                                               COALESCE(SUM(CASE WHEN event_type = 'COMPLETE' THEN 1 END), 0) AS complete,
                                               COALESCE(SUM(CASE WHEN event_type = 'ABORT' THEN 1 END), 0)    AS abort
                                        FROM lineage_events le,
                                             local_now
                                        WHERE (le.event_time AT TIME ZONE :timezone) >= local_now.time
                                          AND (le.event_time AT TIME ZONE :timezone) < local_now.time + INTERVAL '1 day'
                                        GROUP BY local_now.time) as today on
                  ds.day = today.start_interval
              ORDER BY ds.day;
              """)
  List<LineageMetric> getLastWeekMetrics(String timezone);

  @SqlQuery(
      """
          WITH hourly_series AS (
              SELECT
                  generate_series(
                          date_trunc('hour', NOW() - INTERVAL '23 hours'),
                          date_trunc('hour', NOW()),
                          '1 hour'
                  ) AS start_interval
          ),
               before_count AS (
                   SELECT
                       count(*) AS job_count
                   FROM jobs
                   WHERE created_at < date_trunc('hour', NOW() - INTERVAL '23 hours')
               ),
               hourly_jobs AS (
                   SELECT
                       hs.start_interval,
                       COUNT(j.uuid) AS jobs_in_hour
                   FROM hourly_series hs
                            LEFT JOIN jobs j
                                      ON j.created_at >= hs.start_interval
                                          AND j.created_at < hs.start_interval + INTERVAL '1 hour'
                   GROUP BY hs.start_interval
               ),
               cumulative_jobs AS (
                   SELECT
                       start_interval,
                       SUM(jobs_in_hour) OVER (ORDER BY start_interval) + (SELECT job_count FROM before_count) AS cumulative_job_count
                   FROM hourly_jobs
               )
          SELECT
              start_interval,
              start_interval + INTERVAL '1 hour' AS end_interval,
              cumulative_job_count AS count
          FROM cumulative_jobs
          ORDER BY start_interval;
          """)
  List<IntervalMetric> getLastDayJobs();

  @SqlQuery(
      """
        WITH local_now AS (
            SELECT (NOW() AT TIME ZONE :timezone) AS local_now
        ),
             daily_series AS (
                 SELECT
                     generate_series(
                             date_trunc('day', ln.local_now - INTERVAL '6 days'),  -- Start at the beginning of 6 days ago in the desired timezone
                             date_trunc('day', ln.local_now),   -- End at the beginning of the next day in the desired timezone
                             '1 day'
                     ) AS start_interval
                 FROM local_now ln
             ),
             before_count AS (
                 SELECT
                     count(*) AS job_count
                 FROM jobs, local_now ln
                 WHERE (jobs.created_at AT TIME ZONE :timezone) < date_trunc('day', ln.local_now - INTERVAL '6 days')
             ),
             daily_jobs AS (
                 SELECT
                     ds.start_interval,
                     COUNT(j.uuid) AS jobs_in_day
                 FROM daily_series ds
                          LEFT JOIN jobs j
                                    ON (j.created_at AT TIME ZONE :timezone) >= ds.start_interval
                                        AND (j.created_at AT TIME ZONE :timezone) < ds.start_interval + INTERVAL '1 day'
                 GROUP BY ds.start_interval
             ),
             cumulative_jobs AS (
                 SELECT
                     start_interval,
                     SUM(jobs_in_day) OVER (ORDER BY start_interval)
                         + (SELECT job_count FROM before_count) AS cumulative_job_count
                 FROM daily_jobs
             )
        SELECT
            start_interval AS start_interval,
            start_interval + INTERVAL '1 day' AS end_interval,
            cumulative_job_count AS count
        FROM cumulative_jobs
        ORDER BY start_interval;
        """)
  List<IntervalMetric> getLastWeekJobs(String timezone);

  @SqlQuery(
      """
              WITH hourly_series AS (
                  SELECT
                      generate_series(
                              date_trunc('hour', NOW() - INTERVAL '23 hours'),
                              date_trunc('hour', NOW()),
                              '1 hour'
                      ) AS start_interval
              ),
                   before_count AS (
                       SELECT
                           count(*) AS dataset_count
                       FROM datasets
                       WHERE created_at < date_trunc('hour', NOW() - INTERVAL '23 hours')
                   ),
                   hourly_datasets AS (
                       SELECT
                           hs.start_interval,
                           COUNT(d.uuid) AS datasets_in_hour
                       FROM hourly_series hs
                                LEFT JOIN datasets d
                                          ON d.created_at >= hs.start_interval
                                              AND d.created_at < hs.start_interval + INTERVAL '1 hour'
                       GROUP BY hs.start_interval
                   ),
                   cumulative_datasets AS (
                       SELECT
                           start_interval,
                           SUM(datasets_in_hour) OVER (ORDER BY start_interval) + (SELECT dataset_count FROM before_count) AS cumulative_dataset_count
                       FROM hourly_datasets
                   )
              SELECT
                  start_interval,
                  start_interval + INTERVAL '1 hour' AS end_interval,
                  cumulative_dataset_count AS count
              FROM cumulative_datasets
              ORDER BY start_interval;
              """)
  List<IntervalMetric> getLastDayDatasets();

  @SqlQuery(
      """
        WITH local_now AS (
            SELECT (NOW() AT TIME ZONE :timezone) AS local_now
        ),
             daily_series AS (
                 SELECT
                     generate_series(
                             date_trunc('day', ln.local_now - INTERVAL '6 days'),         -- Start at the beginning of 6 days ago in the desired timezone
                             date_trunc('day', ln.local_now                   ),          -- End at the beginning of the next day in the desired timezone
                             '1 day'
                     ) AS start_interval
                 FROM local_now ln
             ),
             before_count AS (
                 SELECT
                     count(*) AS dataset_count
                 FROM datasets d
                          CROSS JOIN local_now ln
                 WHERE (d.created_at AT TIME ZONE :timezone) < date_trunc('day', ln.local_now - INTERVAL '6 days')
             ),
             daily_datasets AS (
                 SELECT
                     ds.start_interval,
                     COUNT(d.uuid) AS datasets_in_day
                 FROM daily_series ds
                          LEFT JOIN datasets d
                                    ON (d.created_at AT TIME ZONE :timezone) >= ds.start_interval
                                        AND (d.created_at AT TIME ZONE :timezone) < ds.start_interval + INTERVAL '1 day'
                 GROUP BY ds.start_interval
             ),
             cumulative_datasets AS (
                 SELECT
                     start_interval,
                     SUM(datasets_in_day) OVER (ORDER BY start_interval) + (SELECT dataset_count FROM before_count) AS cumulative_dataset_count
                 FROM daily_datasets
             )
        SELECT
            start_interval AS start_interval,
            start_interval + INTERVAL '1 day' AS end_interval,
            cumulative_dataset_count AS count
        FROM cumulative_datasets
        ORDER BY start_interval;
        """)
  List<IntervalMetric> getLastWeekDatasets(String timezone);

  @SqlQuery(
      """
                  WITH hourly_series AS (
                      SELECT
                          generate_series(
                                  date_trunc('hour', NOW() - INTERVAL '23 hours'),
                                  date_trunc('hour', NOW()),
                                  '1 hour'
                          ) AS start_interval
                  ),
                       before_count AS (
                           SELECT
                               count(*) AS dataset_count
                           FROM sources
                           WHERE created_at < date_trunc('hour', NOW() - INTERVAL '23 hours')
                       ),
                       hourly_sources AS (
                           SELECT
                               hs.start_interval,
                               COUNT(s.uuid) AS sources_in_hour
                           FROM hourly_series hs
                                    LEFT JOIN sources s
                                              ON s.created_at >= hs.start_interval
                                                  AND s.created_at < hs.start_interval + INTERVAL '1 hour'
                           GROUP BY hs.start_interval
                       ),
                       cumulative_sources AS (
                           SELECT
                               start_interval,
                               SUM(sources_in_hour) OVER (ORDER BY start_interval) + (SELECT dataset_count FROM before_count) AS cumulative_dataset_count
                           FROM hourly_sources
                       )
                  SELECT
                      start_interval,
                      start_interval + INTERVAL '1 hour' AS end_interval,
                      cumulative_dataset_count AS count
                  FROM cumulative_sources
                  ORDER BY start_interval;
                  """)
  List<IntervalMetric> getLastDaySources();

  @SqlQuery(
      """
        WITH local_now AS (
            SELECT (NOW() AT TIME ZONE :timezone) AS local_now
        ),
             daily_series AS (
                 SELECT
                     generate_series(
                             date_trunc('day', ln.local_now - INTERVAL '6 days'),       -- Start at the beginning of 6 days ago in the desired timezone
                             date_trunc('day', ln.local_now),                           -- End at the beginning of the next day in the desired timezone
                             '1 day'
                     ) AS start_interval
                 FROM local_now ln
             ),
             before_count AS (
                 SELECT
                     count(*) AS dataset_count
                 FROM sources s
                          CROSS JOIN local_now ln
                 WHERE (s.created_at AT TIME ZONE :timezone) < date_trunc('day', ln.local_now - INTERVAL '6 days')
             ),
             daily_sources AS (
                 SELECT
                     ds.start_interval,
                     COUNT(s.uuid) AS sources_in_day
                 FROM daily_series ds
                          LEFT JOIN sources s
                                    ON (s.created_at AT TIME ZONE :timezone) >= ds.start_interval
                                        AND (s.created_at AT TIME ZONE :timezone) < ds.start_interval + INTERVAL '1 day'
                 GROUP BY ds.start_interval
             ),
             cumulative_sources AS (
                 SELECT
                     start_interval,
                     SUM(sources_in_day) OVER (ORDER BY start_interval) + (SELECT dataset_count FROM before_count) AS cumulative_dataset_count
                 FROM daily_sources
             )
        SELECT
            start_interval AS start_interval,
            start_interval + INTERVAL '1 day' AS end_interval,
            cumulative_dataset_count AS count
        FROM cumulative_sources
        ORDER BY start_interval;
        """)
  List<IntervalMetric> getLastWeekSources(String timezone);
}
