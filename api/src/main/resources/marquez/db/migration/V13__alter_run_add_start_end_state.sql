/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE runs ADD start_run_state_uuid UUID REFERENCES run_states(uuid);
ALTER TABLE runs ADD end_run_state_uuid UUID REFERENCES run_states(uuid);