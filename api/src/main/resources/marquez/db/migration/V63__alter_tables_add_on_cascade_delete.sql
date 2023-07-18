ALTER TABLE column_lineage
DROP CONSTRAINT column_lineage_output_dataset_version_uuid_fkey,
ADD CONSTRAINT column_lineage_output_dataset_version_uuid_fkey
  FOREIGN KEY (output_dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE column_lineage
DROP CONSTRAINT column_lineage_output_dataset_field_uuid_fkey,
ADD CONSTRAINT column_lineage_output_dataset_field_uuid_fkey
  FOREIGN KEY (output_dataset_field_uuid)
  REFERENCES dataset_fields(uuid)
  ON DELETE CASCADE;

ALTER TABLE column_lineage
DROP CONSTRAINT column_lineage_input_dataset_version_uuid_fkey,
ADD CONSTRAINT column_lineage_input_dataset_version_uuid_fkey
  FOREIGN KEY (input_dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE column_lineage
DROP CONSTRAINT column_lineage_input_dataset_field_uuid_fkey,
ADD CONSTRAINT column_lineage_input_dataset_field_uuid_fkey
  FOREIGN KEY (input_dataset_field_uuid)
  REFERENCES dataset_fields(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_facets
DROP CONSTRAINT dataset_facets_dataset_uuid_fkey,
ADD CONSTRAINT dataset_facets_dataset_uuid_fkey
  FOREIGN KEY (dataset_uuid)
  REFERENCES datasets(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_facets
DROP CONSTRAINT dataset_facets_run_uuid_fkey,
ADD CONSTRAINT dataset_facets_run_uuid_fkey
  FOREIGN KEY (run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_facets
DROP CONSTRAINT dataset_facets_dataset_version_uuid_fkey,
ADD CONSTRAINT dataset_facets_dataset_version_uuid_fkey
  FOREIGN KEY (dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_fields
DROP CONSTRAINT dataset_fields_dataset_uuid_fkey,
ADD CONSTRAINT dataset_fields_dataset_uuid_fkey
  FOREIGN KEY (dataset_uuid)
  REFERENCES datasets(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_fields_tag_mapping
DROP CONSTRAINT dataset_fields_tag_mapping_tag_uuid_fkey,
ADD CONSTRAINT dataset_fields_tag_mapping_tag_uuid_fkey
  FOREIGN KEY (tag_uuid)
  REFERENCES tags(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_fields_tag_mapping
DROP CONSTRAINT dataset_fields_tag_mapping_dataset_field_uuid_fkey,
ADD CONSTRAINT dataset_fields_tag_mapping_dataset_field_uuid_fkey
  FOREIGN KEY (dataset_field_uuid)
  REFERENCES dataset_fields(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_symlinks
DROP CONSTRAINT dataset_symlinks_namespace_uuid_fkey,
ADD CONSTRAINT dataset_symlinks_namespace_uuid_fkey
  FOREIGN KEY (namespace_uuid)
  REFERENCES namespaces(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_versions
DROP CONSTRAINT dataset_versions_dataset_uuid_fkey,
ADD CONSTRAINT dataset_versions_dataset_uuid_fkey
  FOREIGN KEY (dataset_uuid)
  REFERENCES datasets(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_versions_field_mapping
DROP CONSTRAINT dataset_versions_field_mapping_dataset_version_uuid_fkey,
ADD CONSTRAINT dataset_versions_field_mapping_dataset_version_uuid_fkey
  FOREIGN KEY (dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE dataset_versions_field_mapping
DROP CONSTRAINT dataset_versions_field_mapping_dataset_field_uuid_fkey,
ADD CONSTRAINT dataset_versions_field_mapping_dataset_field_uuid_fkey
  FOREIGN KEY (dataset_field_uuid)
  REFERENCES dataset_fields(uuid)
  ON DELETE CASCADE;

ALTER TABLE datasets
DROP CONSTRAINT datasets_namespace_uuid_fkey,
ADD CONSTRAINT datasets_namespace_uuid_fkey
  FOREIGN KEY (namespace_uuid)
  REFERENCES namespaces(uuid)
  ON DELETE CASCADE;

ALTER TABLE datasets
DROP CONSTRAINT datasets_source_uuid_fkey,
ADD CONSTRAINT datasets_source_uuid_fkey
  FOREIGN KEY (source_uuid)
  REFERENCES sources(uuid)
  ON DELETE CASCADE;

ALTER TABLE datasets_tag_mapping
DROP CONSTRAINT datasets_tag_mapping_dataset_uuid_fkey,
ADD CONSTRAINT datasets_tag_mapping_dataset_uuid_fkey
  FOREIGN KEY (dataset_uuid)
  REFERENCES datasets(uuid)
  ON DELETE CASCADE;

ALTER TABLE datasets_tag_mapping
DROP CONSTRAINT datasets_tag_mapping_tag_uuid_fkey,
ADD CONSTRAINT datasets_tag_mapping_tag_uuid_fkey
  FOREIGN KEY (tag_uuid)
  REFERENCES tags(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_facets
DROP CONSTRAINT job_facets_job_uuid_fkey,
ADD CONSTRAINT job_facets_job_uuid_fkey
  FOREIGN KEY (job_uuid)
  REFERENCES jobs(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_facets
DROP CONSTRAINT job_facets_run_uuid_fkey,
ADD CONSTRAINT job_facets_run_uuid_fkey
  FOREIGN KEY (run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_versions
DROP CONSTRAINT job_versions_job_uuid_fkey,
ADD CONSTRAINT job_versions_job_uuid_fkey
  FOREIGN KEY (job_uuid)
  REFERENCES jobs(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_versions
DROP CONSTRAINT job_versions_job_uuid_fkey,
ADD CONSTRAINT job_versions_job_uuid_fkey
  FOREIGN KEY (job_uuid)
  REFERENCES jobs(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_versions_io_mapping
DROP CONSTRAINT job_versions_io_mapping_dataset_uuid_fkey,
ADD CONSTRAINT job_versions_io_mapping_dataset_uuid_fkey
  FOREIGN KEY (dataset_uuid)
  REFERENCES datasets(uuid)
  ON DELETE CASCADE;

ALTER TABLE job_versions_io_mapping
DROP CONSTRAINT job_versions_io_mapping_job_version_uuid_fkey,
ADD CONSTRAINT job_versions_io_mapping_job_version_uuid_fkey
  FOREIGN KEY (job_version_uuid)
  REFERENCES job_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE jobs
DROP CONSTRAINT jobs_parent_fk_jobs,
ADD CONSTRAINT jobs_parent_fk_jobs
  FOREIGN KEY (parent_job_uuid)
  REFERENCES jobs(uuid)
  ON DELETE CASCADE;

ALTER TABLE jobs
DROP CONSTRAINT jobs_symlink_target_uuid_fkey,
ADD CONSTRAINT jobs_symlink_target_uuid_fkey
  FOREIGN KEY (symlink_target_uuid)
  REFERENCES jobs(uuid)
  ON DELETE CASCADE;

ALTER TABLE jobs
DROP CONSTRAINT jobs_namespace_uuid_fkey,
ADD CONSTRAINT jobs_namespace_uuid_fkey
  FOREIGN KEY (namespace_uuid)
  REFERENCES namespaces(uuid)
  ON DELETE CASCADE;

ALTER TABLE namespace_ownerships
DROP CONSTRAINT namespace_ownerships_namespace_uuid_fkey,
ADD CONSTRAINT namespace_ownerships_namespace_uuid_fkey
  FOREIGN KEY (namespace_uuid)
  REFERENCES namespaces(uuid)
  ON DELETE CASCADE;

ALTER TABLE namespace_ownerships
DROP CONSTRAINT namespace_ownerships_owner_uuid_fkey,
ADD CONSTRAINT namespace_ownerships_owner_uuid_fkey
  FOREIGN KEY (owner_uuid)
  REFERENCES owners(uuid)
  ON DELETE CASCADE;

ALTER TABLE run_facets
DROP CONSTRAINT run_facets_run_uuid_fkey,
ADD CONSTRAINT run_facets_run_uuid_fkey
  FOREIGN KEY (run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE run_states
DROP CONSTRAINT run_states_run_uuid_fkey,
ADD CONSTRAINT run_states_run_uuid_fkey
  FOREIGN KEY (run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs
DROP CONSTRAINT runs_start_run_state_uuid_fkey,
ADD CONSTRAINT runs_start_run_state_uuid_fkey
  FOREIGN KEY (start_run_state_uuid)
  REFERENCES run_states(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs
DROP CONSTRAINT runs_parent_fk_runs,
ADD CONSTRAINT runs_parent_fk_runs
  FOREIGN KEY (parent_run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs
DROP CONSTRAINT runs_end_run_state_uuid_fkey,
ADD CONSTRAINT runs_end_run_state_uuid_fkey
  FOREIGN KEY (end_run_state_uuid)
  REFERENCES run_states(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs
DROP CONSTRAINT runs_run_args_uuid_fkey,
ADD CONSTRAINT runs_run_args_uuid_fkey
  FOREIGN KEY (run_args_uuid)
  REFERENCES run_args(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs
DROP CONSTRAINT runs_job_version_uuid_fkey,
ADD CONSTRAINT runs_job_version_uuid_fkey
  FOREIGN KEY (job_version_uuid)
  REFERENCES job_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs_input_mapping
DROP CONSTRAINT runs_input_mapping_dataset_version_uuid_fkey,
ADD CONSTRAINT runs_input_mapping_dataset_version_uuid_fkey
  FOREIGN KEY (dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;

ALTER TABLE runs_input_mapping
DROP CONSTRAINT runs_input_mapping_run_uuid_fkey,
ADD CONSTRAINT runs_input_mapping_run_uuid_fkey
  FOREIGN KEY (run_uuid)
  REFERENCES runs(uuid)
  ON DELETE CASCADE;

ALTER TABLE stream_versions
DROP CONSTRAINT stream_versions_dataset_version_uuid_fkey,
ADD CONSTRAINT stream_versions_dataset_version_uuid_fkey
  FOREIGN KEY (dataset_version_uuid)
  REFERENCES dataset_versions(uuid)
  ON DELETE CASCADE;
