-- It's required to support streaming CDC
DO $$ 
DECLARE
  table_name text;
BEGIN
  FOR table_name IN 
    SELECT tablename 
    FROM pg_tables 
    WHERE schemaname = 'public' AND tablename IN (
      'column_lineage', 'lineage_events', 'datasets', 'dataset_versions', 
      'dataset_fields', 'jobs', 'job_versions', 'job_facets', 
      'job_versions_io_mapping', 'job_versions_io_mapping_inputs', 
      'job_versions_io_mapping_outputs'
    )
  LOOP
    IF (SELECT relreplident FROM pg_class WHERE relname = table_name) != 'f' THEN 
      EXECUTE format('ALTER TABLE %I REPLICA IDENTITY FULL;', table_name);
    END IF;
  END LOOP;
END $$;