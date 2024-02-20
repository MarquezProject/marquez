package marquez.db.v2;

import io.openlineage.server.OpenLineage;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.v2.RunLevelLineageMetadata;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.stringtemplate4.StringTemplateEngine;

@Slf4j
public class BatchSqlWriteCall implements HandleConsumer<Exception> {
  private final RunLevelLineageMetadata runLevelLineageMeta;

  private BatchSqlWriteCall(@NonNull final RunLevelLineageMetadata runLevelLineageMeta) {
    this.runLevelLineageMeta = runLevelLineageMeta;
  }

  public static BatchSqlWriteCall newCallFor(@NotNull final OpenLineage.RunEvent olRunEvent) {
    return new BatchSqlWriteCall(RunLevelLineageMetadata.newInstanceFor(olRunEvent));
  }

  @Override
  public void useHandle(@NonNull Handle dbCallHandle) {
    final Batch dbCallAsBatch =
        dbCallHandle.createBatch().setTemplateEngine(new StringTemplateEngine());

    // TIMESTAMP
    final Instant rowsModifiedOn = Instant.now();
    dbCallAsBatch.define("created_at", rowsModifiedOn);
    dbCallAsBatch.define("updated_at", rowsModifiedOn);

    // INSERT lineage_events
    dbCallAsBatch
        .add(WRITE_LINEAGE_EVENT)
        .define("run_id", runLevelLineageMeta.getRunId().getValue())
        .define("run_state", runLevelLineageMeta.getRunState())
        .define("run_transitioned_at", runLevelLineageMeta.getRunTransitionedOn())
        .define("job_namespace_name", runLevelLineageMeta.getJobNamespace().getValue())
        .define("job_name", runLevelLineageMeta.getJobName().getValue())
        .define("event_received_time", rowsModifiedOn)
        .define("event", runLevelLineageMeta.getRawData())
        .define("producer", runLevelLineageMeta.getProducer())
        .define("_event_type", "RUN_EVENT");

    // UPSERT jobs
    dbCallAsBatch
        .add(WRITE_JOB_META)
        .define("job_uuid", runLevelLineageMeta.getJobUuid())
        .define("job_type", runLevelLineageMeta.getJobType())
        .define("job_namespace_uuid", runLevelLineageMeta.getJobNamespaceUuid())
        .define("job_namespace_name", runLevelLineageMeta.getJobNamespace().getValue())
        .define("job_name", runLevelLineageMeta.getJobName().getValue())
        .define("job_description", runLevelLineageMeta.getJobDescription().orElse(null))
        .define("job_location", runLevelLineageMeta.getJobLocation().orElse(null));

    // INSERT job_versions
    if (runLevelLineageMeta.getRunState().isDone()) {
      dbCallAsBatch
          .add(WRITE_JOB_VERSION_META)
          .define("job_version_uuid", runLevelLineageMeta.getJobVersion().getValue());
    }

    dbCallAsBatch
        .add(WRITE_RUN_META)
        .define("run_state_uuid", runLevelLineageMeta.getRunStateUuid())
        .define("run_nominal_start_time", runLevelLineageMeta.getRunNominalStartTime())
        .define("run_nominal_end_time", runLevelLineageMeta.getRunNominalEndTime())
        .define("run_external_id", runLevelLineageMeta.getRunExternalId().orElse(null))
        .define("run_started_at", runLevelLineageMeta.getRunStartedAt().orElse(null))
        .define("run_ended_at", runLevelLineageMeta.getRunEndedAt().orElse(null));

    // INSERT datasets
    // activeOlRun.getInputs().forEach(() -> {});
    // activeOlRun.getInputs().forEach(() -> {});

    // EXECUTE
    try {
      dbCallAsBatch.execute();
    } catch (Exception e) {
      log.error("BatchSqlWriteCall.useHandle()", e);
    }
  }

  /* ... */
  private static final String WRITE_LINEAGE_EVENT =
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
  private static final String WRITE_JOB_META =
      """
      WITH namespace AS (
        INSERT INTO namespaces (
          uuid,
          created_at,
          updated_at,
          name
        ) VALUES (
          '<job_namespace_uuid>', -- replace with the actual event_time value
          '<created_at>',         -- replace with the actual event_time value
          '<updated_at>',         -- replace with the actual event_time value
          '<job_namespace_name>'  -- replace with the actual event_time value
        )
        ON CONFLICT (name) DO NOTHING
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
                      description = EXCLUDED.description,
                      location    = EXCLUDED.location
      """;

  /* ... */
  private static final String WRITE_JOB_VERSION_META =
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
          location,
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
        ON CONFLICT (uuid) DO NOTHING
      )
      UPDATE jobs
         SET current_version_uuid = '<job_version_uuid>'
       WHERE uuid = '<job_uuid>'
      """;

  /* ... */
  private static final String WRITE_RUN_META =
      """
      WITH run AS (
        INSERT INTO runs (
          uuid,
          job_version_uuid,
          nominal_start_time,
          nominal_end_time,
          external_id,
          transitioned_at,
          started_at,
          ended_at
        ) VALUES (
          '<run_id>',                   -- replace with the actual UUID value
          '<job_version_uuid>',          -- replace with the actual job version UUID value
          '<run_nominal_start_time>',    -- replace with the actual nominal start time value
          '<run_nominal_end_time>',      -- replace with the actual nominal end time value
          NULLIF('<run_external_id>',''), -- replace with the actual transitioned at value
          '<run_transitioned_at>',        -- replace with the actual transitioned at value
          NULLIF('<run_started_at>',''),  -- replace with the actual transitioned at value
          NULLIF('<run_ended_at>','')     -- replace with the actual transitioned at value
        ) ON CONFLICT (uuid) DO NOTHING
      ),
      run_state AS (
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
      ),
      current_run_uuid_for_job AS (
        UPDATE jobs
           SET current_run_uuid = '<run_id>'
         WHERE uuid = '<job_uuid>'
      )
      UPDATE job_versions
         SET current_run_uuid = '<run_id>'
       WHERE uuid = '<job_version_uuid>'
      """;

  /* ... */
  private static final String UPSERT_DATASET_ROW = """
          """;
}
