package marquez.db.v2;

import static java.lang.String.format;

import com.google.common.collect.ImmutableList;
import io.openlineage.server.OpenLineage;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
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

      dbCallAsBatchAdd(ioMeta.getInputs().asList(), dbCallAsBatch);
      dbCallAsBatchAdd(ioMeta.getInputs().asList(), dbCallAsBatch);

      dbCallAsBatch.execute();
    }

    /** ... */
    void dbCallAsBatchAdd(
        @NonNull ImmutableList<Metadata.Dataset> ioMeta, @NonNull Batch dbCallAsBatch) {
      IntStream.range(0, ioMeta.size())
          .forEachOrdered(
              posOfDatasetMeta -> {
                final Metadata.Dataset datasetMeta = ioMeta.get(posOfDatasetMeta);
                dbCallAsBatch
                    .add(format(Sql.WRITE_DATASET_META, posOfDatasetMeta))
                    .define(
                        format("dataset_namespace_uuid_%d", posOfDatasetMeta), UUID.randomUUID())
                    .define(
                        format("dataset_namespace_name_%d", posOfDatasetMeta),
                        datasetMeta.getNamespace().getValue())
                    .define(format("dataset_namespace_description_%d", posOfDatasetMeta), null)
                    .define(format("source_uuid_%d", posOfDatasetMeta), UUID.randomUUID())
                    .define(format("source_type_%d", posOfDatasetMeta), "DB")
                    .define(
                        format("source_name_%d", posOfDatasetMeta),
                        datasetMeta.getSource().getName().getValue())
                    .define(
                        format("source_connection_url_%d", posOfDatasetMeta),
                        datasetMeta.getSource().getConnectionUrl().toASCIIString())
                    .define(format("source_description_%d", posOfDatasetMeta), null)
                    .define(format("dataset_uuid_%d", posOfDatasetMeta), UUID.randomUUID())
                    .define(format("dataset_type_%d", posOfDatasetMeta), datasetMeta.getType())
                    .define(
                        format("dataset_name_%d", posOfDatasetMeta),
                        datasetMeta.getName().getValue());
                // ...
                final ImmutableList<Metadata.Dataset.Schema.Field> fieldsMeta =
                    datasetMeta.getSchema().getFields().asList();
                IntStream.range(0, fieldsMeta.size())
                    .forEachOrdered(
                        posOfFieldMeta -> {
                          final Metadata.Dataset.Schema.Field fieldMeta =
                              fieldsMeta.get(posOfFieldMeta);
                          dbCallAsBatch
                              .add(format(Sql.WRITE_DATASET_FIELDS_META, posOfFieldMeta))
                              .define(
                                  "dataset_namespace_name", datasetMeta.getNamespace().getValue())
                              .define("dataset_name", datasetMeta.getName().getValue())
                              .define(
                                  format("dataset_field_uuid_%d", posOfFieldMeta),
                                  UUID.randomUUID())
                              .define(
                                  format("dataset_field_type_%d", posOfFieldMeta),
                                  fieldMeta.getType())
                              .define(
                                  format("dataset_field_name_%d", posOfFieldMeta),
                                  fieldMeta.getName())
                              .define(
                                  format("dataset_field_description_%d", posOfFieldMeta),
                                  fieldMeta.getDescription().orElse(null));
                        });
              });
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
