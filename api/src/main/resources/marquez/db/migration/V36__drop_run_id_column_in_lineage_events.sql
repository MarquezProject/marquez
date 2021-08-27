DROP TRIGGER lineage_events_run_uuid ON lineage_events;
DROP FUNCTION write_run_uuid;
ALTER TABLE lineage_events DROP COLUMN run_id;
