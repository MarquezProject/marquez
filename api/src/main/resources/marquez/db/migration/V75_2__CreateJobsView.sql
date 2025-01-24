-- V75_2__CreateJobsView.sql
/* SPDX-License-Identifier: Apache-2.0 */

-- Create or replace the jobs_view without dropping existing columns
CREATE OR REPLACE VIEW jobs_view AS
SELECT
    j.uuid,
    j.name,
    j.namespace_name,
    j.simple_name,
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
    j.parent_job_uuid::character(36) AS parent_job_uuid_string,
    j.aliases,
    j.current_run_uuid
FROM
    jobs j
LEFT JOIN
    jobs p ON j.parent_job_uuid = p.uuid
WHERE
    j.is_hidden IS FALSE
    AND j.symlink_target_uuid IS NULL;