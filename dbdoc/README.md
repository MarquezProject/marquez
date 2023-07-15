# marquez

## Tables

| Name | Columns | Comment | Type |
| ---- | ------- | ------- | ---- |
| [public.flyway_schema_history](public.flyway_schema_history.md) | 10 |  | BASE TABLE |
| [public.namespaces](public.namespaces.md) | 7 |  | BASE TABLE |
| [public.owners](public.owners.md) | 3 |  | BASE TABLE |
| [public.namespace_ownerships](public.namespace_ownerships.md) | 5 |  | BASE TABLE |
| [public.sources](public.sources.md) | 7 |  | BASE TABLE |
| [public.datasets](public.datasets.md) | 15 |  | BASE TABLE |
| [public.jobs](public.jobs.md) | 18 |  | BASE TABLE |
| [public.job_versions](public.job_versions.md) | 11 |  | BASE TABLE |
| [public.job_versions_io_mapping](public.job_versions_io_mapping.md) | 3 |  | BASE TABLE |
| [public.run_args](public.run_args.md) | 4 |  | BASE TABLE |
| [public.runs](public.runs.md) | 20 |  | BASE TABLE |
| [public.run_states](public.run_states.md) | 4 |  | BASE TABLE |
| [public.dataset_versions](public.dataset_versions.md) | 9 |  | BASE TABLE |
| [public.stream_versions](public.stream_versions.md) | 2 |  | BASE TABLE |
| [public.runs_input_mapping](public.runs_input_mapping.md) | 2 |  | BASE TABLE |
| [public.job_contexts](public.job_contexts.md) | 4 |  | BASE TABLE |
| [public.dataset_fields](public.dataset_fields.md) | 7 |  | BASE TABLE |
| [public.dataset_versions_field_mapping](public.dataset_versions_field_mapping.md) | 2 |  | BASE TABLE |
| [public.tags](public.tags.md) | 5 |  | BASE TABLE |
| [public.datasets_tag_mapping](public.datasets_tag_mapping.md) | 3 |  | BASE TABLE |
| [public.dataset_fields_tag_mapping](public.dataset_fields_tag_mapping.md) | 3 |  | BASE TABLE |
| [public.lineage_events](public.lineage_events.md) | 8 |  | BASE TABLE |
| [public.jobs_view](public.jobs_view.md) | 18 |  | VIEW |
| [public.runs_view](public.runs_view.md) | 20 |  | VIEW |
| [public.jobs_fqn](public.jobs_fqn.md) | 6 |  | BASE TABLE |
| [public.dataset_symlinks](public.dataset_symlinks.md) | 7 |  | BASE TABLE |
| [public.column_lineage](public.column_lineage.md) | 8 |  | BASE TABLE |
| [public.dataset_facets](public.dataset_facets.md) | 9 |  | BASE TABLE |
| [public.job_facets](public.job_facets.md) | 7 |  | BASE TABLE |
| [public.run_facets](public.run_facets.md) | 6 |  | BASE TABLE |
| [public.facet_migration_lock](public.facet_migration_lock.md) | 2 |  | BASE TABLE |
| [public.run_facets_view](public.run_facets_view.md) | 6 |  | VIEW |
| [public.job_facets_view](public.job_facets_view.md) | 7 |  | VIEW |
| [public.dataset_facets_view](public.dataset_facets_view.md) | 9 |  | VIEW |
| [public.datasets_view](public.datasets_view.md) | 15 |  | VIEW |

## Stored procedures and functions

| Name | ReturnType | Arguments | Type |
| ---- | ------- | ------- | ---- |
| public.write_run_job_uuid | trigger |  | FUNCTION |
| public.rewrite_jobs_fqn_table | trigger |  | FUNCTION |

## Relations

