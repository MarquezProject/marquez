package marquez.api.v2.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.Utils.newJobVersionFor;
import static marquez.common.Utils.toJson;
import static marquez.common.Utils.toUrl;
import static marquez.common.models.DatasetType.DB_TABLE;

import com.google.common.collect.ImmutableSet;
import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;
import marquez.common.models.Version;

/** ... */
public final class Metadata {
  private Metadata() {}

  /** ... */
  @Builder
  public static class Run {
    @Getter private final RunId id;
    @Getter private final RunState state;
    @Getter private final Instant transitionedOn;
    @Nullable private final Instant startedAt;
    @Nullable private final Instant endedAt;
    @Getter private final Instant nominalStartTime;
    @Getter private final Instant nominalEndTime;
    @Nullable private final String externalId;

    @Getter private final Job job;
    @Getter private final IO io;

    @Getter private final String rawMeta;
    @Getter private final URI producer;

    /* ... */
    public static Run newInstanceFor(@NonNull final OpenLineage.RunEvent event) {
      final OpenLineage.Run run = event.getRun();
      final RunId runId = RunId.of(event.getRun().getRunId());
      final RunState runState = RunState.forType(event.getEventType());
      final Instant runTransitionedOnAsUtc = toUtc(event.getEventTime());
      final Run.RunBuilder runBuilder =
          Run.builder()
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
      runBuilder.job(Job.newInstanceFor(event.getJob()));
      runBuilder.io(IO.newInstanceFor(event.getInputs(), event.getOutputs()));

      return runBuilder.build();
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

    /** ... */
    static Instant toUtc(@NonNull final ZonedDateTime transitionedOn) {
      return transitionedOn.withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }
  }

  /** ... */
  @Builder
  public static class Job {
    @Getter private final JobType type;
    @Getter private final JobName name;
    @Getter private final NamespaceName namespace;
    @Getter private final JobVersionId versionId;
    @Nullable private final String description;
    @Nullable private final URL location;
    @Nullable private final IO io;

    /* ... */
    public static Job newInstanceFor(@NonNull final OpenLineage.JobEvent event) {
      final OpenLineage.Job job = event.getJob();
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      // ...
      final IO io = IO.newInstanceFor(event.getInputs(), event.getOutputs());
      final ImmutableSet<DatasetVersionId> inputs = IO.onlyVersionIdsFor(io.getInputs());
      final ImmutableSet<DatasetVersionId> outputs = IO.onlyVersionIdsFor(io.getOutputs());
      // ...
      final Optional<URL> location = Facets.locationFor(job);
      final Version jobVersion =
          Version.forJob(
              namespaceName, jobName, inputs, outputs, location.map(URL::toString).orElse(null));
      final JobVersionId jobVersionId =
          JobVersionId.of(namespaceName, jobName, jobVersion.getValue());

      return Job.builder()
          .type(JobType.BATCH)
          .name(jobName)
          .namespace(namespaceName)
          .versionId(jobVersionId)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(location.orElse(null))
          .io(io)
          .build();
    }

    /* ... */
    public static Job newInstanceFor(@NonNull final OpenLineage.Job job) {
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      final Optional<URL> location = Facets.locationFor(job);
      return Job.builder()
          .name(jobName)
          .namespace(namespaceName)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(location.orElse(null))
          .versionId(
              JobVersionId.of(
                  namespaceName,
                  jobName,
                  newJobVersionFor(
                          namespaceName,
                          jobName,
                          IO.EMPTY,
                          IO.EMPTY,
                          location.map(URL::toString).orElse(null))
                      .getValue()))
          .build();
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<URL> getLocation() {
      return Optional.ofNullable(location);
    }

    public Optional<IO> getIo() {
      return Optional.ofNullable(io);
    }
  }

  @Builder
  public static class Dataset {
    @Getter private final DatasetType type;
    @Getter private final DatasetName name;
    @Getter private final NamespaceName namespace;
    @Getter private final DatasetVersionId versionId;
    @Getter private final Schema schema;
    @Getter private final Source source;

    public static Dataset newInstanceFor(@NonNull final OpenLineage.DatasetEvent event) {
      return newInstanceFor(event.getDataset());
    }

