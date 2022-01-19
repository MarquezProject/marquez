/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE runs ADD namespace_name varchar;
ALTER TABLE runs ADD job_name varchar;
ALTER TABLE runs ADD location varchar;

UPDATE runs SET
  namespace_name = jv.namespace_name,
  job_name = jv.job_name,
  location = jv.location
FROM job_versions jv
WHERE jv.uuid = runs.job_version_uuid;

ALTER TABLE runs ADD transitioned_at varchar;
ALTER TABLE runs ADD started_at TIMESTAMP;
ALTER TABLE runs ADD ended_at TIMESTAMP;

UPDATE runs SET
  transitioned_at = updated_at;

UPDATE runs SET
  started_at = rs_s.transitioned_at
FROM run_states rs_s
WHERE rs_s.uuid = start_run_state_uuid;

UPDATE runs SET
  ended_at = rs_e.transitioned_at
FROM run_states rs_e
WHERE rs_e.uuid = end_run_state_uuid;
