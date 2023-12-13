ALTER TABLE job_versions_io_mapping ADD COLUMN job_uuid uuid REFERENCES jobs(uuid) ON DELETE CASCADE;
ALTER TABLE job_versions_io_mapping ADD COLUMN job_symlink_target_uuid uuid REFERENCES jobs(uuid) ON DELETE CASCADE;
ALTER TABLE job_versions_io_mapping ADD COLUMN is_current_job_version boolean DEFAULT FALSE;
ALTER TABLE job_versions_io_mapping ADD COLUMN made_current_at TIMESTAMP;

-- To add job_uuid to the unique constraint, we first drop the primary key, then recreate it; note given that job_version_uuid can be NULL, we need to check that job_version_uuid != NULL before inserting (duplicate columns otherwise)
ALTER TABLE job_versions_io_mapping DROP CONSTRAINT job_versions_io_mapping_pkey;
ALTER TABLE job_versions_io_mapping ALTER COLUMN job_version_uuid DROP NOT NULL;

CREATE INDEX job_versions_io_mapping_job_uuid_job_symlink_target_uuid ON job_versions_io_mapping (job_uuid, job_symlink_target_uuid);

ALTER TABLE job_versions_io_mapping ADD CONSTRAINT job_versions_io_mapping_mapping_pkey UNIQUE (job_version_uuid, dataset_uuid, io_type, job_uuid);