/* SPDX-License-Identifier: Apache-2.0 */

create index jobs_current_version_uuid_index
    on jobs (current_version_uuid) WHERE current_version_uuid IS NOT NULL;

create index jobs_symlink_target_uuid_index
    on jobs (symlink_target_uuid) WHERE symlink_target_uuid IS NOT NULL;

create index jobs_current_job_context_uuid_index
    on jobs (current_job_context_uuid);