```mermaid
erDiagram

"public.namespace_ownerships" }o--o| "public.namespaces" : "FOREIGN KEY (namespace_uuid) REFERENCES namespaces(uuid)"
"public.namespace_ownerships" }o--o| "public.owners" : "FOREIGN KEY (owner_uuid) REFERENCES owners(uuid)"
"public.datasets" }o--o| "public.namespaces" : "FOREIGN KEY (namespace_uuid) REFERENCES namespaces(uuid)"
"public.datasets" }o--o| "public.sources" : "FOREIGN KEY (source_uuid) REFERENCES sources(uuid)"
"public.jobs" }o--o| "public.namespaces" : "FOREIGN KEY (namespace_uuid) REFERENCES namespaces(uuid)"
"public.jobs" }o--o| "public.jobs" : "FOREIGN KEY (parent_job_uuid) REFERENCES jobs(uuid)"
"public.jobs" }o--o| "public.jobs" : "FOREIGN KEY (symlink_target_uuid) REFERENCES jobs(uuid)"
"public.job_versions" }o--o| "public.jobs" : "FOREIGN KEY (job_uuid) REFERENCES jobs(uuid)"
"public.job_versions_io_mapping" }o--|| "public.datasets" : "FOREIGN KEY (dataset_uuid) REFERENCES datasets(uuid)"
"public.job_versions_io_mapping" }o--|| "public.job_versions" : "FOREIGN KEY (job_version_uuid) REFERENCES job_versions(uuid)"
"public.runs" }o--o| "public.job_versions" : "FOREIGN KEY (job_version_uuid) REFERENCES job_versions(uuid)"
"public.runs" }o--o| "public.run_args" : "FOREIGN KEY (run_args_uuid) REFERENCES run_args(uuid)"
"public.runs" }o--o| "public.runs" : "FOREIGN KEY (parent_run_uuid) REFERENCES runs(uuid)"
"public.runs" }o--o| "public.run_states" : "FOREIGN KEY (end_run_state_uuid) REFERENCES run_states(uuid)"
"public.runs" }o--o| "public.run_states" : "FOREIGN KEY (start_run_state_uuid) REFERENCES run_states(uuid)"
"public.run_states" }o--o| "public.runs" : "FOREIGN KEY (run_uuid) REFERENCES runs(uuid)"
"public.dataset_versions" }o--o| "public.datasets" : "FOREIGN KEY (dataset_uuid) REFERENCES datasets(uuid)"
"public.stream_versions" }o--o| "public.dataset_versions" : "FOREIGN KEY (dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.runs_input_mapping" }o--|| "public.runs" : "FOREIGN KEY (run_uuid) REFERENCES runs(uuid)"
"public.runs_input_mapping" }o--|| "public.dataset_versions" : "FOREIGN KEY (dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.dataset_fields" }o--o| "public.datasets" : "FOREIGN KEY (dataset_uuid) REFERENCES datasets(uuid)"
"public.dataset_versions_field_mapping" }o--|| "public.dataset_versions" : "FOREIGN KEY (dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.dataset_versions_field_mapping" }o--|| "public.dataset_fields" : "FOREIGN KEY (dataset_field_uuid) REFERENCES dataset_fields(uuid)"
"public.datasets_tag_mapping" }o--|| "public.datasets" : "FOREIGN KEY (dataset_uuid) REFERENCES datasets(uuid)"
"public.datasets_tag_mapping" }o--|| "public.tags" : "FOREIGN KEY (tag_uuid) REFERENCES tags(uuid)"
"public.dataset_fields_tag_mapping" }o--|| "public.dataset_fields" : "FOREIGN KEY (dataset_field_uuid) REFERENCES dataset_fields(uuid)"
"public.dataset_fields_tag_mapping" }o--|| "public.tags" : "FOREIGN KEY (tag_uuid) REFERENCES tags(uuid)"
"public.dataset_symlinks" }o--o| "public.namespaces" : "FOREIGN KEY (namespace_uuid) REFERENCES namespaces(uuid)"
"public.column_lineage" }o--o| "public.dataset_versions" : "FOREIGN KEY (input_dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.column_lineage" }o--o| "public.dataset_versions" : "FOREIGN KEY (output_dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.column_lineage" }o--o| "public.dataset_fields" : "FOREIGN KEY (input_dataset_field_uuid) REFERENCES dataset_fields(uuid)"
"public.column_lineage" }o--o| "public.dataset_fields" : "FOREIGN KEY (output_dataset_field_uuid) REFERENCES dataset_fields(uuid)"
"public.dataset_facets" }o--o| "public.datasets" : "FOREIGN KEY (dataset_uuid) REFERENCES datasets(uuid)"
"public.dataset_facets" }o--o| "public.runs" : "FOREIGN KEY (run_uuid) REFERENCES runs(uuid)"
"public.dataset_facets" }o--o| "public.dataset_versions" : "FOREIGN KEY (dataset_version_uuid) REFERENCES dataset_versions(uuid)"
"public.job_facets" }o--o| "public.jobs" : "FOREIGN KEY (job_uuid) REFERENCES jobs(uuid)"
"public.job_facets" }o--o| "public.runs" : "FOREIGN KEY (run_uuid) REFERENCES runs(uuid)"
"public.run_facets" }o--o| "public.runs" : "FOREIGN KEY (run_uuid) REFERENCES runs(uuid)"

"public.flyway_schema_history" {
  integer installed_rank
  varchar_50_ version
  varchar_200_ description
  varchar_20_ type
  varchar_1000_ script
  integer checksum
  varchar_100_ installed_by
  timestamp_without_time_zone installed_on
  integer execution_time
  boolean success
}
"public.namespaces" {
  uuid uuid
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  varchar name
  text description
  varchar_64_ current_owner_name
  boolean is_hidden
}
"public.owners" {
  uuid uuid
  timestamp_without_time_zone created_at
  varchar_64_ name
}
"public.namespace_ownerships" {
  uuid uuid
  timestamp_without_time_zone started_at
  timestamp_without_time_zone ended_at
  uuid namespace_uuid FK
  uuid owner_uuid FK
}
"public.sources" {
  uuid uuid
  varchar_64_ type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  varchar name
  varchar connection_url
  text description
}
"public.datasets" {
  uuid uuid
  varchar_64_ type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid namespace_uuid FK
  uuid source_uuid FK
  varchar name
  varchar physical_name
  text description
  uuid current_version_uuid
  timestamp_without_time_zone last_modified_at
  varchar namespace_name
  varchar source_name
  boolean is_deleted
  boolean is_hidden
}
"public.jobs" {
  uuid uuid
  varchar_64_ type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid namespace_uuid FK
  varchar simple_name
  text description
  uuid current_version_uuid
  varchar namespace_name
  uuid current_job_context_uuid
  varchar current_location
  jsonb current_inputs
  uuid symlink_target_uuid FK
  uuid parent_job_uuid FK
  character_36_ parent_job_uuid_string
  boolean is_hidden
  varchar name
  varchar__ aliases
}
"public.job_versions" {
  uuid uuid
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid job_uuid FK
  uuid version
  varchar_255_ location
  uuid latest_run_uuid
  uuid job_context_uuid
  uuid namespace_uuid
  varchar namespace_name
  varchar job_name
}
"public.job_versions_io_mapping" {
  uuid job_version_uuid FK
  uuid dataset_uuid FK
  varchar_64_ io_type
}
"public.run_args" {
  uuid uuid
  timestamp_without_time_zone created_at
  text args
  varchar_255_ checksum
}
"public.runs" {
  uuid uuid
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid job_version_uuid FK
  uuid run_args_uuid FK
  timestamp_without_time_zone nominal_start_time
  timestamp_without_time_zone nominal_end_time
  varchar_64_ current_run_state
  uuid start_run_state_uuid FK
  uuid end_run_state_uuid FK
  varchar external_id
  varchar namespace_name
  varchar job_name
  varchar location
  timestamp_without_time_zone transitioned_at
  timestamp_without_time_zone started_at
  timestamp_without_time_zone ended_at
  uuid job_context_uuid
  uuid parent_run_uuid FK
  uuid job_uuid
}
"public.run_states" {
  uuid uuid
  timestamp_without_time_zone transitioned_at
  uuid run_uuid FK
  varchar_64_ state
}
"public.dataset_versions" {
  uuid uuid
  timestamp_without_time_zone created_at
  uuid dataset_uuid FK
  uuid version
  uuid run_uuid
  jsonb fields
  varchar namespace_name
  varchar dataset_name
  varchar_63_ lifecycle_state
}
"public.stream_versions" {
  uuid dataset_version_uuid FK
  varchar_255_ schema_location
}
"public.runs_input_mapping" {
  uuid run_uuid FK
  uuid dataset_version_uuid FK
}
"public.job_contexts" {
  uuid uuid
  timestamp_without_time_zone created_at
  text context
  varchar_255_ checksum
}
"public.dataset_fields" {
  uuid uuid
  text type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid dataset_uuid FK
  varchar_255_ name
  text description
}
"public.dataset_versions_field_mapping" {
  uuid dataset_version_uuid FK
  uuid dataset_field_uuid FK
}
"public.tags" {
  uuid uuid
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  varchar name
  text description
}
"public.datasets_tag_mapping" {
  uuid dataset_uuid FK
  uuid tag_uuid FK
  timestamp_without_time_zone tagged_at
}
"public.dataset_fields_tag_mapping" {
  uuid dataset_field_uuid FK
  uuid tag_uuid FK
  timestamp_without_time_zone tagged_at
}
"public.lineage_events" {
  timestamp_with_time_zone event_time
  jsonb event
  text event_type
  text job_name
  text job_namespace
  text producer
  uuid run_uuid
  timestamp_with_time_zone created_at
}
"public.jobs_view" {
  uuid uuid
  varchar name
  varchar namespace_name
  varchar simple_name
  uuid parent_job_uuid
  text parent_job_name
  varchar_64_ type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid namespace_uuid
  text description
  uuid current_version_uuid
  uuid current_job_context_uuid
  varchar current_location
  jsonb current_inputs
  uuid symlink_target_uuid
  character_36_ parent_job_uuid_string
  varchar__ aliases
}
"public.runs_view" {
  uuid uuid
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid parent_run_uuid
  uuid job_version_uuid
  uuid run_args_uuid
  timestamp_without_time_zone nominal_start_time
  timestamp_without_time_zone nominal_end_time
  varchar_64_ current_run_state
  uuid start_run_state_uuid
  uuid end_run_state_uuid
  varchar external_id
  varchar location
  timestamp_without_time_zone transitioned_at
  timestamp_without_time_zone started_at
  timestamp_without_time_zone ended_at
  uuid job_context_uuid
  uuid job_uuid
  varchar job_name
  varchar namespace_name
}
"public.jobs_fqn" {
  uuid uuid
  uuid namespace_uuid
  varchar namespace_name
  varchar parent_job_name
  varchar__ aliases
  varchar job_fqn
}
"public.dataset_symlinks" {
  uuid dataset_uuid
  varchar name
  uuid namespace_uuid FK
  varchar_64_ type
  boolean is_primary
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
}
"public.column_lineage" {
  uuid output_dataset_version_uuid FK
  uuid output_dataset_field_uuid FK
  uuid input_dataset_version_uuid FK
  uuid input_dataset_field_uuid FK
  text transformation_description
  varchar_255_ transformation_type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
}
"public.dataset_facets" {
  timestamp_with_time_zone created_at
  uuid dataset_uuid FK
  uuid dataset_version_uuid FK
  uuid run_uuid FK
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_64_ type
  varchar_255_ name
  jsonb facet
}
"public.job_facets" {
  timestamp_with_time_zone created_at
  uuid job_uuid FK
  uuid run_uuid FK
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_255_ name
  jsonb facet
}
"public.run_facets" {
  timestamp_with_time_zone created_at
  uuid run_uuid FK
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_255_ name
  jsonb facet
}
"public.facet_migration_lock" {
  timestamp_with_time_zone created_at
  uuid run_uuid
}
"public.run_facets_view" {
  timestamp_with_time_zone created_at
  uuid run_uuid
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_255_ name
  jsonb facet
}
"public.job_facets_view" {
  timestamp_with_time_zone created_at
  uuid job_uuid
  uuid run_uuid
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_255_ name
  jsonb facet
}
"public.dataset_facets_view" {
  timestamp_with_time_zone created_at
  uuid dataset_uuid
  uuid dataset_version_uuid
  uuid run_uuid
  timestamp_with_time_zone lineage_event_time
  varchar_64_ lineage_event_type
  varchar_64_ type
  varchar_255_ name
  jsonb facet
}
"public.datasets_view" {
  uuid uuid
  varchar_64_ type
  timestamp_without_time_zone created_at
  timestamp_without_time_zone updated_at
  uuid namespace_uuid
  uuid source_uuid
  varchar name
  dataset_name__ dataset_symlinks
  varchar physical_name
  text description
  uuid current_version_uuid
  timestamp_without_time_zone last_modified_at
  varchar namespace_name
  varchar source_name
  boolean is_deleted
}
```

---

> Generated by [tbls](https://github.com/k1LoW/tbls)
