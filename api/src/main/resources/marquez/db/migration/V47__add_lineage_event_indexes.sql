CREATE INDEX lineage_events_event_time
    on lineage_events(event_time DESC);

CREATE INDEX lineage_events_namespace_event_time
    on lineage_events(job_namespace, event_time DESC);
