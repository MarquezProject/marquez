CREATE INDEX CONCURRENTLY lineage_events_event_time
    on lineage_events(event_time DESC);
