CREATE OR REPLACE VIEW jobs_view
AS
SELECT j.uuid,
       j.name,
       j.namespace_name,
       j.simple_name    AS simple_name,
       j.parent_job_uuid,
       p.name::text AS parent_job_name,
       j.type,
       j.created_at,
       j.updated_at,
       j.namespace_uuid,
       j.description,
       j.current_version_uuid,
       j.current_job_context_uuid,
       j.current_location,
       j.current_inputs,
       j.symlink_target_uuid,
       j.parent_job_uuid::char(36) AS parent_job_uuid_string,
       j.aliases,
       j.current_run_uuid
FROM jobs j
LEFT JOIN jobs p ON j.parent_job_uuid=p.uuid
WHERE j.is_hidden IS FALSE AND j.symlink_target_uuid IS NULL;

CREATE OR REPLACE FUNCTION rewrite_jobs_fqn_table() RETURNS TRIGGER AS
$$
DECLARE
    job_uuid uuid;
    job_updated_at timestamp with time zone;
    new_symlink_target_uuid uuid;
    old_symlink_target_uuid uuid;
    inserted_job jobs_view%rowtype;
    full_name varchar;
BEGIN
    full_name = NEW.name;
    IF NEW.parent_job_uuid IS NOT NULL THEN
        SELECT p.name || '.' || NEW.name INTO full_name
        FROM jobs p
        WHERE p.uuid=NEW.parent_job_uuid;
    END IF;
    INSERT INTO jobs (uuid, type, created_at, updated_at, namespace_uuid, name, simple_name, description,
                      current_version_uuid, namespace_name, current_job_context_uuid,
                      current_location, current_inputs, symlink_target_uuid, parent_job_uuid, current_run_uuid,
                      is_hidden)
    SELECT NEW.uuid,
           NEW.type,
           NEW.created_at,
           NEW.updated_at,
           NEW.namespace_uuid,
           full_name,
           NEW.name,
           NEW.description,
           NEW.current_version_uuid,
           NEW.namespace_name,
           NULL,
           NEW.current_location,
           NEW.current_inputs,
           NEW.symlink_target_uuid,
           NEW.parent_job_uuid,
           NEW.current_run_uuid,
           false
    ON CONFLICT (namespace_uuid, name)
        DO UPDATE SET updated_at               = now(),
                      parent_job_uuid = COALESCE(jobs.parent_job_uuid, EXCLUDED.parent_job_uuid),
                      simple_name = CASE
                            WHEN EXCLUDED.parent_job_uuid IS NOT NULL THEN EXCLUDED.simple_name
                            ELSE jobs.name
                            END,
                      type                     = EXCLUDED.type,
                      description              = EXCLUDED.description,
                      current_location         = EXCLUDED.current_location,
                      current_inputs           = EXCLUDED.current_inputs,
                      -- update the symlink target if null. otherwise, keep the old value
                      symlink_target_uuid      = COALESCE(jobs.symlink_target_uuid,
                                                          EXCLUDED.symlink_target_uuid),
                      current_run_uuid           = EXCLUDED.current_run_uuid,
                      is_hidden                = false
                      -- the SELECT statement below will get the OLD symlink_target_uuid in case of update and the NEW
                      -- version in case of insert
    RETURNING uuid,
        updated_at,
        symlink_target_uuid,
        (SELECT symlink_target_uuid FROM jobs j2 WHERE j2.uuid=jobs.uuid)
        INTO job_uuid, job_updated_at, new_symlink_target_uuid, old_symlink_target_uuid;


    -- update the jobs table when updating a job's symlink target
    IF (new_symlink_target_uuid IS NOT NULL AND new_symlink_target_uuid IS DISTINCT FROM old_symlink_target_uuid) THEN
        RAISE INFO 'Updating jobs aliases and symlinks due to % to job % (%)', TG_OP, NEW.name, job_uuid;
        WITH RECURSIVE
            jobs_symlink AS (SELECT j.uuid, j.uuid AS link_target_uuid, j.symlink_target_uuid
                             FROM jobs j
                             -- include only jobs that have symlinks pointing to them to keep this table small
                             INNER JOIN jobs js ON js.symlink_target_uuid=j.uuid
                             WHERE j.symlink_target_uuid IS NULL
                             UNION
                             SELECT j.uuid, jn.link_target_uuid, j.symlink_target_uuid
                             FROM jobs j
                             INNER JOIN jobs_symlink jn ON j.symlink_target_uuid = jn.uuid),
            aliases AS (SELECT s.link_target_uuid,
                               ARRAY_AGG(DISTINCT f.name) AS aliases
                        FROM jobs_symlink s
                        INNER JOIN jobs f ON f.uuid = s.uuid
                        GROUP BY s.link_target_uuid)
        UPDATE jobs
        SET aliases = j.aliases, symlink_target_uuid=j.link_target_uuid
        FROM (
                 SELECT j.uuid,
                        CASE WHEN j.uuid=s.link_target_uuid THEN NULL ELSE s.link_target_uuid END AS link_target_uuid,
                        a.aliases
                 FROM jobs j
                 LEFT JOIN jobs_symlink s ON s.uuid=j.uuid
                 LEFT JOIN aliases a ON a.link_target_uuid = j.uuid
             ) j
        WHERE jobs.uuid=j.uuid;
        UPDATE job_versions_io_mapping
        SET job_symlink_target_uuid=j.symlink_target_uuid
        FROM jobs j
        WHERE job_versions_io_mapping.job_uuid=j.uuid AND j.uuid = NEW.uuid;
    END IF;
    SELECT * INTO inserted_job FROM jobs_view
    WHERE uuid=job_uuid OR (new_symlink_target_uuid IS NOT NULL AND uuid=new_symlink_target_uuid);
    return inserted_job;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_symlinks ON jobs_view;

CREATE TRIGGER update_symlinks
    INSTEAD OF UPDATE OR INSERT
    ON jobs_view
    FOR EACH ROW
EXECUTE FUNCTION rewrite_jobs_fqn_table();
