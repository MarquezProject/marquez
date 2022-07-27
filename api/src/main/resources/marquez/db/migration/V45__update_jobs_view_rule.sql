ALTER TABLE jobs
    DROP CONSTRAINT IF EXISTS jobs_namespace_uuid_name_key;
DROP INDEX IF EXISTS jobs_namespace_uuid_name_key;

ALTER TABLE jobs DROP CONSTRAINT IF EXISTS jobs_namespace_uuid_name_null_parent;
DROP INDEX IF EXISTS jobs_namespace_uuid_name_null_parent;

DROP INDEX IF EXISTS jobs_namespace_uuid_name_parent;
ALTER TABLE jobs
    DROP CONSTRAINT IF EXISTS unique_jobs_namespace_uuid_name_parent;
DROP INDEX IF EXISTS unique_jobs_namespace_uuid_name_parent;

ALTER TABLE jobs
    ADD COLUMN parent_job_uuid_string char(36) DEFAULT '';
UPDATE jobs
SET parent_job_uuid_string=parent_job_uuid::text
WHERE parent_job_uuid IS NOT NULL;

CREATE UNIQUE INDEX unique_jobs_namespace_uuid_name_parent ON jobs (name, namespace_uuid, parent_job_uuid_string);
ALTER TABLE jobs
    ADD CONSTRAINT unique_jobs_namespace_uuid_name_parent UNIQUE USING INDEX unique_jobs_namespace_uuid_name_parent;

CREATE TABlE jobs_fqn
(
    uuid            uuid    NOT NULL PRIMARY KEY,
    namespace_uuid  uuid    NOT NULL,
    namespace_name  varchar NOT NULL,
    parent_job_name varchar,
    aliases         varchar[],
    job_fqn         varchar NOT NULL
);