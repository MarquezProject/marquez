ALTER TABLE runs ADD COLUMN IF NOT EXISTS job_uuid uuid;

CREATE INDEX IF NOT EXISTS runs_job_uuid ON runs(job_uuid, transitioned_at DESC);

-- Trigger updates for records being written by the still-running version of the application
CREATE OR REPLACE FUNCTION write_run_job_uuid()
    RETURNS trigger
    LANGUAGE plpgsql AS
$func$
BEGIN
    NEW.job_uuid := (SELECT uuid FROM jobs j WHERE j.name=OLD.job_name AND j.namespace_name=OLD.namespace_name);
    RETURN NEW;
END
$func$;

DROP TRIGGER IF EXISTS runs_insert_job_uuid ON runs;

CREATE TRIGGER runs_insert_job_uuid
    BEFORE INSERT ON runs
    FOR EACH ROW
    WHEN (NEW.job_uuid IS NULL AND NEW.job_name IS NOT NULL AND NEW.namespace_name IS NOT NULL)
EXECUTE PROCEDURE write_run_job_uuid();

CREATE OR REPLACE VIEW runs_view
AS
SELECT r.uuid,
       r.created_at,
       r.updated_at,
       r.parent_run_uuid,
       job_version_uuid,
       run_args_uuid,
       nominal_start_time,
       nominal_end_time,
       current_run_state,
       start_run_state_uuid,
       end_run_state_uuid,
       external_id,
       location,
       transitioned_at,
       started_at,
       ended_at,
       job_context_uuid,
       job_uuid,
       j.name AS job_name,
       j.namespace_name
FROM runs r
INNER JOIN jobs_view j ON j.uuid = r.job_uuid;
