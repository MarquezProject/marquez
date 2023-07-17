create index if not exists run_states_uuid_index on run_states(run_uuid);
create index if not exists parent_run_uuid_index on runs(parent_run_uuid);
create index if not exists start_run_state_uuid_index on runs(start_run_state_uuid);
create index if not exists end_run_state_uuid_index on runs(end_run_state_uuid);
