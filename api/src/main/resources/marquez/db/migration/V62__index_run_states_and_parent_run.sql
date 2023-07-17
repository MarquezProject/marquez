create index if not exists run_states_uuid_index on run_states(run_uuid);
create index if not exists runs_parent_run_uuid_index on runs(parent_run_uuid);
create index if not exists runs_start_run_state_uuid_index on runs(start_run_state_uuid);
create index if not exists runs_end_run_state_uuid_index on runs(end_run_state_uuid);
