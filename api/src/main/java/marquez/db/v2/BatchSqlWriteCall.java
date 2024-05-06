package marquez.db.v2;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;

import com.google.common.collect.ImmutableList;
import io.openlineage.server.OpenLineage;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
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
    @Nullable final Metadata.IO ioMeta;

    WriteRunMetadata(@NonNull final Metadata.Run runMeta) {
      this.runMeta = runMeta;
      this.jobMeta = runMeta.getJob();
      this.ioMeta = runMeta.getIo().orElse(null);
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

      // ...
      // 1. inputs: get.current() or new version (without runID) new input
      // 2. outputs: always new version (with runID)
      if (ioMeta != null) {
        batchSqlAddAll(
            Stream.concat(
                    ioMeta.getInputs().asList().stream(), ioMeta.getOutputs().asList().stream())
                .collect(toImmutableList()),
            dbCallAsBatch);
      }

      // ...
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

      batchSqlAdd(datasetMeta, dbCallAsBatch);

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

  /** ... */
  static void batchSqlAddAll(
      @NonNull final ImmutableList<Metadata.Dataset> ioMeta, @NonNull final Batch dbCallAsBatch) {
    IntStream.range(0, ioMeta.size())
        .forEachOrdered(
            idx -> {
              batchSqlAddWithIdx(idx, ioMeta.get(idx), dbCallAsBatch);
            });
  }

  /** ... */
  static void batchSqlAdd(
      @NonNull final Metadata.Dataset datasetMeta, @NonNull final Batch dbCallAsBatch) {
    batchSqlAddWithIdx(0, datasetMeta, dbCallAsBatch);
  }

  /** ... */
  static void batchSqlAddWithIdx(
      final int idx,
      @NonNull final Metadata.Dataset datasetMeta,
      @NonNull final Batch dbCallAsBatch) {
    dbCallAsBatch
        .add(format(Sql.WRITE_DATASET_META, idx))
        .define(format("dataset_namespace_uuid_%d", idx), UUID.randomUUID())
        .define(format("dataset_namespace_name_%d", idx), datasetMeta.getNamespace().getValue())
        .define(format("dataset_namespace_description_%d", idx), null)
        .define(format("source_uuid_%d", idx), UUID.randomUUID())
        .define(format("source_type_%d", idx), "DB")
        .define(format("source_name_%d", idx), datasetMeta.getSource().getName().getValue())
        .define(
            format("source_connection_url_%d", idx),
            datasetMeta.getSource().getConnectionUrl().toASCIIString())
        .define(format("source_description_%d", idx), null)
        .define(format("dataset_uuid_%d", idx), UUID.randomUUID())
        .define(format("dataset_type_%d", idx), datasetMeta.getType())
        .define(format("dataset_name_%d", idx), datasetMeta.getName().getValue());

    datasetMeta
        .getSchema()
        .ifPresent(
            schema -> {
              batchSqlAddAll(schema.getFields().asList(), datasetMeta, dbCallAsBatch);
            });

    try {
      dbCallAsBatch
          .add(format(Sql.WRITE_DATASET_VERSION_META, idx))
          .define(format("dataset_version_uuid_%d", idx), datasetMeta.getVersionId().getVersion())
          .define(format("dataset_namespace_name_%d", idx), datasetMeta.getNamespace().getValue())
          .define(format("dataset_name_%d", idx), datasetMeta.getName().getValue());
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  /** ... */
  static void batchSqlAddAll(
      @NonNull final ImmutableList<Metadata.Dataset.Schema.Field> fieldsMeta,
      @NonNull final Metadata.Dataset datasetMeta,
      @NonNull final Batch dbCallAsBatch) {
    IntStream.range(0, fieldsMeta.size())
        .forEachOrdered(
            idx -> {
              final Metadata.Dataset.Schema.Field fieldMeta = fieldsMeta.get(idx);
              dbCallAsBatch
                  .add(format(Sql.WRITE_DATASET_FIELDS_META, idx))
                  .define(
                      format("dataset_namespace_name_%d", idx),
                      datasetMeta.getNamespace().getValue())
                  .define(format("dataset_name_%d", idx), datasetMeta.getName().getValue())
                  .define(format("dataset_field_uuid_%d", idx), UUID.randomUUID())
                  .define(format("dataset_field_type_%d", idx), fieldMeta.getType())
                  .define(format("dataset_field_name_%d", idx), fieldMeta.getName())
                  .define(
                      format("dataset_field_description_%d", idx),
                      fieldMeta.getDescription().orElse(null));
            });
  }
}