    static Dataset newInstanceFor(@NonNull final OpenLineage.Dataset dataset) {
      final DatasetName datasetName = DatasetName.of(dataset.getName());
      final NamespaceName namespaceName = NamespaceName.of(dataset.getNamespace());
      return Dataset.builder()
          .type(DB_TABLE)
          .name(datasetName)
          .namespace(namespaceName)
          .schema(Facets.schemaFor(dataset))
          .source(Facets.sourceFor(dataset))
          .build();
    }

    @Builder
    public static class Schema {
      @Nullable private final ImmutableSet<Dataset.Schema.Field> fields;

      public Optional<ImmutableSet<Dataset.Schema.Field>> getFields() {
        return Optional.ofNullable(fields);
      }

      @Builder
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
    public static class Source {
      @Getter private final SourceName name;
      @Getter private final URL connectionUrl;
    }
  }

  public static class IO {
    static final ImmutableSet<DatasetId> EMPTY = ImmutableSet.of();

    @Getter ImmutableSet<Dataset> inputs;
    @Getter ImmutableSet<Dataset> outputs;

    IO(@NonNull final ImmutableSet<Dataset> inputs, @NonNull final ImmutableSet<Dataset> outputs) {
      this.inputs = inputs;
      this.outputs = outputs;
    }

    public static IO newInstanceFor(
        @NonNull final List<OpenLineage.InputDataset> inputs,
        @NonNull final List<OpenLineage.OutputDataset> outputs) {
      final ImmutableSet.Builder<Dataset> inputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.InputDataset input : inputs) {
        inputsBuilder.add(Dataset.newInstanceFor(input));
      }
      final ImmutableSet.Builder<Dataset> outputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.OutputDataset output : outputs) {
        outputsBuilder.add(Dataset.newInstanceFor(output));
      }

      return new IO(inputsBuilder.build(), outputsBuilder.build());
    }

    static ImmutableSet<DatasetVersionId> onlyVersionIdsFor(
        @NonNull final ImmutableSet<Dataset> datasets) {
      return datasets.stream().map(Dataset::getVersionId).collect(toImmutableSet());
    }
  }

  static class Facets {
    static final String DOCUMENTATION = "documentation";
    static final String DESCRIPTION = "description";

    static final String SOURCE_CODE_LOCATION = "sourceCodeLocation";
    static final String URL = "url";

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

    static Optional<URL> locationFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(SOURCE_CODE_LOCATION))
          .map(facets -> (String) facets.getAdditionalProperties().get(URL))
          .map(facet -> toUrl(facet));
    }

    static Optional<String> descriptionFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets())
          .map(facets -> facets.getAdditionalProperties().get(DOCUMENTATION))
          .map(facet -> (String) facet.getAdditionalProperties().get(DESCRIPTION));
    }

    static Dataset.Schema schemaFor(@NonNull final OpenLineage.Dataset dataset) {
      return Dataset.Schema.builder()
          .fields(
              Optional.ofNullable(dataset.getFacets())
                  .map(facets -> facets.getAdditionalProperties().get(SCHEMA))
                  .map(facets -> facets.getAdditionalProperties().get(SCHEMA_FIELDS))
                  .map(facets -> (List<Map<?, ?>>) facets)
                  .map(
                      facets ->
                          facets.stream()
                              .map(
                                  facet ->
                                      Dataset.Schema.Field.builder()
                                          .type((String) facet.get(SCHEMA_FIELD_NAME))
                                          .name((String) facet.get(SCHEMA_FIELD_TYPE))
                                          .description((String) facet.get(SCHEMA_FIELD_DESCRIPTION))
                                          .build())
                              .collect(toImmutableSet()))
                  .orElse(null))
          .build();
    }

    static Dataset.Source sourceFor(@NonNull final OpenLineage.Dataset dataset) {
      final SourceName sourceName =
          Optional.ofNullable(dataset.getFacets())
              .map(facets -> facets.getAdditionalProperties().get(SOURCE))
              .map(facets -> (String) facets.getAdditionalProperties().get(SOURCE_NAME))
              .map(facet -> SourceName.of(facet))
              .orElseThrow();
      final URL connectionUrl =
          Optional.ofNullable(dataset.getFacets())
              .map(facets -> facets.getAdditionalProperties().get(SOURCE))
              .map(facets -> (String) facets.getAdditionalProperties().get(SOURCE_CONNECTION_URL))
              .map(facet -> toUrl(facet))
              .orElseThrow();

      return Dataset.Source.builder().name(sourceName).connectionUrl(connectionUrl).build();
    }
  }
}
