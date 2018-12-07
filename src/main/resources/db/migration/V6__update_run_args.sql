ALTER TABLE datasets ALTER COLUMN namespace_guid SET NOT NULL;
ALTER TABLE datasets ALTER COLUMN datasource_uuid SET NOT NULL;

ALTER TABLE job_runs DROP COLUMN job_run_args_guid;
ALTER TABLE job_runs ADD job_version_guid UUID NOT NULL;
ALTER TABLE job_runs ADD FOREIGN KEY(job_version_guid) REFERENCES job_versions(guid);

ALTER TABLE job_run_args ADD COLUMN hex_digest VARCHAR(64) NOT NULL UNIQUE;
ALTER TABLE job_run_args ADD COLUMN args_json VARCHAR(512) NOT NULL;
ALTER TABLE job_run_args DROP COLUMN content_hash;
ALTER TABLE job_run_args DROP COLUMN args;
ALTER TABLE job_run_args DROP guid;

ALTER TABLE job_runs ADD job_run_args_hex_digest VARCHAR(64);
ALTER TABLE job_runs ADD FOREIGN KEY(job_run_args_hex_digest) REFERENCES job_run_args(hex_digest);

ALTER TABLE jobs DROP COLUMN category;
ALTER TABLE jobs DROP COLUMN current_ownership;
ALTER TABLE jobs DROP COLUMN current_owner_name;
ALTER TABLE jobs ADD COLUMN current_version_guid UUID;