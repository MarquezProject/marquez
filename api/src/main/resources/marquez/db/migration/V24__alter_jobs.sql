/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE jobs ADD current_job_context_uuid UUID;
ALTER TABLE jobs ADD current_location varchar;
ALTER TABLE jobs ADD current_inputs JSONB;
ALTER TABLE jobs ADD current_outputs JSONB;

UPDATE jobs SET
  current_job_context_uuid = jv.job_context_uuid,
  current_location = jv.location
FROM job_versions jv
WHERE current_version_uuid = jv.uuid;

UPDATE jobs SET (current_inputs) = (select jsonb_agg(query) from
  (select jv.namespace_name as namespaceName, ds.name as datasetName
   from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
   where m.io_type = 'INPUT' and jobs.uuid = jv.job_uuid) query);

UPDATE jobs SET (current_outputs) = (select jsonb_agg(query) from
  (select jv.namespace_name as namespaceName, ds.name as datasetName
   from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
   where m.io_type = 'OUTPUT' and jobs.uuid = jv.job_uuid) query);