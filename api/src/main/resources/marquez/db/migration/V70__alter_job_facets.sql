ALTER TABLE job_facets
    DROP CONSTRAINT IF EXISTS job_facets_job_version_uuid_fkey,
    ADD CONSTRAINT job_facets_job_version_uuid_fkey
        FOREIGN KEY (job_version_uuid)
        REFERENCES job_versions(uuid)
        ON DELETE CASCADE;
