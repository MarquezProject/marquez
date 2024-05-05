ALTER TABLE owners ALTER COLUMN created_at TYPE TIMESTAMPTZ;

ALTER TABLE namespace_ownerships ALTER COLUMN started_at TYPE TIMESTAMPTZ;
ALTER TABLE namespace_ownerships ALTER COLUMN ended_at TYPE TIMESTAMPTZ;

ALTER TABLE namespaces ALTER COLUMN created_at TYPE TIMESTAMPTZ;
ALTER TABLE namespaces ALTER COLUMN updated_at TYPE TIMESTAMPTZ;
ALTER TABLE namespaces DROP COLUMN current_owner_name;

ALTER TABLE lineage_events RENAME COLUMN created_at TO "event_received_time";
ALTER TABLE lineage_events RENAME COLUMN run_uuid TO "run_id";
ALTER TABLE lineage_events RENAME COLUMN job_namespace TO "job_namespace_name";

ALTER TABLE lineage_events ALTER COLUMN event_time TYPE TIMESTAMPTZ;
ALTER TABLE lineage_events ALTER COLUMN event_received_time DROP DEFAULT;
ALTER TABLE lineage_events ALTER COLUMN event_received_time SET NOT NULL;
ALTER TABLE lineage_events ALTER COLUMN event_type TYPE VARCHAR(64);
ALTER TABLE lineage_events ALTER COLUMN _event_type DROP DEFAULT;
ALTER TABLE lineage_events ALTER COLUMN _event_type SET NOT NULL;
ALTER TABLE lineage_events ALTER COLUMN job_name TYPE VARCHAR;
ALTER TABLE lineage_events ALTER COLUMN job_namespace_name TYPE VARCHAR(255);
ALTER TABLE lineage_events ALTER COLUMN producer TYPE VARCHAR(255);

ALTER TABLE jobs DROP COLUMN current_job_context_uuid CASCADE;
ALTER TABLE jobs DROP COLUMN current_inputs;
ALTER TABLE jobs DROP COLUMN parent_job_uuid_string;
ALTER TABLE jobs DROP COLUMN simple_name;
ALTER TABLE jobs RENAME COLUMN current_location TO "location";
ALTER TABLE jobs DROP CONSTRAINT IF EXISTS unique_jobs_namespace_uuid_name_parent;
ALTER TABLE jobs ADD UNIQUE (namespace_name, name);
ALTER TABLE jobs ADD current_run_uuid UUID;
ALTER TABLE jobs ADD display_name VARCHAR(255);

ALTER TABLE runs DROP COLUMN run_args_uuid;
ALTER TABLE runs DROP COLUMN job_context_uuid;
ALTER TABLE runs DROP COLUMN parent_run_uuid;
ALTER TABLE runs DROP COLUMN start_run_state_uuid;
ALTER TABLE runs DROP COLUMN end_run_state_uuid;
ALTER TABLE runs DROP COLUMN created_at;
ALTER TABLE runs DROP COLUMN updated_at;
ALTER TABLE runs DROP COLUMN location;
ALTER TABLE runs DROP COLUMN job_uuid CASCADE;
ALTER TABLE runs DROP COLUMN job_name;
ALTER TABLE runs DROP COLUMN namespace_name;
ALTER TABLE runs RENAME COLUMN external_id TO "external_run_id";

CREATE INDEX IF NOT EXISTS run_states_state_idx ON run_states(state);
ALTER TABLE run_states ADD UNIQUE (run_uuid, state);

ALTER TABLE job_versions DROP COLUMN version;
ALTER TABLE job_versions DROP COLUMN job_context_uuid CASCADE;
ALTER TABLE job_versions ADD CONSTRAINT namespaces_pkey FOREIGN KEY (namespace_uuid) REFERENCES namespaces(uuid);
ALTER TABLE job_versions RENAME COLUMN latest_run_uuid TO "current_run_uuid";
ALTER TABLE job_versions RENAME COLUMN location TO "job_location";

ALTER TABLE datasets DROP COLUMN physical_name;
ALTER TABLE datasets ADD display_name VARCHAR(255);

ALTER TABLE dataset_versions DROP COLUMN version;
ALTER TABLE dataset_versions DROP COLUMN fields;
ALTER TABLE dataset_versions DROP COLUMN lifecycle_state;
ALTER TABLE dataset_versions ADD facets JSONB;

ALTER TABLE sources ADD display_name VARCHAR(255);

DROP INDEX IF EXISTS lineage_events_run_id_index;
CREATE INDEX IF NOT EXISTS lineage_events_run_uuid_idx ON lineage_events(run_id);

DROP TABLE IF EXISTS stream_versions;
DROP TABLE IF EXISTS run_args;

DROP VIEW IF EXISTS dataset_facets_view;
DROP VIEW IF EXISTS job_facets_view;
DROP VIEW IF EXISTS run_facets_view;

DROP TABLE IF EXISTS dataset_facets;
DROP TABLE IF EXISTS job_facets;
DROP TABLE IF EXISTS run_facets;
DROP TABLE IF EXISTS facet_migration_lock;

DROP TRIGGER IF EXISTS runs_insert_job_uuid ON runs;
