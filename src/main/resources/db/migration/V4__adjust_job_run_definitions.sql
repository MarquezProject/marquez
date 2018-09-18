ALTER TABLE job_runs
DROP COLUMN run_guid;

ALTER TABLE job_runs
DROP COLUMN job_version_guid;

ALTER TABLE job_runs
DROP COLUMN input_dataset_version_guid;

ALTER TABLE job_runs
DROP COLUMN output_dataset_version_guid;

ALTER TABLE job_runs
DROP COLUMN latest_heartbeat;

ALTER TABLE job_runs
ADD COLUMN job_run_definition_guid UUID REFERENCES job_run_definitions(guid);

ALTER TABLE job_runs
ADD COLUMN current_state INTEGER;