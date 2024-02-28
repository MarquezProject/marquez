package marquez.db.v2;

import io.openlineage.server.OpenLineage;
import java.time.Instant;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.v2.models.Metadata;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.statement.Batch;

/** ... */
public interface BatchSqlWriteCall extends HandleConsumer<Exception> {
  /** ... */
  @Slf4j
  class WriteRunMetadata implements BatchSqlWriteCall {
    final Metadata.Run runMeta;
    final Metadata.Job jobMeta;
    final Metadata.IO ioMeta;

    WriteRunMetadata(@NonNull final Metadata.Run runMeta) {
      this.runMeta = runMeta;
      this.jobMeta = runMeta.getJob();
      this.ioMeta = runMeta.getIo();
    }

    static WriteRunMetadata newWriteCallFor(@NotNull final OpenLineage.RunEvent event) {
      return new WriteRunMetadata(Metadata.Run.newInstanceFor(event));
    }

    /** ... */
    @Override
    public void useHandle(@NonNull Handle dbCallHandle) {
      final Batch dbCallAsBatch = dbCallHandle.createBatch();

      final Instant now = Instant.now();
      dbCallAsBatch.define("created_at", now);
      dbCallAsBatch.define("updated_at", now);

      dbCallAsBatch
          .add(Sql.WRITE_LINEAGE_EVENT)
          .define("run_id", runMeta.getId().getValue())
          .define("run_state", runMeta.getState())
          .define("run_transitioned_at", runMeta.getTransitionedOn())
          .define("job_namespace_name", jobMeta.getNamespace().getValue())
          .define("job_name", jobMeta.getName().getValue())
          .define("event_received_time", now)
          .define("event", runMeta.getRawMeta())
          .define("producer", runMeta.getProducer())
          .define("_event_type", "RUN_EVENT");

      dbCallAsBatch
          .add(Sql.WRITE_JOB_META)
          .define("job_uuid", UUID.randomUUID())
          .define("job_type", jobMeta.getType())
          .define("job_namespace_uuid", UUID.randomUUID())
          .define("job_namespace_name", jobMeta.getNamespace().getValue())
          .define("job_name", jobMeta.getName().getValue())
          .define("job_description", jobMeta.getDescription().orElse(null))
          .define("job_location", jobMeta.getLocation().orElse(null))
          .define("job_version_uuid", jobMeta.getVersion().getValue());

      dbCallAsBatch
          .add(Sql.WRITE_JOB_VERSION_META)
          .define("job_version_uuid", jobMeta.getVersion().getValue());

      dbCallAsBatch
          .add(Sql.WRITE_RUN_META)
          .define("run_state_uuid", UUID.randomUUID())
          .define("run_nominal_start_time", runMeta.getNominalStartTime())
          .define("run_nominal_end_time", runMeta.getNominalEndTime())
          .define("run_external_id", runMeta.getExternalId().orElse(null))
          .define("run_started_at", runMeta.getStartedAt().orElse(null))
          .define("run_ended_at", runMeta.getEndedAt().orElse(null));

      ioMeta
          .getInputs()
          .forEach(
              datasetMeta -> {
                dbCallAsBatch.add(Sql.WRITE_DATASET_META);
              });

      ioMeta
          .getInputs()
          .forEach(
              datasetMeta -> {
                dbCallAsBatch.add(Sql.WRITE_DATASET_META);
              });

      dbCallAsBatch.execute();
    }
  }

  @Slf4j
  class WriteOnlyJobMetadata implements BatchSqlWriteCall {
    final Metadata.Job jobMeta;

    WriteOnlyJobMetadata(@NonNull final Metadata.Job jobMeta) {
      this.jobMeta = jobMeta;
    }

    static WriteOnlyJobMetadata newWriteOnlyCallFor(@NotNull final OpenLineage.JobEvent event) {
      return new WriteOnlyJobMetadata(Metadata.Job.newInstanceFor(event));
    }

    /** ... */
    @Override
    public void useHandle(@NonNull Handle dbCallHandle) {
      final Batch dbCallAsBatch = dbCallHandle.createBatch();

      final Instant now = Instant.now();
      dbCallAsBatch.define("created_at", now);
      dbCallAsBatch.define("updated_at", now);

      dbCallAsBatch
          .add(Sql.WRITE_JOB_META)
          .define("job_uuid", UUID.randomUUID())
          .define("job_type", jobMeta.getType())
          .define("job_namespace_uuid", UUID.randomUUID())
          .define("job_namespace_name", jobMeta.getNamespace().getValue())
          .define("job_name", jobMeta.getName().getValue())
          .define("job_description", jobMeta.getDescription().orElse(null))
          .define("job_location", jobMeta.getLocation().orElse(null))
          .define("job_version_uuid", jobMeta.getVersion().getValue());

      dbCallAsBatch
          .add(Sql.WRITE_JOB_VERSION_META)
          .define("job_version_uuid", jobMeta.getVersion().getValue());

      dbCallAsBatch.execute();
    }
  }

  @Slf4j
  class WriteOnlyDatasetMetadata implements BatchSqlWriteCall {
    final Metadata.Dataset datasetMeta;

    WriteOnlyDatasetMetadata(@NonNull final Metadata.Dataset datasetMeta) {
      this.datasetMeta = datasetMeta;
    }

    static WriteOnlyDatasetMetadata newWriteOnlyCallFor(
        @NotNull final OpenLineage.DatasetEvent event) {
      return new WriteOnlyDatasetMetadata(Metadata.Dataset.newInstanceFor(event));
    }

    /** ... */
    @Override
    public void useHandle(@NonNull Handle dbCallHandle) {
      final Batch dbCallAsBatch = dbCallHandle.createBatch();

      final Instant now = Instant.now();
      dbCallAsBatch.define("created_at", now);
      dbCallAsBatch.define("updated_at", now);

      dbCallAsBatch.execute();
    }
  }

  /** ... */
  static BatchSqlWriteCall newWriteCallFor(@NotNull final OpenLineage.BaseEvent event) {
    if (event instanceof OpenLineage.RunEvent) {
      return WriteRunMetadata.newWriteCallFor((OpenLineage.RunEvent) event);
    } else if (event instanceof OpenLineage.JobEvent) {
      return WriteOnlyJobMetadata.newWriteOnlyCallFor((OpenLineage.JobEvent) event);
    } else if (event instanceof OpenLineage.DatasetEvent) {
      return WriteOnlyDatasetMetadata.newWriteOnlyCallFor((OpenLineage.DatasetEvent) event);
    } else {
      throw new IllegalArgumentException();
    }
  }
}
