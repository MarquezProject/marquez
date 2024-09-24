CREATE TABLE IF NOT EXISTS current_hour_lineage_metrics
(
    event_time TIMESTAMP,
    state      VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS current_hour_lineage_metrics_for_event_time_idx
    ON current_hour_lineage_metrics (event_time);

CREATE TABLE IF NOT EXISTS current_day_lineage_metrics
(
    event_time TIMESTAMP,
    state      VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS current_day_lineage_metrics_for_event_time_idx
    ON current_day_lineage_metrics (event_time);