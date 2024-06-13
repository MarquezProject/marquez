package marquez.db.v2;

/** ... */
public interface Sql {
  Upsert UPSERT = new Upsert();
  Select SELECT = new Select();

  class Upsert {
    String NAMESPACE_META =
        """
                      INSERT INTO namespaces (
                        uuid,
                        created_at,
                        updated_at,
                        name,
                        description
                      ) VALUES (
                        :uuid,                   -- replace with the actual event_time value
                        :createdAt,              -- replace with the actual event_time value
                        :updatedAt',             -- replace with the actual event_time value
                        :namespace,              -- replace with the actual event_time value
                        NULLIF(:description, '') -- replace with the actual event_time value
                     )
                     ON CONFLICT (name)
                     DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                                   description = COALESCE(NULLIF(EXCLUDED.description, ''), :description)
                      """;
  }

  class Select {
    String NAMESPACE = "";
  }

  /* ... */
  String WRITE_LINEAGE_EVENT =
      """
      INSERT INTO lineage_events (
        event_time,
        event,
        event_type,
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
        ON CONFLICT (name) DO NOTHING
        DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                      description = COALESCE(NULLIF(EXCLUDED.description, ''), namespaces.description)
        RETURNING *
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
      )
      SELECT
        '<job_uuid>',                    -- replace with the actual event_time value
        '<job_type>',                    -- replace with the actual event_time value
        '<created_at>',                  -- replace with the actual event_time value
        '<updated_at>',                  -- replace with the actual event_time value
        namespace.uuid,                  -- replace with the actual event_time value
        NULLIF('<job_description>', ''), -- replace with the actual event_time value
        namespace.name,                  -- replace with the actual event_time value
        NULLIF('<job_location>', ''),    -- replace with the actual event_time value
        '<job_name>'                     -- replace with the actual event_time value
      FROM namespace
      ON CONFLICT (namespace_name, name)
      DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                    description = COALESCE(NULLIF(EXCLUDED.description, ''), jobs.description),
                    location    = COALESCE(NULLIF(EXCLUDED.location, ''), jobs.location)
      """;

