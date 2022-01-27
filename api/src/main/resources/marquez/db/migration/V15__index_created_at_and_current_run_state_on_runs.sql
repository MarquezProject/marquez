/* SPDX-License-Identifier: Apache-2.0 */

create index runs_created_at_current_run_state_index
    on runs (created_at desc, current_run_state);
