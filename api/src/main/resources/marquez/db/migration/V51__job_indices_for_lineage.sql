/* SPDX-License-Identifier: Apache-2.0 */

create index jobs_current_version_uuid_index
    on jobs (current_version_uuid);

create index jobs_symlink_target_uuid_index
    on jobs (symlink_target_uuid);

create index jobs_current_job_context_uuid_index
    on jobs (current_job_context_uuid);
