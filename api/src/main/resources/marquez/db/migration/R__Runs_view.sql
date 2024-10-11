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
       COALESCE(s.name, j.name) AS job_name,
       COALESCE(s.namespace_name, j.namespace_name) AS namespace_name
FROM runs r
INNER JOIN jobs j ON j.uuid = r.job_uuid
LEFT JOIN jobs_view s ON j.symlink_target_uuid=s.uuid;
