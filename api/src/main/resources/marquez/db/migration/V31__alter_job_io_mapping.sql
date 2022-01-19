/* SPDX-License-Identifier: Apache-2.0 */

create table job_versions_io_mapping_inputs as select * from job_versions_io_mapping where io_type = 'INPUT';
create table job_versions_io_mapping_outputs as select * from job_versions_io_mapping where io_type = 'OUTPUT';
alter table job_versions_io_mapping_inputs add column job_uuid uuid;
alter table job_versions_io_mapping_outputs add column job_uuid uuid;
update job_versions_io_mapping_outputs set job_uuid = j.job_uuid from job_versions j where job_version_uuid = j.uuid;
update job_versions_io_mapping_inputs set job_uuid = j.job_uuid from job_versions j where job_version_uuid = j.uuid;

create index job_versions_io_mapping_outputs_jv_idx on job_versions_io_mapping_outputs (job_version_uuid) include (dataset_uuid);
create index job_versions_io_mapping_outputs_ds_idx on job_versions_io_mapping_outputs (dataset_uuid) include (job_version_uuid);
create index job_versions_io_mapping_inputs_jv_idx on job_versions_io_mapping_inputs (job_version_uuid) include (dataset_uuid);
create index job_versions_io_mapping_inputs_ds_idx on job_versions_io_mapping_inputs (dataset_uuid) include (job_version_uuid);
