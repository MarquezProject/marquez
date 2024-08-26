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
public interface OpsDao extends BaseDao {

  @SqlQuery(
      """
              SELECT
                  gs.start_interval AS start_interval,
                  gs.start_interval + INTERVAL '1 hour' AS end_interval,
                  COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'FAIL'), 0) AS fail,
                          COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'START'), 0) AS start,
                          COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'COMPLETE'), 0) AS complete,
                          COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'ABORT'), 0) AS abort
                      FROM
                          generate_series(
                                  DATE_TRUNC('hour', NOW() - INTERVAL '1 day'),  -- Start 24 hours ago, truncated to the hour
                                  DATE_TRUNC('hour', NOW() - INTERVAL '1 hour'), -- End at the start of the current hour
                                  '1 hour'::interval                             -- Step by 1 hour
                          ) AS gs(start_interval)
                              LEFT JOIN (
                              SELECT
                                  id,
                                  DATE_TRUNC('hour', metric_time) AS metric_hour,
                                  state
                              FROM
                                  data_ops_lineage_metrics
                              WHERE
                                  metric_time >= NOW() - INTERVAL '1 day'
                          ) dom ON gs.start_interval = dom.metric_hour
                      GROUP BY
                          gs.start_interval
                      ORDER BY
                          gs.start_interval;
              """)
  List<LineageMetric> getLastDayMetrics();

  @SqlUpdate(
      """
              INSERT INTO data_ops_lineage_metrics (metric_time, state)
              VALUES (:metricTime, :state);
              """)
  void createLineageMetric(@Bind("metricTime") Instant metricTime, @Bind("state") String state);
}
