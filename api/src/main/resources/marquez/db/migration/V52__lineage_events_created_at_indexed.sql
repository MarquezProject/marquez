ALTER TABLE lineage_events ADD created_at TIMESTAMP;

create index lineage_events_created_at_index
    on lineage_events (created_at desc);