  /* ... */
  String WRITE_JOB_VERSION_META =
      """
      WITH job AS (
        SELECT uuid, namespace_uuid, namespace_name, name, location
          FROM jobs
         WHERE namespace_name = '<job_namespace_name>'
           AND name = '<job_name>'
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
        FROM job
        ON CONFLICT (uuid)
        DO UPDATE SET updated_at  = EXCLUDED.updated_at
        RETURNING *
      )
      UPDATE jobs
         SET current_version_uuid = job_version.uuid
        FROM job, job_version
       WHERE jobs.uuid = job.uuid
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
          '<run_id>',                                       -- replace with the actual UUID value
          '<job_version_uuid>',                             -- replace with the actual job version UUID value
          NULLIF('<run_nominal_start_time>','')::timestamp, -- replace with the actual nominal start time value
          NULLIF('<run_nominal_end_time>','')::timestamp,   -- replace with the actual nominal end time value
          '<run_state>',                                    -- replace with the actual nominal end time value
          NULLIF('<run_external_id>',''),                   -- replace with the actual transitioned at value
          '<run_transitioned_at>',                          -- replace with the actual transitioned at value
          NULLIF('<run_started_at>','')::timestamp,         -- replace with the actual transitioned at value
          NULLIF('<run_ended_at>','')::timestamp            -- replace with the actual transitioned at value
        )
        ON CONFLICT (uuid)
        DO UPDATE SET current_run_state  = EXCLUDED.current_run_state,
                      started_at         = COALESCE(EXCLUDED.started_at, runs.started_at),
                      ended_at           = COALESCE(EXCLUDED.ended_at, runs.ended_at)
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
  String WRITE_DATASET_META =
      """
      WITH namespace AS (
        INSERT INTO namespaces (
          uuid,
          created_at,
          updated_at,
          name,
          description
        ) VALUES (
          '<dataset_namespace_uuid_%1$d>',                   -- replace with the actual event_time value
          '<created_at>',                                    -- replace with the actual event_time value
          '<updated_at>',                                    -- replace with the actual event_time value
          '<dataset_namespace_name_%1$d>',                   -- replace with the actual event_time value
          NULLIF('<dataset_namespace_description_%1$d>', '') -- replace with the actual event_time value
        )
        ON CONFLICT (name)
        DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                      description = COALESCE(NULLIF(EXCLUDED.description, ''), namespaces.description)
        RETURNING *
      ),
      source AS (
        INSERT INTO sources (
          uuid,
          type,
          created_at,
          updated_at,
          name,
          connection_url,
          description
        ) VALUES (
          '<source_uuid_%1$d>',                   -- replace with the actual UUID value
          '<source_type_%1$d>',                   -- replace with the actual source type value
          '<created_at>',                         -- replace with the actual created_at value
          '<updated_at>',                         -- replace with the actual updated_at value
          '<source_name>',                        -- replace with the actual source name value
          '<source_connection_url_%1$d>',         -- replace with the actual connection URL value
          NULLIF('<source_description_%1$d>', '') -- replace with the actual description value
        )
        ON CONFLICT (name)
        DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                      description = COALESCE(NULLIF(EXCLUDED.description, ''), sources.description)
        RETURNING *
      )
      INSERT INTO datasets (
        uuid,
        type,
        created_at,
        updated_at,
        namespace_uuid,
        source_uuid,
        name,
        description,
        namespace_name,
        source_name
      )
      SELECT
        '<dataset_uuid_%1$d>',                     -- replace with the actual UUID value
        '<dataset_type_%1$d>',                     -- replace with the actual type value
        '<created_at>',                            -- replace with the actual created_at value
        '<updated_at>',                            -- replace with the actual updated_at value
        namespace.uuid,                            -- replace with the actual namespace_uuid value
        source.uuid,                               -- replace with the actual source_uuid value
        '<dataset_name_%1$d>',                     -- replace with the actual name value
        NULLIF('<dataset_description_%1$d>',  ''), -- replace with the actual description value
        namespace.name,                            -- replace with the actual namespace_name value
        source.name                                -- replace with the actual source_name value
      FROM namespace, source
      ON CONFLICT (namespace_name, name)
      DO UPDATE SET updated_at  = EXCLUDED.updated_at,
                    description = COALESCE(NULLIF(EXCLUDED.description, ''), datasets.description)
      """;

  String WRITE_DATASET_FIELDS_META =
      """
      WITH dataset AS (
        SELECT uuid
          FROM datasets
         WHERE namespace_name = '<dataset_namespace_name_%1$d>'
           AND name = '<dataset_name_%1$d>'
      )
      INSERT INTO dataset_fields (
        uuid,
        type,
        created_at,
        updated_at,
        dataset_uuid,
        name,
        description
      )
      SELECT
        '<dataset_field_uuid_%1$d>',                    -- replace with the actual UUID value
        '<dataset_field_type_%1$d>',                    -- replace with the actual UUID value
        '<created_at>',                                 -- replace with the actual UUID value
        '<updated_at>',                                 -- replace with the actual UUID value
        dataset.uuid,                                   -- replace with the actual UUID value
        '<dataset_field_name_%1$d>',                    -- replace with the actual UUID value
        NULLIF('<dataset_field_description_%1$d>',  '') -- replace with the actual UUID value
      FROM dataset ON CONFLICT (uuid) DO NOTHING
      """;

  String WRITE_DATASET_VERSION_META =
      """
      WITH dataset AS (
        SELECT uuid, namespace_name, name
          FROM datasets
         WHERE namespace_name = '<dataset_namespace_name_%1$d>'
           AND name = '<dataset_name_%1$d>'
      ),
      dataset_version AS (
        INSERT INTO dataset_versions (
          uuid,
          created_at,
          dataset_uuid,
          run_uuid,
          namespace_name,
          dataset_name
        )
        SELECT
          '<dataset_version_uuid_%1$d>', -- replace with the actual event_time value
          '<created_at>',           -- replace with the actual event_time value
          dataset.uuid              -- replace with the actual event_time value
          '<run_id>',               -- replace with the actual event_time value
          dataset.namespace_name,   -- replace with the actual event_time value
          dataset.name              -- replace with the actual event_time value
        FROM dataset ON CONFLICT (uuid) DO NOTHING
      )
      UPDATE datasets
         SET current_version_uuid = '<dataset_version_uuid_%1$d>'
        FROM dataset
       WHERE uuid = dataset.uuid
      """;
}
