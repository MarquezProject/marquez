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
    ADD COLUMN parent_job_id_string char(36) DEFAULT '';
UPDATE jobs
SET parent_job_id_string=parent_job_uuid::text
WHERE parent_job_uuid IS NOT NULL;

CREATE UNIQUE INDEX unique_jobs_namespace_uuid_name_parent ON jobs (name, namespace_uuid, parent_job_id_string);
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

CREATE OR REPLACE VIEW jobs_view
AS
SELECT f.uuid,
       f.job_fqn AS name,
       f.namespace_name,
       j.name    AS simple_name,
       j.parent_job_uuid,
       f.parent_job_name::text,
       j.type,
       j.created_at,
       j.updated_at,
       f.namespace_uuid,
       j.description,
       j.current_version_uuid,
       j.current_job_context_uuid,
       j.current_location,
       j.current_inputs,
       j.symlink_target_uuid,
       j.parent_job_id_string,
       f.aliases
FROM jobs_fqn f,
     jobs j
WHERE j.uuid = f.uuid;


CREATE OR REPLACE FUNCTION rewrite_jobs_fqn_table() RETURNS TRIGGER AS
$$
    DECLARE
        id uuid;
        inserted jobs_view%rowtype;
BEGIN
    INSERT INTO jobs (uuid, type, created_at, updated_at, namespace_uuid, name, description,
                      current_version_uuid, namespace_name, current_job_context_uuid,
                      current_location, current_inputs, symlink_target_uuid, parent_job_uuid,
                      parent_job_id_string)
    SELECT NEW.uuid,
           NEW.type,
           NEW.created_at,
           NEW.updated_at,
           NEW.namespace_uuid,
           NEW.name,
           NEW.description,
           NEW.current_version_uuid,
           NEW.namespace_name,
           NEW.current_job_context_uuid,
           NEW.current_location,
           NEW.current_inputs,
           NEW.symlink_target_uuid,
           NEW.parent_job_uuid,
           COALESCE(NEW.parent_job_uuid::char(36), '')
    ON CONFLICT (name, namespace_uuid, parent_job_id_string)
        DO UPDATE SET updated_at               = EXCLUDED.updated_at,
                      type                     = EXCLUDED.type,
                      description              = EXCLUDED.description,
                      current_job_context_uuid = EXCLUDED.current_job_context_uuid,
                      current_location         = EXCLUDED.current_location,
                      current_inputs           = EXCLUDED.current_inputs,
                      -- update the symlink target if not null. otherwise, keep the old value
                      symlink_target_uuid      = COALESCE(
                              EXCLUDED.symlink_target_uuid,
                              jobs.symlink_target_uuid)
    RETURNING uuid INTO id;
    IF TG_OP = 'INSERT' OR
       (TG_OP = 'UPDATE' AND OLD.symlink_target_uuid IS DISTINCT FROM NEW.symlink_target_uuid) THEN
        WITH RECURSIVE
            jobs_symlink AS (SELECT uuid, uuid AS link_target_uuid, symlink_target_uuid
                             FROM jobs j
                             WHERE symlink_target_uuid IS NULL
                             UNION
                             SELECT j.uuid, jn.link_target_uuid, j.symlink_target_uuid
                             FROM jobs j
                                      INNER JOIN jobs_symlink jn ON j.symlink_target_uuid = jn.uuid),
            fqn AS (SELECT j.uuid,
                           CASE
                               WHEN j.parent_job_uuid IS NULL THEN j.name
                               ELSE jf.job_fqn || '.' || j.name
                               END AS name,
                           j.namespace_uuid,
                           j.namespace_name,
                           jf.job_fqn AS parent_job_name,
                           j.parent_job_uuid
                    FROM jobs j
                             LEFT JOIN jobs_fqn jf ON jf.uuid=j.parent_job_uuid
                             LEFT JOIN jobs_symlink js ON js.link_target_uuid=j.uuid
                    WHERE j.uuid=id OR j.symlink_target_uuid=id OR js.uuid=id
                    UNION
                    SELECT j1.uuid,
                           f.name || '.' || j1.name AS name,
                           f.namespace_uuid         AS namespace_uuid,
                           f.namespace_name         AS namespace_name,
                           f.name                   AS parent_job_name,
                           j1.parent_job_uuid
                    FROM jobs j1
                             INNER JOIN fqn f ON f.uuid = j1.parent_job_uuid),
            aliases AS (SELECT s.link_target_uuid,
                               ARRAY_AGG(DISTINCT f.name) FILTER (WHERE f.name IS NOT NULL) AS aliases
                        FROM jobs_symlink s
                        INNER JOIN fqn f ON f.uuid = s.uuid
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
    END IF;
    SELECT * INTO inserted FROM jobs_view WHERE uuid=id;
    return inserted;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_symlinks
    INSTEAD OF UPDATE OR INSERT
    ON jobs_view
    FOR EACH ROW
EXECUTE FUNCTION rewrite_jobs_fqn_table();