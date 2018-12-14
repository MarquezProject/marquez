ALTER TABLE jobs DROP COLUMN nominal_time;
ALTER TABLE jobs ALTER COLUMN description DROP NOT NULL;
ALTER TABLE jobs ADD COLUMN current_owner_name VARCHAR(64);

ALTER TABLE job_versions ADD COLUMN version UUID;
ALTER TABLE job_versions ADD COLUMN uri VARCHAR(256);
ALTER TABLE job_versions DROP COLUMN git_repo_uri;
ALTER TABLE job_versions DROP COLUMN git_sha;
ALTER TABLE job_versions ALTER COLUMN input_dataset DROP NOT NULL;
ALTER TABLE job_versions ALTER COLUMN output_dataset DROP NOT NULL;

CREATE TABLE IF NOT EXISTS job_run_definitions (
    guid UUID PRIMARY KEY,
    job_version_guid UUID REFERENCES job_versions(guid),
    run_args_json TEXT,
    content_hash UUID NOT NULL,
    nominal_start_time INTEGER,
    nominal_end_time INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

