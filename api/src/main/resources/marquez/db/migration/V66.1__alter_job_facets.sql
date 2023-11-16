ALTER TABLE job_facets ALTER COLUMN lineage_event_type DROP NOT NULL;
ALTER TABLE job_facets DROP CONSTRAINT job_facets_run_uuid_fkey;
ALTER TABLE job_facets ADD COLUMN job_version_uuid uuid REFERENCES job_versions (uuid);

CREATE INDEX job_facets_job_version_uuid ON job_facets (job_version_uuid);