package marquez.db;

import java.util.List;
import marquez.db.mappers.LineageMetricRowMapper;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(LineageMetricRowMapper.class)
public interface OpsDao extends BaseDao {

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
                data_ops_lineage_metrics_hourly hourly_metrics
                ON
                    hs.start_interval = hourly_metrics.start_interval
            ORDER BY
                hs.start_interval;
            """)
  List<LineageMetric> getLastDayMetrics();
}
