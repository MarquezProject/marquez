create index runs_started_at_current_run_state_index
    on runs (started_at desc, current_run_state);
