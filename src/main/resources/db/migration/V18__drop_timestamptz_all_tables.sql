ALTER TABLE datasets
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at TYPE timestamp,
ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE db_table_versions
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE iceberg_table_versions
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE job_run_states
ALTER COLUMN transitioned_at TYPE timestamp,
ALTER COLUMN transitioned_at SET NOT NULL;

ALTER TABLE job_runs
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN nominal_start_time TYPE timestamp,
ALTER COLUMN nominal_end_time TYPE timestamp;

UPDATE job_versions SET updated_at = CURRENT_TIMESTAMP;

ALTER TABLE job_versions
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at TYPE timestamp,
ALTER COLUMN updated_at SET NOT NULL,
ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

UPDATE jobs SET updated_at = CURRENT_TIMESTAMP;

ALTER TABLE jobs
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at TYPE timestamp,
ALTER COLUMN updated_at SET NOT NULL,
ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE namespace_ownerships
ALTER COLUMN started_at TYPE timestamp,
ALTER COLUMN started_at SET DEFAULT CURRENT_TIMESTAMP;

UPDATE namespaces SET updated_at = CURRENT_TIMESTAMP;

ALTER TABLE namespaces
ALTER COLUMN updated_at TYPE timestamp,
ALTER COLUMN updated_at SET NOT NULL,
ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE owners
ALTER COLUMN created_at TYPE timestamp,
ALTER COLUMN created_at SET NOT NULL;
