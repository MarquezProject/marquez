package marquez.db.v2;

import static java.lang.String.format;

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
      log.debug("Writing metadata: {}", runMeta);

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

      // ...
      int batchEntryForInputMeta = 0;
      for (final Metadata.Dataset inputMeta : ioMeta.getInputs()) {
        dbCallAsBatch
            .add(format(Sql.WRITE_DATASET_META, batchEntryForInputMeta))
            .define(format("dataset_namespace_uuid_%d", batchEntryForInputMeta), UUID.randomUUID())
            .define(
                format("dataset_namespace_name_%d", batchEntryForInputMeta),
                inputMeta.getNamespace().getValue())
            .define(format("dataset_namespace_description_%d", batchEntryForInputMeta), null)
            .define(format("source_uuid_%d", batchEntryForInputMeta), UUID.randomUUID())
            .define(format("source_type_%d", batchEntryForInputMeta), "DB")
            .define(
                format("source_name_%d", batchEntryForInputMeta),
                inputMeta.getSource().getName().getValue())
            .define(
                format("source_connection_url_%d", batchEntryForInputMeta),
                inputMeta.getSource().getConnectionUrl().toASCIIString())
            .define(format("source_description_%d", batchEntryForInputMeta), null)
            .define(format("dataset_uuid_%d", batchEntryForInputMeta), UUID.randomUUID())
            .define(format("dataset_type_%d", batchEntryForInputMeta), inputMeta.getType())
            .define(
                format("dataset_name_%d", batchEntryForInputMeta), inputMeta.getName().getValue());
        // ...
        int batchEntryForFieldMeta = 0;
        for (final Metadata.Dataset.Schema.Field fieldMeta : inputMeta.getSchema().getFields()) {
          dbCallAsBatch
              .add(format(Sql.WRITE_DATASET_FIELDS_META, batchEntryForFieldMeta))
              .define("dataset_namespace_name", inputMeta.getNamespace().getValue())
              .define("dataset_name", inputMeta.getName().getValue())
              .define(format("dataset_field_uuid_%d", batchEntryForFieldMeta), UUID.randomUUID())
              .define(format("dataset_field_type_%d", batchEntryForFieldMeta), fieldMeta.getType())
              .define(format("dataset_field_name_%d", batchEntryForFieldMeta), fieldMeta.getName())
              .define(
                  format("dataset_field_description_%d", batchEntryForFieldMeta),
                  fieldMeta.getDescription().orElse(null));
          batchEntryForFieldMeta++;
        }
        batchEntryForInputMeta++;
      }

      int batchEntryForOutputMeta = 0;
      for (final Metadata.Dataset outputMeta : ioMeta.getOutputs()) {
        dbCallAsBatch
            .add(format(Sql.WRITE_DATASET_META, batchEntryForOutputMeta))
            .define(format("dataset_namespace_uuid_%d", batchEntryForOutputMeta), UUID.randomUUID())
            .define(
                format("dataset_namespace_name_%d", batchEntryForOutputMeta),
                outputMeta.getNamespace().getValue())
            .define(format("dataset_namespace_description_%d", batchEntryForOutputMeta), null)
            .define(format("source_uuid_%d", batchEntryForOutputMeta), UUID.randomUUID())
            .define(format("source_type_%d", batchEntryForOutputMeta), "DB")
            .define(
                format("source_name_%d", batchEntryForOutputMeta),
                outputMeta.getSource().getName().getValue())
            .define(
                format("source_connection_url_%d", batchEntryForOutputMeta),
                outputMeta.getSource().getConnectionUrl().toASCIIString())
            .define(format("source_description_%d", batchEntryForOutputMeta), null)
            .define(format("dataset_uuid_%d", batchEntryForOutputMeta), UUID.randomUUID())
            .define(format("dataset_type_%d", batchEntryForOutputMeta), outputMeta.getType())
            .define(
                format("dataset_name_%d", batchEntryForOutputMeta),
                outputMeta.getName().getValue());
        // ...
        int batchEntryForFieldMeta = 0;
        for (final Metadata.Dataset.Schema.Field fieldMeta : outputMeta.getSchema().getFields()) {
          dbCallAsBatch
              .add(format(Sql.WRITE_DATASET_FIELDS_META, batchEntryForFieldMeta))
              .define("dataset_namespace_name", outputMeta.getNamespace().getValue())
              .define("dataset_name", outputMeta.getName().getValue())
              .define(format("dataset_field_uuid_%d", batchEntryForFieldMeta), UUID.randomUUID())
              .define(format("dataset_field_type_%d", batchEntryForFieldMeta), fieldMeta.getType())
              .define(format("dataset_field_name_%d", batchEntryForFieldMeta), fieldMeta.getName())
              .define(
                  format("dataset_field_description_%d", batchEntryForFieldMeta),
                  fieldMeta.getDescription().orElse(null));
          batchEntryForFieldMeta++;
        }
        batchEntryForOutputMeta++;
      }

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
