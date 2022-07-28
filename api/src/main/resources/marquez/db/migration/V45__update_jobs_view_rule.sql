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

WITH RECURSIVE
    jobs_symlink AS (SELECT uuid, uuid AS link_target_uuid, symlink_target_uuid
                     FROM jobs j
                     WHERE symlink_target_uuid IS NULL
                     UNION
                     SELECT j.uuid, jn.link_target_uuid, j.symlink_target_uuid
                     FROM jobs j
                              INNER JOIN jobs_symlink jn ON j.symlink_target_uuid = jn.uuid),
    fqn AS (SELECT j.uuid,
                   j.name AS name,
                   j.namespace_uuid,
                   j.namespace_name,
                   NULL::text AS parent_job_name,
                   j.parent_job_uuid
            FROM jobs j
            WHERE parent_job_uuid IS NULL
            UNION
            SELECT j1.uuid,
                   f.name || '.' || j1.name AS name,
                   f.namespace_uuid         AS namespace_uuid,
                   f.namespace_name         AS namespace_name,
                   f.name                   AS parent_job_name,
                   j1.parent_job_uuid
            FROM jobs j1
                     INNER JOIN fqn f ON j1.parent_job_uuid = f.uuid),
    aliases AS (SELECT s.link_target_uuid,
                       ARRAY_AGG(DISTINCT f.name) FILTER (WHERE f.name IS NOT NULL) AS aliases
                FROM jobs_symlink s
                INNER JOIN fqn f ON f.uuid = s.uuid
                WHERE s.link_target_uuid != s.uuid
                GROUP BY s.link_target_uuid)
INSERT
INTO jobs_fqn
SELECT j.uuid,
       jf.namespace_uuid,
       jf.namespace_name,
       jf.parent_job_name,
       a.aliases,
       jf.name AS job_fqn
FROM jobs j
         LEFT JOIN jobs_symlink js ON j.uuid = js.uuid
         LEFT JOIN aliases a ON a.link_target_uuid = js.link_target_uuid
         INNER JOIN fqn jf ON jf.uuid = COALESCE(js.link_target_uuid, j.uuid)
ON CONFLICT (uuid) DO UPDATE
    SET job_fqn=EXCLUDED.job_fqn,
        aliases = jobs_fqn.aliases || EXCLUDED.aliases;