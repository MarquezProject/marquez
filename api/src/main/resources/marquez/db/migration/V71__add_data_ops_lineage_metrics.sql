CREATE MATERIALIZED VIEW IF NOT EXISTS data_ops_lineage_metrics_hourly AS
SELECT
    DATE_TRUNC('hour', event_time) AS start_interval,
    COUNT(*) FILTER (WHERE event_type = 'FAIL') AS fail,
    COUNT(*) FILTER (WHERE event_type = 'START') AS start,
    COUNT(*) FILTER (WHERE event_type = 'COMPLETE') AS complete,
    COUNT(*) FILTER (WHERE event_type = 'ABORT') AS abort
FROM
    lineage_events
GROUP BY
    DATE_TRUNC('hour', event_time)
WITH DATA;

CREATE INDEX IF NOT EXISTS idx_metrics_hourly_start_interval
    ON data_ops_lineage_metrics_hourly (start_interval);

CREATE MATERIALIZED VIEW IF NOT EXISTS data_ops_lineage_metrics_daily AS
SELECT
    DATE_TRUNC('day', event_time) AS start_interval,
    COUNT(*) FILTER (WHERE event_type = 'FAIL') AS fail,
    COUNT(*) FILTER (WHERE event_type = 'START') AS start,
    COUNT(*) FILTER (WHERE event_type = 'COMPLETE') AS complete,
    COUNT(*) FILTER (WHERE event_type = 'ABORT') AS abort
FROM
    lineage_events
GROUP BY
    DATE_TRUNC('day', event_time)
WITH DATA;

CREATE INDEX IF NOT EXISTS idx_metrics_daily_start_interval
    ON data_ops_lineage_metrics_daily (start_interval);