/* SPDX-License-Identifier: Apache-2.0 */

create index runs_created_at_by_name_index
    on runs(job_name, namespace_name, created_at DESC)
    include (uuid, created_at, updated_at, nominal_start_time, nominal_end_time, current_run_state, started_at, ended_at, namespace_name, job_name, location);