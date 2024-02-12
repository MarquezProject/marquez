package marquez.db.v2;

import io.openlineage.server.OpenLineage;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import marquez.api.models.v2.ActiveOlRun;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.Batch;

public class BatchSqlWriteCall implements HandleConsumer<Exception> {
  private final ActiveOlRun activeOlRun;

  private BatchSqlWriteCall(@NonNull final ActiveOlRun activeOlRun) {
    this.activeOlRun = activeOlRun;
  }

  public static BatchSqlWriteCall newCallFor(@NotNull final OpenLineage.RunEvent olRunEvent) {
    return new BatchSqlWriteCall(ActiveOlRun.newRunFor(olRunEvent));
  }

  @Override
  public void useHandle(Handle dbCallHandle) {
    final Batch dbCallAsBatch = dbCallHandle.createBatch();

    // TIMESTAMP
    final Instant rowsModifiedOn = Instant.now();
    dbCallAsBatch.define("<created_at>", rowsModifiedOn);
    dbCallAsBatch.define("<updated_at>", rowsModifiedOn);

    // INSERT lineage_events
    dbCallAsBatch
        .add(INSERT_LINEAGE_EVENT_ROW)
        .define("<run_uuid>", activeOlRun.getRunUuid())
        .define("<run_state>", activeOlRun.getRunState())
        .define("<job_namespace>", activeOlRun.getJobNamespace().getValue())
        .define("<job_name>", activeOlRun.getJobName().getValue())
        .define("<_event_type>", activeOlRun.getType())
        .define("<event_time>", activeOlRun.getTransitionedOn())
        .define("<event_received_time>", activeOlRun.getReceivedAt())
        .define("<event>", activeOlRun.getRawData())
        .define("<producer>", activeOlRun.getProducer());

    // UPSERT jobs
    dbCallAsBatch
        .add(UPSERT_JOB_ROW)
        .define("<job_uuid>", activeOlRun.getJobUuid())
        .define("<job_type>", activeOlRun.getJobType())
        .define("<job_namespace_name>", activeOlRun.getJobNamespace().getValue())
        .define("<job_name>", activeOlRun.getJobName().getValue())
        .define("<job_description>", activeOlRun.getJobDescription().orElse(null));

    // INSERT job_versions
    if (activeOlRun.isDone()) {
      dbCallAsBatch
          .add(INSERT_JOB_VERSION_ROW)
          .define("<job_version_uuid>", activeOlRun.getJobVersionUuid())
          .define("<job_uuid>", activeOlRun.getJobUuid())
          .define("<job_name>", activeOlRun.getJobName())
          .define("<job_namespace_uuid>", activeOlRun.getJobNamespaceUuid())
          .define("<job_namespace_name>", activeOlRun.getJobNamespace())
          .define("<job_location>", activeOlRun.getJobLocation());
      // ADD edge node-> in/out EDGES
    }

    dbCallAsBatch
        .add(INSERT_RUN_ROW)
        .define("<run_uuid>", activeOlRun.getRunUuid())
        .define("<run_transitioned_at>", activeOlRun.getRunTransitionedAt())
        .define("<run_nominal_start_time>", activeOlRun.getNominalStartTime())
        .define("<run_nominal_end_time>", activeOlRun.getNominalEndTime())
        .define("<run_external_id>", activeOlRun.getExternalId())
        .define("<job_name>", activeOlRun.getJobName())
        .define("<job_namespace_name>", activeOlRun.getJobNamespace())
        .define("<job_location>", activeOlRun.getJobLocation())
        .define("<job_version_uuid>", activeOlRun.getJobVersionUuid());

    // EXECUTE
    dbCallAsBatch.execute();

    // INSERT datasets
    // activeOlRun.getInputs().forEach(() -> {});
    // activeOlRun.getInputs().forEach(() -> {});
  }

  private static final String INSERT_LINEAGE_EVENT_ROW =
      """
                INSERT INTO lineage_events (
                    event_time,
                    event,
                    event_type,
                    job_name,
                    job_namespace,
                    producer,
                    run_uuid,
                    event_received_time,
                    _event_type
                ) VALUES (
                    <event_time>, -- replace with the actual event_time value
                    <event>,      -- replace with the actual event value (should be a JSONB object)
                    <event_type>, -- replace with the actual event_type value
                    <job_name>,   -- replace with the actual job_name value
                    <job_namespace>, -- replace with the actual job_namespace value
                    <producer>,   -- replace with the actual producer value
                    <run_uuid>,   -- replace with the actual run_uuid value
                    <created_at>, -- replace with the actual created_at value (optional, as it has a default)
                    <event_received_time> -- replace with the actual _event_type value (optional, as it has a default)
                )
                """;
  private static final String UPSERT_JOB_ROW =
      """
                WITH namespace AS (
                ),
                job_version AS (
                  INSERT INTO job_versions (

                  )
                )
                INSERT INTO jobs (
                    uuid,
                    type,
                    created_at,
                    updated_at,
                    namespace_uuid,
                    description,
                    current_version_uuid,
                    namespace_name,
                    current_location,
                    is_hidden,
                    name,
                    aliases
                ) VALUES (
                    <job_uuid>,
                    <job_type>,
                    <created_at>,
                    <updated_at>,
                    <namespace_uuid>,
                    <description>,
                    <current_version_uuid>,
                    <namespace_name>,
                    <current_location>,
                    <is_hidden>,
                    <name>,
                    <aliases>
                ) ON CONFLICT (name)
                DO UPDATE SET
                    type = EXCLUDED.type,
                    updated_at = EXCLUDED.updated_at,
                    namespace_uuid = EXCLUDED.namespace_uuid,
                    description = EXCLUDED.description,
                    current_version_uuid = EXCLUDED.current_version_uuid,
                    namespace_name = EXCLUDED.namespace_name,
                    current_location = EXCLUDED.current_location,
                    is_hidden = EXCLUDED.is_hidden,
                    aliases = EXCLUDED.aliases
                """;
  private static final String INSERT_JOB_VERSION_ROW = "";
  private static final String INSERT_RUN_ROW = """
          """;
  private static final String UPSERT_DATASET_ROW = """
          """;
}
