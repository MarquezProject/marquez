ALTER TABLE lineage_events ADD created_at TIMESTAMP;

create index lineage_events_created_at_index on lineage_events (created_at desc);

UPDATE lineage_events
SET created_at = (event_time AT TIME ZONE 'UTC')::timestamp;

ALTER TABLE lineage_events ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'UTC');

