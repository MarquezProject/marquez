-- V75_1__AddJobTableIndexes.sql
/* SPDX-License-Identifier: Apache-2.0 */

-- Indexes for jobs
CREATE INDEX IF NOT EXISTS idx_jobs_namespace_uuid ON jobs (namespace_uuid);
CREATE INDEX IF NOT EXISTS idx_jobs_name ON jobs (name);
CREATE INDEX IF NOT EXISTS idx_jobs_current_version_uuid ON jobs (current_version_uuid);
CREATE INDEX IF NOT EXISTS idx_jobs_aliases ON jobs USING GIN (aliases);

-- Indexes for job_facets
CREATE INDEX IF NOT EXISTS idx_job_facets_job_version_uuid ON job_facets (job_version_uuid);
CREATE INDEX IF NOT EXISTS idx_job_facets_run_uuid ON job_facets (run_uuid);

-- Indexes for runs
CREATE INDEX IF NOT EXISTS idx_runs_job_name_namespace_name ON runs (job_name, namespace_name);
CREATE INDEX IF NOT EXISTS idx_runs_uuid ON runs (uuid);
CREATE INDEX IF NOT EXISTS idx_runs_namespace_name ON runs (namespace_name);
CREATE INDEX IF NOT EXISTS idx_runs_job_name ON runs (job_name);
CREATE INDEX IF NOT EXISTS idx_runs_current_run_state ON runs (current_run_state);

-- Indexes for jobs_tag_mapping
CREATE INDEX IF NOT EXISTS idx_jobs_tag_mapping_job_uuid_tag_uuid ON jobs_tag_mapping (job_uuid, tag_uuid);
CREATE INDEX IF NOT EXISTS idx_jobs_tag_mapping_tag_uuid ON jobs_tag_mapping (tag_uuid);
CREATE INDEX IF NOT EXISTS idx_jobs_tag_mapping_job_uuid ON jobs_tag_mapping (job_uuid);

-- Indexes for tags
CREATE INDEX IF NOT EXISTS idx_tags_uuid ON tags (uuid);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags (name);

-- Indexes for namespaces
CREATE INDEX IF NOT EXISTS idx_namespaces_name ON namespaces (name);

-- Indexes for job_versions
CREATE INDEX IF NOT EXISTS idx_job_versions_uuid ON job_versions (uuid);