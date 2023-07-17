CREATE INDEX IF NOT EXISTS run_states_uuid_index ON run_states(run_uuid);
CREATE INDEX IF NOT EXISTS runs_parent_run_uuid_index ON runs(parent_run_uuid);
CREATE INDEX IF NOT EXISTS runs_start_run_state_uuid_index ON runs(start_run_state_uuid);
CREATE INDEX IF NOT EXISTS runs_end_run_state_uuid_index ON runs(end_run_state_uuid);
