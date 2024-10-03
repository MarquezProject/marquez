package marquez.api.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.Utils.toJson;
import static marquez.common.models.DatasetType.DB_TABLE;

import com.google.common.collect.ImmutableSet;
import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;

/** ... */
public final class Metadata {
  private Metadata() {}

  /** ... */
  @Builder
  @ToString
  public static final class ParentRun {
    @Getter private final RunId id;
    @Getter private final ParentRun.Job job;

    /* ... */
    public static ParentRun newInstanceWith(
        @NonNull final RunId id, @NonNull final ParentRun.Job job) {
      return ParentRun.builder().id(id).job(job).build();
    }

    /** ... */
    @Builder
    @ToString
    public static final class Job {
      @Getter private final JobName name;
      @Getter private final NamespaceName namespace;
    }
  }

  /** ... */
  @Builder
  @ToString
  public static final class Run {
    @Nullable private final ParentRun parent;
    @Getter private final RunId id;
    @Getter private final RunState state;
    @Getter private final Instant transitionedOn;
    @Nullable private final Instant startedAt;
    @Nullable private final Instant endedAt;
    @Nullable private final Instant nominalStartTime;
    @Nullable private final Instant nominalEndTime;
    @Nullable private final String externalId;

    @Getter private final Job job;
    @Nullable private final IO io;

    @Getter private final String rawMeta;
    @Getter private final URI producer;

    /* ... */
    public static Run newInstanceFor(@NonNull final OpenLineage.RunEvent event) {
      final OpenLineage.Run run = event.getRun();
      final RunId runId = RunId.of(run.getRunId());
      final RunState runState = RunState.forType(event.getEventType());
      final Instant runTransitionedOnAsUtc = toUtc(event.getEventTime());
      final RunBuilder runBuilder =
          Run.builder()
              .parent(Facets.parentRunFor(run).orElse(null))
              .id(runId)
              .state(runState)
              .transitionedOn(runTransitionedOnAsUtc)
              .nominalStartTime(Facets.nominalStartTimeFor(run).orElse(null))
              .nominalEndTime(Facets.nominalEndTimeFor(run).orElse(null))
              .rawMeta(toJson(event))
              .producer(event.getProducer());

      // ...
      if (runState.isDone()) {
        runBuilder.endedAt(runTransitionedOnAsUtc);
      } else {
        runBuilder.startedAt(runTransitionedOnAsUtc);
      }

      // ...
      final Job job =
          Job.newInstanceWith(run, event.getJob(), event.getInputs(), event.getOutputs());

      // ...
      runBuilder.job(job);
      runBuilder.io(job.getIo().orElse(null));

      return runBuilder.build();
    }

    public Optional<ParentRun> getParent() {
      return Optional.ofNullable(parent);
    }

    public Optional<Instant> getNominalStartTime() {
      return Optional.ofNullable(nominalStartTime);
    }

    public Optional<Instant> getNominalEndTime() {
      return Optional.ofNullable(nominalEndTime);
    }

    public Optional<Instant> getStartedAt() {
      return Optional.ofNullable(startedAt);
    }

    public Optional<Instant> getEndedAt() {
      return Optional.ofNullable(endedAt);
    }

    public Optional<String> getExternalId() {
      return Optional.ofNullable(externalId);
    }

    public Optional<IO> getIo() {
      return Optional.ofNullable(io);
    }

    /** ... */
    static Instant toUtc(@NonNull final ZonedDateTime transitionedOn) {
      return transitionedOn.withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }
  }

  /** ... */
  @Builder
  @ToString
  public static final class Job {
    @Getter private final JobId id;
    @Getter private final JobType type;
    @Getter private final JobName name;
    @Getter private final NamespaceName namespace;
    @Getter private final JobVersionId versionId;
    @Nullable private final String description;
    @Nullable private final URI location;
    @Nullable private final IO io;

    /* ... */
    public static Job newInstanceWith(
        @NonNull final OpenLineage.Run run,
        @NonNull final OpenLineage.Job job,
        @Nullable final List<OpenLineage.InputDataset> inputs,
        @Nullable final List<OpenLineage.OutputDataset> outputs) {
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      final JobId jobId = JobId.of(namespaceName, jobName);
      final Optional<URI> jobLocation = Facets.locationFor(job);
      final IO io = IO.newInstanceWith(run, inputs, outputs);

      // ...
      final JobVersionId jobVersionId = VersionId.forJob(jobId, jobLocation.orElse(null), io);

      // ...
      return Job.builder()
          .id(jobId)
          .type(JobType.BATCH)
          .name(jobName)
          .namespace(namespaceName)
          .versionId(jobVersionId)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(jobLocation.orElse(null))
          .io(io)
          .build();
    }

