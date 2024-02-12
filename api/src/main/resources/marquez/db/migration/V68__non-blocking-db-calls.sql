
ALTER TABLE lineage_events RENAME COLUMN created_at TO "event_received_time";

ALTER TABLE jobs DROP COLUMN current_job_context_uuid;
ALTER TABLE jobs DROP COLUMN current_inputs;
ALTER TABLE jobs DROP COLUMN parent_job_uuid_string;
ALTER TABLE jobs DROP COLUMN simple_name;

ALTER TABLE runs DROP COLUMN run_args_uuid;
ALTER TABLE runs DROP COLUMN job_context_uuid;
ALTER TABLE runs DROP COLUMN parent_run_uuid;
ALTER TABLE runs DROP COLUMN start_run_state_uuid;
ALTER TABLE runs DROP COLUMN end_run_state_uuid;
ALTER TABLE runs DROP COLUMN created_at;
ALTER TABLE runs DROP COLUMN updated_at;

ALTER TABLE job_versions DROP COLUMN job_context_uuid;
