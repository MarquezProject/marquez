create index run_states_uuid_index on run_states(run_uuid);
create index parent_run_uuid_index on runs(parent_run_uuid);

create index start_run_state_uuid_index on runs(start_run_state_uuid);
create index end_run_state_uuid_index on runs(end_run_state_uuid);