    /* ... */
    public static Job newInstanceFor(@NonNull final OpenLineage.JobEvent event) {
      final OpenLineage.Job job = event.getJob();
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      final JobId jobId = JobId.of(namespaceName, jobName);
      final Optional<URI> jobLocation = Facets.locationFor(job);
      final IO io = IO.newInstanceWith(event.getInputs(), event.getOutputs());

      // ...
      final JobVersionId jobVersionId = VersionId.forJob(jobId, jobLocation.orElse(null), io);

      // ...
      return Job.builder()
          .id(jobId)
          .type(JobType.BATCH)
          .name(jobName)
          .namespace(namespaceName)
          .versionId(jobVersionId)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(jobLocation.orElse(null))
          .io(io)
          .build();
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<URI> getLocation() {
      return Optional.ofNullable(location);
    }

    public Optional<IO> getIo() {
      return Optional.ofNullable(io);
    }
  }

  @Builder
  @ToString
  @EqualsAndHashCode
  public static final class Dataset {
    @Getter private final DatasetId id;
    @Getter private final DatasetType type;
    @Getter private final DatasetName name;
    @Getter private final NamespaceName namespace;
    @Getter private final DatasetVersionId versionId;
    @Nullable private final Schema schema;
    @Getter private final Source source;

    /* ... */
    public static Dataset newInstanceFor(@NonNull final OpenLineage.DatasetEvent event) {
      return newInstanceWith(null, event.getDataset());
    }

    /* ... */
    static Dataset newInstanceFor(@NonNull final OpenLineage.Dataset dataset) {
      return newInstanceWith(null, dataset);
    }

    /* ... */
    static Dataset newInstanceWith(
        @Nullable final OpenLineage.Run run, @NonNull final OpenLineage.Dataset dataset) {
      final NamespaceName namespaceName = NamespaceName.of(dataset.getNamespace());
      final DatasetName datasetName = DatasetName.of(dataset.getName());
      final Dataset.Schema datasetSchema = Facets.schemaFor(dataset).orElse(null);
      final DatasetId datasetId = new DatasetId(namespaceName, datasetName);
      final Dataset.Source source = Facets.sourceFor(dataset);

      // ...
      final DatasetVersionId datasetVersionId =
          VersionId.forDataset(datasetId, datasetSchema, source);

      // ...
      return Dataset.builder()
          .id(datasetId)
          .type(DB_TABLE)
          .name(datasetName)
          .namespace(namespaceName)
          .versionId(datasetVersionId)
          .schema(datasetSchema)
          .source(Facets.sourceFor(dataset))
          .build();
    }

    @Builder
    @ToString
    public static class Schema {
      @Getter @NonNull private final ImmutableSet<Schema.Field> fields;

      @Builder
      @ToString
      @EqualsAndHashCode
      public static class Field {
        @Getter private final String name;
        @Getter private final String type;
        @Nullable private final String description;

        public Optional<String> getDescription() {
          return Optional.ofNullable(description);
        }
      }
    }

    @Builder
    @ToString
    public static class Source {
      @Getter private final SourceName name;
      @Getter private final URI connectionUrl;
    }

    public Optional<Dataset.Schema> getSchema() {
      return Optional.ofNullable(schema);
    }
  }

  /* ... */
  @ToString
  public static final class IO {
    @Getter ImmutableSet<Dataset> inputs;
    @Getter ImmutableSet<Dataset> outputs;

    /* ... */
    IO(@NonNull final ImmutableSet<Dataset> inputs, @NonNull final ImmutableSet<Dataset> outputs) {
      this.inputs = inputs;
      this.outputs = outputs;
    }

    /* ... */
    public static @Nullable IO newInstanceWith(
        @Nullable final List<OpenLineage.InputDataset> inputs,
        @Nullable final List<OpenLineage.OutputDataset> outputs) {
      return newInstanceWith(null, inputs, outputs);
    }

    /* ... */
    public static @Nullable IO newInstanceWith(
        @Nullable final OpenLineage.Run run,
        @Nullable final List<OpenLineage.InputDataset> inputs,
        @Nullable final List<OpenLineage.OutputDataset> outputs) {
      if (inputs == null && outputs == null) {
        // ...
        return null;
      }

      final ImmutableSet.Builder<Dataset> inputsBuilder = ImmutableSet.builder();
      if (inputs != null) {
        for (final OpenLineage.InputDataset input : inputs) {
          inputsBuilder.add(Dataset.newInstanceFor(input));
        }
      }
      final ImmutableSet.Builder<Dataset> outputsBuilder = ImmutableSet.builder();
      if (outputs != null) {
        for (final OpenLineage.OutputDataset output : outputs) {
          outputsBuilder.add(Dataset.newInstanceWith(run, output));
        }
      }

      return new IO(inputsBuilder.build(), outputsBuilder.build());
    }
  }

  /* ... */
  static class Facets {
    static final String DOCUMENTATION = "documentation";
    static final String DESCRIPTION = "description";
    static final String URL = "url";

    static final String JOB_SOURCE_CODE_LOCATION = "sourceCodeLocation";

