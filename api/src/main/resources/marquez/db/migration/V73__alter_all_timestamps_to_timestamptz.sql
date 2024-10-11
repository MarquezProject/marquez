ALTER TABLE namespaces ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE namespaces ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE sources ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE sources ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

-- Drop view 'datasets_view' before applying time zone to table 'datasets'.
DROP VIEW IF EXISTS datasets_view CASCADE;

ALTER TABLE datasets ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE datasets ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE dataset_fields ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE dataset_fields ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE dataset_versions ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';

-- Drop view 'jobs_view' (and trigger) before applying time zone to table 'jobs'.
DROP TRIGGER IF EXISTS update_symlinks ON jobs_view;
DROP VIEW IF EXISTS jobs_view CASCADE;

ALTER TABLE jobs ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE jobs ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE job_versions ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE job_versions ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE runs ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE runs ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE run_states ALTER COLUMN transitioned_at TYPE TIMESTAMPTZ
USING transitioned_at AT TIME ZONE 'UTC';

ALTER TABLE tags ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE tags ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

