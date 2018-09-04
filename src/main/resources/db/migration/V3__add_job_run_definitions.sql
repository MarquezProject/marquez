ALTER TABLE jobs DROP COLUMN nominal_time;

CREATE TABLE IF NOT EXISTS job_run_definitions (
    guid UUID PRIMARY KEY,
    job_version_guid UUID REFERENCES job_versions(guid) NOT NULL,
    run_args_json TEXT,
    nominal_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

