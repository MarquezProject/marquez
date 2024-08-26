CREATE TABLE data_ops_lineage_metrics
(
    id          SERIAL PRIMARY KEY,
    metric_time TIMESTAMP    NOT NULL,
    state       VARCHAR(64)  NOT NULL
);

CREATE INDEX idx_metric_time_name ON data_ops_lineage_metrics (metric_time);