    static final String PARENT = "parent";
    static final String PARENT_RUN = "run";
    static final String PARENT_RUN_ID = "runId";
    static final String PARENT_JOB = "job";
    static final String PARENT_JOB_NAME = "name";
    static final String PARENT_JOB_NAMESPACE = "namespace";

    static final String RUN_NOMINAL_TIME = "nominalTime";
    static final String RUN_NOMINAL_START_TIME = "nominalStartTime";
    static final String RUN_NOMINAL_END_TIME = "nominalEndTime";

    static final String SCHEMA = "schema";
    static final String SCHEMA_FIELDS = "fields";
    static final String SCHEMA_FIELD_TYPE = "type";
    static final String SCHEMA_FIELD_NAME = "name";
    static final String SCHEMA_FIELD_DESCRIPTION = "description";

    static final String SOURCE = "dataSource";
    static final String SOURCE_NAME = "name";
    static final String SOURCE_CONNECTION_URL = "uri";

    Facets() {}

    @SuppressWarnings("unchecked")
    static Optional<ParentRun> parentRunFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(PARENT))
          .map(OpenLineage.RunFacet::getAdditionalProperties)
          .flatMap(
              facets -> {
                final Optional<RunId> parentRunId =
                    Optional.ofNullable(facets.get(PARENT_RUN))
                        .map(parentFacets -> (Map<String, Object>) parentFacets)
                        .map(parentFacets -> parentFacets.get(PARENT_RUN_ID))
                        .map(parentFacet -> new RunId((String) parentFacet));

                final Optional<ParentRun.Job> parentJob =
                    Optional.ofNullable(facets.get(PARENT_JOB))
                        .map(parentFacets -> (Map<String, Object>) parentFacets)
                        .map(
                            parentFacet ->
                                new ParentRun.Job(
                                    JobName.of((String) parentFacet.get(PARENT_JOB_NAME)),
                                    NamespaceName.of(
                                        (String) parentFacet.get(PARENT_JOB_NAMESPACE))));

                return parentRunId.flatMap(
                    runId -> parentJob.map(job -> ParentRun.newInstanceWith(runId, job)));
              });
    }

    static Optional<Instant> nominalStartTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(RUN_NOMINAL_TIME))
          .map(facets -> (String) facets.getAdditionalProperties().get(RUN_NOMINAL_START_TIME))
          .map(facet -> ZonedDateTime.parse(facet).withZoneSameInstant(ZoneOffset.UTC).toInstant());
    }

    static Optional<Instant> nominalEndTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(RUN_NOMINAL_TIME))
          .map(facets -> (String) facets.getAdditionalProperties().get(RUN_NOMINAL_END_TIME))
          .map(facet -> ZonedDateTime.parse(facet).withZoneSameInstant(ZoneOffset.UTC).toInstant());
    }

    static Optional<URI> locationFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(JOB_SOURCE_CODE_LOCATION))
          .map(facets -> (String) facets.getAdditionalProperties().get(URL))
          .map(URI::create);
    }

    static Optional<String> descriptionFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(DOCUMENTATION))
          .map(facet -> (String) facet.getAdditionalProperties().get(DESCRIPTION));
    }

    @SuppressWarnings("unchecked")
    static Optional<Dataset.Schema> schemaFor(@NonNull final OpenLineage.Dataset dataset) {
      return Optional.ofNullable(dataset.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(SCHEMA))
          .map(facets -> facets.getAdditionalProperties().get(SCHEMA_FIELDS))
          .map(facets -> (List<Map<?, ?>>) facets)
          .map(
              facets ->
                  facets.stream()
                      .map(
                          facet ->
                              Dataset.Schema.Field.builder()
                                  .name((String) facet.get(SCHEMA_FIELD_NAME))
                                  .type((String) facet.get(SCHEMA_FIELD_TYPE))
                                  .description((String) facet.get(SCHEMA_FIELD_DESCRIPTION))
                                  .build())
                      .collect(toImmutableSet()))
          .map(
              fields ->
                  // ...
                  (fields.isEmpty())
                      ? null // ...
                      : Dataset.Schema.builder().fields(fields).build());
    }

    static Dataset.Source sourceFor(@NonNull final OpenLineage.Dataset dataset) {
      final SourceName sourceName =
          Optional.ofNullable(dataset.getFacets())
              .map(facets -> facets.getAdditionalProperties().get(SOURCE))
              .map(facets -> (String) facets.getAdditionalProperties().get(SOURCE_NAME))
              .map(SourceName::of)
              .orElseThrow();
      final URI connectionUrl =
          Optional.ofNullable(dataset.getFacets())
              .map(facets -> facets.getAdditionalProperties().get(SOURCE))
              .map(facets -> (String) facets.getAdditionalProperties().get(SOURCE_CONNECTION_URL))
              .map(URI::create)
              .orElseThrow();

      return Dataset.Source.builder().name(sourceName).connectionUrl(connectionUrl).build();
    }
  }
}
