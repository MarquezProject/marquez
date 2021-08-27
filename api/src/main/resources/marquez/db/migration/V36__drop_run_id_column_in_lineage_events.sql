DROP TRIGGER lineage_events_run_uuid;
DROP FUNCTION write_run_uuid;
ALTER TABLE lineage_events DROP COLUMN run_id;
