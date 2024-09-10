CREATE MATERIALIZED VIEW IF NOT EXISTS lineage_events_by_type_hourly_view AS
SELECT DATE_TRUNC('hour', event_time)                  AS start_interval,
       COUNT(*) FILTER (WHERE event_type = 'FAIL')     AS fail,
       COUNT(*) FILTER (WHERE event_type = 'START')    AS start,
       COUNT(*) FILTER (WHERE event_type = 'COMPLETE') AS complete,
       COUNT(*) FILTER (WHERE event_type = 'ABORT')    AS abort
FROM lineage_events
GROUP BY DATE_TRUNC('hour', event_time);

CREATE INDEX IF NOT EXISTS lineage_events_by_type_hourly_view_for_start_interval_idx
    ON lineage_events_by_type_hourly_view (start_interval);

CREATE MATERIALIZED VIEW IF NOT EXISTS lineage_events_by_type_daily_view AS
SELECT DATE_TRUNC('day', event_time)                   AS start_interval,
       COUNT(*) FILTER (WHERE event_type = 'FAIL')     AS fail,
       COUNT(*) FILTER (WHERE event_type = 'START')    AS start,
       COUNT(*) FILTER (WHERE event_type = 'COMPLETE') AS complete,
       COUNT(*) FILTER (WHERE event_type = 'ABORT')    AS abort
FROM lineage_events
GROUP BY DATE_TRUNC('day', event_time);

CREATE INDEX IF NOT EXISTS lineage_events_by_type_daily_view_for_start_interval_idx
    ON lineage_events_by_type_daily_view (start_interval);
