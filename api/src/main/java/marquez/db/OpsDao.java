package marquez.db;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
                gs.start_interval + :intervalStep * ('1 ' || :intervalUnit)::INTERVAL AS end_interval,
                COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'FAIL'), 0) AS fail,
                COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'START'), 0) AS start,
                COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'COMPLETE'), 0) AS complete,
                COALESCE(COUNT(dom.id) FILTER (WHERE dom.state = 'ABORT'), 0) AS abort
            FROM
                generate_series(
                    :startInterval::TIMESTAMP,
                    :endInterval::TIMESTAMP,
                    :intervalStep * ('1 ' || :intervalUnit)::INTERVAL
                ) AS gs(start_interval)
            LEFT JOIN (
                SELECT
                    id,
                    DATE_TRUNC(:dateTruncUnit, metric_time) AS metric_time_truncated,
                    state
                FROM
                    data_ops_lineage_metrics
                WHERE
                    metric_time >= :metricStartInterval::TIMESTAMP
            ) dom ON gs.start_interval = dom.metric_time_truncated
            GROUP BY
                gs.start_interval
            ORDER BY
                gs.start_interval;
            """)
    List<LineageMetric> getMetrics(
            @Bind("startInterval") Instant startInterval,
            @Bind("endInterval") Instant endInterval,
            @Bind("intervalStep") int intervalStep,
            @Bind("intervalUnit") String intervalUnit,
            @Bind("dateTruncUnit") String dateTruncUnit,
            @Bind("metricStartInterval") Instant metricStartInterval);

    default List<LineageMetric> getLastDayMetrics() {
        Instant now = Instant.now();
        Instant startInterval = now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        Instant endInterval = now.minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);

        return getMetrics(
                startInterval,
                endInterval,
                1,
                "hour",
                "hour",
                startInterval
        );
    }

    default List<LineageMetric> getLastWeekMetrics() {
        Instant now = Instant.now();
        Instant startInterval = now.minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant endInterval = now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

        return getMetrics(
                startInterval,
                endInterval,
                1,
                "day",
                "day",
                startInterval
        );
    }

    @SqlUpdate(
            """
            INSERT INTO data_ops_lineage_metrics (metric_time, state)
            VALUES (:metricTime, :state);
            """)
    void createLineageMetric(@Bind("metricTime") Instant metricTime, @Bind("state") String state);
}
