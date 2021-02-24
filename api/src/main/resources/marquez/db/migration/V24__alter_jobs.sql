ALTER TABLE jobs ADD job_context_template_uuid UUID;
ALTER TABLE jobs ADD location_template varchar;
ALTER TABLE jobs ADD inputs_template JSONB;
ALTER TABLE jobs ADD outputs_template JSONB;

UPDATE jobs SET
  job_context_template_uuid = jv.job_context_uuid,
  location_template = jv.location
FROM job_versions jv
WHERE current_version_uuid = jv.uuid;

UPDATE jobs SET (inputs_template) = (select jsonb_agg(query) from
  (select jv.namespace_name as namespaceName, ds.name as datasetName
   from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
   where m.io_type = 'INPUT' and jobs.uuid = jv.job_uuid) query);

UPDATE jobs SET (inputs_template) = (select jsonb_agg(query) from
  (select jv.namespace_name as namespaceName, ds.name as datasetName
   from job_versions_io_mapping m inner join job_versions jv on m.job_version_uuid = jv.uuid inner join datasets ds on m.dataset_uuid = ds.uuid
   where m.io_type = 'OUTPUT' and jobs.uuid = jv.job_uuid) query);