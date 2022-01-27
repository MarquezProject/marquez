/* SPDX-License-Identifier: Apache-2.0 */

UPDATE jobs SET (current_inputs) = (select jsonb_agg(query) from
    (select distinct ds.namespace_name as "namespace", ds.name as "name"
     from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
     where m.io_type = 'INPUT' and jobs.uuid = jv.job_uuid) query);

alter table jobs drop column current_outputs;