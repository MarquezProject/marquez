ALTER TABLE job_runs
DROP COLUMN run_guid,
DROP COLUMN job_version_guid,
DROP COLUMN input_dataset_version_guid,
DROP COLUMN output_dataset_version_guid,
DROP COLUMN latest_heartbeat;

ALTER TABLE job_runs
ADD COLUMN job_run_definition_guid UUID REFERENCES job_run_definitions(guid),
ADD COLUMN current_state INTEGER;