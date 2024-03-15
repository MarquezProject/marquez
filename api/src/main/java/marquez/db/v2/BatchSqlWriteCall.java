package marquez.db.v2;

import com.google.common.collect.Iterables;
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
      log.info("Writing metadata: {}", runMeta);

      final Batch dbCallAsBatch = dbCallHandle.createBatch();

      final Instant nowAsUtc = Instant.now();
      dbCallAsBatch.define("created_at", nowAsUtc);
      dbCallAsBatch.define("updated_at", nowAsUtc);

      dbCallAsBatch
          .add(Sql.WRITE_LINEAGE_EVENT)
          .define("run_id", runMeta.getId().getValue())
          .define("run_state", runMeta.getState())
          .define("run_transitioned_at", runMeta.getTransitionedOn())
          .define("job_namespace_name", jobMeta.getNamespace().getValue())
          .define("job_name", jobMeta.getName().getValue())
          .define("event_received_time", nowAsUtc)
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
          .define("job_location", jobMeta.getLocation().orElse(null));

      dbCallAsBatch
          .add(Sql.WRITE_JOB_VERSION_META)
          .define("job_version_uuid", jobMeta.getVersionId().getVersion());

      dbCallAsBatch
          .add(Sql.WRITE_RUN_META)
          .define("run_state_uuid", UUID.randomUUID())
          .define("run_nominal_start_time", runMeta.getNominalStartTime().orElse(null))
          .define("run_nominal_end_time", runMeta.getNominalEndTime().orElse(null))
          .define("run_external_id", runMeta.getExternalId().orElse(null))
          .define("run_started_at", runMeta.getStartedAt().orElse(null))
          .define("run_ended_at", runMeta.getEndedAt().orElse(null));

      Iterables.concat(ioMeta.getInputs(), ioMeta.getOutputs())
          .forEach(
              ioMeta -> {
                dbCallAsBatch
                    .add(Sql.WRITE_DATASET_META)
                    .define("dataset_namespace_uuid", UUID.randomUUID())
                    .define("dataset_namespace_name", ioMeta.getNamespace().getValue())
                    .define("dataset_namespace_description", null)
                    .define("source_uuid", UUID.randomUUID())
                    .define("source_type", "DB")
                    .define("source_name", ioMeta.getSource().getName().getValue())
                    .define(
                        "source_connection_url",
                        ioMeta.getSource().getConnectionUrl().toASCIIString())
                    .define("source_description", null)
                    .define("dataset_uuid", UUID.randomUUID())
                    .define("dataset_type", ioMeta.getType())
                    .define("dataset_name", ioMeta.getName().getValue());

                // ...
                // dbCallAsBatch.add(Sql.WRITE_DATASET_VERSION_META);
                ioMeta
                    .getSchema()
                    .getFields()
                    .forEach(
                        fieldMeta -> {
                          UUID uuid = UUID.randomUUID();
                          log.info("fieldMeta ('{}') for dataset '{}': {}", uuid, fieldMeta);
                          dbCallAsBatch
                              .add(Sql.WRITE_DATASET_FIELDS_META)
                              .define("dataset_field_uuid", uuid)
                              .define("dataset_field_type", fieldMeta.getType())
                              .define("dataset_field_name", fieldMeta.getName())
                              .define(
                                  "dataset_field_description",
                                  fieldMeta.getDescription().orElse(null));
                        });
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

      final Instant nowAsUtc = Instant.now();
      dbCallAsBatch.define("created_at", nowAsUtc);
      dbCallAsBatch.define("updated_at", nowAsUtc);

      dbCallAsBatch
          .add(Sql.WRITE_JOB_META)
          .define("job_uuid", UUID.randomUUID())
          .define("job_type", jobMeta.getType())
          .define("job_namespace_uuid", UUID.randomUUID())
          .define("job_namespace_name", jobMeta.getNamespace().getValue())
          .define("job_name", jobMeta.getName().getValue())
          .define("job_description", jobMeta.getDescription().orElse(null))
          .define("job_location", jobMeta.getLocation().orElse(null));

      dbCallAsBatch
          .add(Sql.WRITE_JOB_VERSION_META)
          .define("job_version_uuid", jobMeta.getVersionId().getVersion());

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

      final Instant nowAsUtc = Instant.now();
      dbCallAsBatch.define("created_at", nowAsUtc);
      dbCallAsBatch.define("updated_at", nowAsUtc);

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
