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

-- Recreate view 'datasets_view' after applying time zone to table 'datasets'.
CREATE OR REPLACE VIEW  datasets_view AS  SELECT d.uuid,
                                     d.type,
                                     d.created_at,
                                     d.updated_at,
                                     CASE
                                         WHEN d.namespace_name::text = namespaces.name::text AND d.name::text = symlinks.name::text THEN d.namespace_uuid
            ELSE namespaces.uuid
END AS namespace_uuid,
    d.source_uuid,
        CASE
            WHEN d.namespace_name::text = namespaces.name::text AND d.name::text = symlinks.name::text THEN d.name
            ELSE symlinks.name
END AS name,
    ARRAY( SELECT ROW(namespaces.name::character varying(255), symlinks.name::character varying(255))::dataset_name AS "row") AS dataset_symlinks,
    d.physical_name,
    d.description,
    d.current_version_uuid,
    d.last_modified_at,
        CASE
            WHEN d.namespace_name::text = namespaces.name::text AND d.name::text = symlinks.name::text THEN d.namespace_name
            ELSE namespaces.name
END AS namespace_name,
    d.source_name,
    d.is_deleted
   FROM datasets d
     JOIN dataset_symlinks symlinks ON d.uuid = symlinks.dataset_uuid
     JOIN namespaces ON symlinks.namespace_uuid = namespaces.uuid
  WHERE d.is_hidden IS FALSE;


ALTER TABLE dataset_fields ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE dataset_fields ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE dataset_versions ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';

-- Drop view 'jobs_view' before applying time zone to table 'jobs'.
DROP VIEW IF EXISTS jobs_view CASCADE;

ALTER TABLE jobs ALTER COLUMN created_at TYPE TIMESTAMPTZ
USING created_at AT TIME ZONE 'UTC';
ALTER TABLE jobs ALTER COLUMN updated_at TYPE TIMESTAMPTZ
USING updated_at AT TIME ZONE 'UTC';

-- Recreate view 'jobs_view' after applying time zone to table 'jobs'.
CREATE VIEW jobs_view AS  SELECT j.uuid,
                                 j.name,
                                 j.namespace_name,
                                 j.simple_name,
                                 j.parent_job_uuid,
                                 p.name::text AS parent_job_name,
                                  j.type,
                                 j.created_at,
                                 j.updated_at,
                                 j.namespace_uuid,
                                 j.description,
                                 j.current_version_uuid,
                                 j.current_job_context_uuid,
                                 j.current_location,
                                 j.current_inputs,
                                 j.symlink_target_uuid,
                                 j.parent_job_uuid::character(36) AS parent_job_uuid_string,
                                  j.aliases
                          FROM jobs j
                                   LEFT JOIN jobs p ON j.parent_job_uuid = p.uuid
                          WHERE j.is_hidden IS FALSE AND j.symlink_target_uuid IS NULL;

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

