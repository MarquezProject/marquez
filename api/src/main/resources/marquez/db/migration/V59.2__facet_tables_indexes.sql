create index dataset_facets_dataset_uuid_index
    on dataset_facets (dataset_uuid);

create index dataset_facets_dataset_version_uuid_index
    on dataset_facets (dataset_version_uuid);

create index dataset_facets_run_uuid_index
    on dataset_facets (run_uuid);

create index job_facets_job_uuid_index
    on job_facets (job_uuid);

create index job_facets_run_uuid_index
    on job_facets (run_uuid);
