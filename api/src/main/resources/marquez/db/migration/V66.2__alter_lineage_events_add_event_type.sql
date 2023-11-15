ALTER TABLE lineage_events ADD COLUMN _event_type VARCHAR(64);
ALTER TABLE lineage_events ALTER COLUMN _event_type SET DEFAULT 'RUN_EVENT';