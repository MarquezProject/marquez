create table job_versions_io_mapping_input as select * from job_versions_io_mapping where io_type = 'INPUT';
create table job_versions_io_mapping_output as select * from job_versions_io_mapping where io_type = 'OUTPUT';
alter table job_versions_io_mapping_input add column job_uuid uuid;
alter table job_versions_io_mapping_output add column job_uuid uuid;
update job_versions_io_mapping_output set job_uuid = j.job_uuid from job_versions j where job_version_uuid = j.uuid;
update job_versions_io_mapping_input set job_uuid = j.job_uuid from job_versions j where job_version_uuid = j.uuid;

create index job_versions_io_mapping_output_jv_idx on job_versions_io_mapping_output (job_version_uuid) include (dataset_uuid);
create index job_versions_io_mapping_output_ds_idx on job_versions_io_mapping_output (dataset_uuid) include (job_version_uuid);
create index job_versions_io_mapping_input_jv_idx on job_versions_io_mapping_input (job_version_uuid) include (dataset_uuid);
create index job_versions_io_mapping_input_ds_idx on job_versions_io_mapping_input (dataset_uuid) include (job_version_uuid);
