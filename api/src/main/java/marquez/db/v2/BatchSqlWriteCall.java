package marquez.db.v2;

import io.openlineage.server.OpenLineage;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.v2.RunLevelMetadata;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.stringtemplate4.StringTemplateEngine;

/** ... */
@Slf4j
public class BatchSqlWriteCall implements HandleConsumer<Exception> {
  private final RunLevelMetadata runLevelMeta;

  private BatchSqlWriteCall(@NonNull final RunLevelMetadata runLevelMeta) {
    this.runLevelMeta = runLevelMeta;
  }

  /** ... */
  public static BatchSqlWriteCall newCallFor(@NotNull final OpenLineage.RunEvent olRunEvent) {
    return new BatchSqlWriteCall(RunLevelMetadata.newInstanceFor(olRunEvent));
  }

  /** ... */
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
        .add(Sql.WRITE_LINEAGE_EVENT)
        .define("run_id", runLevelMeta.getRunId().getValue())
        .define("run_state", runLevelMeta.getRunState())
        .define("run_transitioned_at", runLevelMeta.getRunTransitionedOn())
        .define("job_namespace_name", runLevelMeta.getJobNamespace().getValue())
        .define("job_name", runLevelMeta.getJobName().getValue())
        .define("event_received_time", rowsModifiedOn)
        .define("event", runLevelMeta.getRawData())
        .define("producer", runLevelMeta.getProducer())
        .define("_event_type", "RUN_EVENT");

    // UPSERT jobs
    dbCallAsBatch
        .add(Sql.WRITE_JOB_META)
        .define("job_uuid", runLevelMeta.getJobUuid())
        .define("job_type", runLevelMeta.getJobType())
        .define("job_namespace_uuid", runLevelMeta.getJobNamespaceUuid())
        .define("job_namespace_name", runLevelMeta.getJobNamespace().getValue())
        .define("job_namespace_description", runLevelMeta.getJobNamespaceDescription().orElse(null))
        .define("job_name", runLevelMeta.getJobName().getValue())
        .define("job_description", runLevelMeta.getJobDescription().orElse(null))
        .define("job_location", runLevelMeta.getJobLocation().orElse(null));

    // INSERT job_versions
    if (runLevelMeta.getRunState().isDone()) {
      dbCallAsBatch
          .add(Sql.WRITE_JOB_VERSION_META)
          .define("job_version_uuid", runLevelMeta.getJobVersion().getValue());
    }

    dbCallAsBatch
        .add(Sql.WRITE_RUN_META)
        .define("run_state_uuid", runLevelMeta.getRunStateUuid())
        .define("run_nominal_start_time", runLevelMeta.getRunNominalStartTime())
        .define("run_nominal_end_time", runLevelMeta.getRunNominalEndTime())
        .define("run_external_id", runLevelMeta.getRunExternalId().orElse(null))
        .define("run_started_at", runLevelMeta.getRunStartedAt().orElse(null))
        .define("run_ended_at", runLevelMeta.getRunEndedAt().orElse(null));

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
}
