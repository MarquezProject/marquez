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
                COALESCE(SUM(CASE WHEN event_type = 'FAIL' THEN 1 ELSE 0 END), 0) AS fail,
                COALESCE(SUM(CASE WHEN event_type = 'START' THEN 1 ELSE 0 END), 0) AS start,
                COALESCE(SUM(CASE WHEN event_type = 'COMPLETE' THEN 1 ELSE 0 END), 0) AS complete,
                COALESCE(SUM(CASE WHEN event_type = 'ABORT' THEN 1 ELSE 0 END), 0) AS abort
            FROM lineage_events, local_now ln
            WHERE (event_time AT TIME ZONE :timezone) >= DATE_TRUNC('day', ln.local_now)
              AND (event_time AT TIME ZONE :timezone) < DATE_TRUNC('day', ln.local_now) + INTERVAL '1 day'
            GROUP BY DATE_TRUNC('day', ln.local_now)
        ) current_day
                           ON ds.start_interval = current_day.start_interval
        ORDER BY ds.start_interval;
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
