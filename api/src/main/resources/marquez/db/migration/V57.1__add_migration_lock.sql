CREATE TABLE IF NOT EXISTS facet_migration_lock (
  created_at  TIMESTAMPTZ,
  run_uuid    UUID
);
