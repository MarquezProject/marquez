package marquez.db.v2;

/** ... */
public interface Sql {
  /* ... */
  String WRITE_LINEAGE_EVENT =
      """
      INSERT INTO lineage_events (
        run_transitioned_at,
        event,
        run_state,
        job_name,
        job_namespace_name,
        producer,
        run_id,
        event_received_time,
        _event_type
      ) VALUES (
        '<run_transitioned_at>', -- replace with the actual event_time value
        '<event>'::jsonb,        -- replace with the actual event value (should be a JSONB object)
        '<run_state>',           -- replace with the actual event_type value
        '<job_name>',            -- replace with the actual job_name value
        '<job_namespace_name>',  -- replace with the actual job_namespace value
        '<producer>',            -- replace with the actual producer value
        '<run_id>',              -- replace with the actual run_uuid value
        '<event_received_time>', -- replace with the actual _event_type value (optional, as it has a default)
        '<_event_type>'          -- replace with the actual event_time value
      )
      """;

  /* ... */
  String WRITE_JOB_META =
      """
      WITH namespace AS (
        INSERT INTO namespaces (
          uuid,
          created_at,
          updated_at,
          name,
          description
        ) VALUES (
          '<job_namespace_uuid>',                   -- replace with the actual event_time value
          '<created_at>',                           -- replace with the actual event_time value
          '<updated_at>',                           -- replace with the actual event_time value
          '<job_namespace_name>',                   -- replace with the actual event_time value
          NULLIF('<job_namespace_description>', '') -- replace with the actual event_time value
        )
        ON CONFLICT (name)
        DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                      description = COALESCE(NULLIF(EXCLUDED.description, ''), namespaces.description)
        )
        INSERT INTO jobs (
          uuid,
          type,
          created_at,
          updated_at,
          namespace_uuid,
          description,
          namespace_name,
          location,
          name
        ) VALUES (
          '<job_uuid>',                    -- replace with the actual event_time value
          '<job_type>',                    -- replace with the actual event_time value
          '<created_at>',                  -- replace with the actual event_time value
          '<updated_at>',                  -- replace with the actual event_time value
          '<job_namespace_uuid>',          -- replace with the actual event_time value
          NULLIF('<job_description>', ''), -- replace with the actual event_time value
          '<job_namespace_name>',          -- replace with the actual event_time value
          NULLIF('<job_location>', ''),    -- replace with the actual event_time value
          '<job_name>'                     -- replace with the actual event_time value
        ) ON CONFLICT (namespace_name, name)
          DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                        description = COALESCE(NULLIF(EXCLUDED.description, ''), jobs.description,
                        location    = COALESCE(NULLIF(EXCLUDED.location, ''), jobs.location
        """;

  /* ... */
  String WRITE_JOB_VERSION_META =
      """
      WITH job AS (
        SELECT uuid, namespace_uuid, namespace_name, name, location
          FROM jobs
         WHERE name = '<job_name>'
      ),
      job_version AS (
        INSERT INTO job_versions (
          uuid,
          created_at,
          updated_at,
          job_uuid,
          job_location,
          namespace_uuid,
          namespace_name,
          job_name
        )
        SELECT
          '<job_version_uuid>', -- replace with the actual event_time value
          '<created_at>',       -- replace with the actual event_time value
          '<updated_at>',       -- replace with the actual event_time value
          job.uuid,             -- replace with the actual event_time value
          job.location,         -- replace with the actual event_time value
          job.namespace_uuid,   -- replace with the actual event_time value
          job.namespace_name,   -- replace with the actual event_time value
          job.name              -- replace with the actual event_time value
        FROM job ON CONFLICT (uuid) DO NOTHING
      )
      UPDATE jobs
         SET current_version_uuid = '<job_version_uuid>'
       WHERE uuid = '<job_uuid>'
      """;

  /* ... */
  String WRITE_RUN_META =
      """
      WITH run AS (
        INSERT INTO runs (
          uuid,
          job_version_uuid,
          nominal_start_time,
          nominal_end_time,
          current_run_state,
          external_run_id,
          transitioned_at,
          started_at,
          ended_at
        ) VALUES (
          '<run_id>',                               -- replace with the actual UUID value
          '<job_version_uuid>',                     -- replace with the actual job version UUID value
          '<run_nominal_start_time>',               -- replace with the actual nominal start time value
          '<run_nominal_end_time>',                 -- replace with the actual nominal end time value
          '<run_state>',                            -- replace with the actual nominal end time value
          NULLIF('<run_external_id>',''),           -- replace with the actual transitioned at value
          '<run_transitioned_at>',                  -- replace with the actual transitioned at value
          NULLIF('<run_started_at>','')::timestamp, -- replace with the actual transitioned at value
          NULLIF('<run_ended_at>','')::timestamp    -- replace with the actual transitioned at value
        ) ON CONFLICT (uuid) DO NOTHING
      ),
      current_run_uuid_for_job_versions AS (
        UPDATE job_versions
           SET current_run_uuid = '<run_id>'
         WHERE uuid = '<job_version_uuid>'
      ),
      current_run_uuid_for_job AS (
        UPDATE jobs
           SET current_run_uuid = '<run_id>'
         WHERE uuid = '<job_uuid>'
      )
      INSERT INTO run_states (
        uuid,
        transitioned_at,
        run_uuid,
        state
      ) VALUES (
        '<run_state_uuid>',      -- replace with the actual UUID value
        '<run_transitioned_at>', -- replace with the actual UUID value
        '<run_id>',              -- replace with the actual UUID value
        '<run_state>'            -- replace with the actual UUID value
      ) ON CONFLICT (run_uuid, state) DO NOTHING
      """;

  /* ... */
  String UPSERT_DATASET_ROW = """
          """;
}
