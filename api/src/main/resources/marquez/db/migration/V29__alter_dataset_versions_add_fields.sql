alter table dataset_versions add column fields jsonb;

UPDATE dataset_versions SET (fields) = (select jsonb_agg((select x from (select distinct f.name, f.type, f.description) as x)) as fields
     from dataset_fields f
     inner join dataset_versions_field_mapping fm on fm.dataset_field_uuid = f.uuid
     where fm.dataset_version_uuid = dataset_versions.uuid
     group by fm.dataset_version_uuid);

alter table dataset_versions ADD COLUMN namespace_name varchar(255);
alter table dataset_versions ADD COLUMN dataset_name varchar(255);

UPDATE dataset_versions SET
    namespace_name = d.namespace_name,
    dataset_name = d.name
FROM datasets d
WHERE d.uuid = dataset_versions.dataset_uuid;

create index dataset_versions_run_uuid on dataset_versions (run_uuid);
create index dataset_versions_name on dataset_versions (dataset_name, namespace_name, created_at DESC);

create unique index dataset_name_index
    on datasets (name, namespace_name);

create index dataset_fields_tag_mapping_tag_index
    on dataset_fields_tag_mapping (tag_uuid);
create index dataset_versions_field_mapping_index
    on dataset_versions_field_mapping (dataset_field_uuid);
create index dataset_fields_name_index
    on dataset_fields (name, dataset_uuid);
create unique index stream_versions_dataset_version_index
    on stream_versions (dataset_version_uuid);
