/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE runs ADD job_context_uuid uuid;

UPDATE runs SET
    job_context_uuid = jv.job_context_uuid
FROM job_versions jv
WHERE jv.uuid = runs.job_version_uuid;