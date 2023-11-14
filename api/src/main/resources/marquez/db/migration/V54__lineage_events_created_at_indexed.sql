-- new column will be created with 'null' filled for existing rows
ALTER TABLE lineage_events ADD created_at TIMESTAMP WITH TIME ZONE;

create index lineage_events_created_at_index on lineage_events (created_at desc NULLS LAST);

-- The new default set to UTC now() will only apply in subsequent INSERT or UPDATE commands; it does not cause rows already in the table to change.
ALTER TABLE lineage_events ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'UTC')::timestamptz;
