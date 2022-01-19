/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE job_versions ADD namespace_uuid UUID;
ALTER TABLE job_versions ADD namespace_name VARCHAR(255);
ALTER TABLE job_versions ADD job_name VARCHAR(255);

UPDATE job_versions SET
  namespace_uuid = jobs.namespace_uuid,
  namespace_name = jobs.namespace_name,
  job_name = jobs.name
FROM jobs
WHERE job_versions.job_uuid = jobs.uuid;

CREATE INDEX job_versions_selector
    ON job_versions (job_name, namespace_name);