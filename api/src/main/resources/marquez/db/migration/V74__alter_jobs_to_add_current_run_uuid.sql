ALTER TABLE jobs ADD COLUMN current_run_uuid UUID;
CREATE INDEX jobs_current_run_uuid_idx ON jobs(current_run_uuid);

-- Backfill current runs for jobs
WITH latest_runs AS (
  SELECT DISTINCT ON (job_uuid) job_uuid, transitioned_at, uuid AS current_run_uuid
    FROM runs
   ORDER BY job_uuid, transitioned_at DESC, started_at DESC
)
UPDATE jobs AS j
   SET current_run_uuid = lr.current_run_uuid
  FROM latest_runs AS lr
 WHERE j.uuid = lr.job_uuid;
