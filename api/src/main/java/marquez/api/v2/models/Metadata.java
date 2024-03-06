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
import java.util.Collections;
import java.util.List;
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

public class Metadata {
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

    @Getter private final Metadata.Job job;
    @Getter private final Metadata.IO io;

    @Getter private final String rawMeta;
    @Getter private final URI producer;

    public static Run newInstanceFor(@NonNull final OpenLineage.RunEvent event) {
      final OpenLineage.Run run = event.getRun();
      final RunId runId = RunId.of(event.getRun().getRunId());
      final RunState runState = RunState.forType(event.getEventType());
      final Instant runTransitionedOnAsUtc = toUtc(event.getEventTime());
      final Instant runStartOrEndTimeAsUtc = Instant.now();
      final Metadata.Run.RunBuilder runBuilder =
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
        runBuilder.endedAt(runStartOrEndTimeAsUtc);
      } else {
        runBuilder.startedAt(runStartOrEndTimeAsUtc);
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

  @Builder
  public static class Job {
    static final ImmutableSet<DatasetId> NO_INPUTS = ImmutableSet.of();
    static final ImmutableSet<DatasetId> NO_OUTPUTS = ImmutableSet.of();

    @Getter private final JobType type;
    @Getter private final JobName name;
    @Getter private final NamespaceName namespace;
    @Nullable private final String description;
    @Nullable private final URL location;
    @Getter private final JobVersionId versionId;
    @Nullable private final Metadata.IO io;

    public static Job newInstanceFor(@NonNull final OpenLineage.JobEvent event) {
      final OpenLineage.Job job = event.getJob();
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      final Optional<URL> locationOrNull = Facets.locationFor(job);
      final Metadata.IO io = IO.newInstanceFor(event.getInputs(), event.getOutputs());
      return Job.builder()
          .name(jobName)
          .namespace(namespaceName)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(locationOrNull.orElse(null))
          .io(io)
          .versionId(
              JobVersionId.of(
                  namespaceName,
                  jobName,
                  newJobVersionFor(
                          namespaceName,
                          jobName,
                          io.getInputs().stream()
                              .map(Dataset::getVersionId)
                              .collect(toImmutableSet()),
                          io.getOutputs().stream()
                              .map(Dataset::getVersionId)
                              .collect(toImmutableSet()),
                          locationOrNull.map(URL::toString).orElse(null))
                      .getValue()))
          .build();
    }

    static Job newInstanceFor(@NonNull final OpenLineage.Job job) {
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(job.getName());
      final Optional<URL> locationOrNull = Facets.locationFor(job);
      return Job.builder()
          .name(jobName)
          .namespace(namespaceName)
          .description(Facets.descriptionFor(job).orElse(null))
          .location(locationOrNull.orElse(null))
          .versionId(
              JobVersionId.of(
                  namespaceName,
                  jobName,
                  newJobVersionFor(
                          namespaceName,
                          jobName,
                          NO_INPUTS,
                          NO_OUTPUTS,
                          locationOrNull.map(URL::toString).orElse(null))
                      .getValue()))
          .build();
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<URL> getLocation() {
      return Optional.ofNullable(location);
    }

    public Optional<Metadata.IO> getIo() {
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

    public static Dataset newInstanceFor(@NonNull final OpenLineage.DatasetEvent event) {
      OpenLineage.Dataset dataset = event.getDataset();
      final DatasetName datasetName = DatasetName.of(dataset.getName());
      final NamespaceName namespaceName = NamespaceName.of(dataset.getNamespace());
      return Dataset.builder()
          .type(DB_TABLE)
          .name(datasetName)
          .namespace(namespaceName)
          .schema(F)
          .build();
    }

    @Builder
    public static class Schema {
      @Getter private final ImmutableSet<Metadata.Dataset.Schema.Field> fields;

      @Builder
      public static class Field {}
    }
  }

  public static class IO {
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
        inputsBuilder.add(new Dataset());
      }
      final ImmutableSet.Builder<Dataset> outputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.OutputDataset output : outputs) {
        outputsBuilder.add(new Dataset());
      }

      return new IO(inputsBuilder.build(), outputsBuilder.build());
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

    Facets() {}

    static Optional<Instant> nominalStartTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets().getAdditionalProperties().get(RUN_NOMINAL_TIME))
          .map(facets -> facets.getAdditionalProperties())
          .map(facet -> (String) facet.get(RUN_NOMINAL_START_TIME))
          .map(value -> ZonedDateTime.parse(value).withZoneSameInstant(ZoneOffset.UTC).toInstant());
    }

    static Optional<Instant> nominalEndTimeFor(@NonNull final OpenLineage.Run run) {
      return Optional.ofNullable(run.getFacets().getAdditionalProperties().get(RUN_NOMINAL_TIME))
          .map(facets -> facets.getAdditionalProperties())
          .map(facet -> (String) facet.get(RUN_NOMINAL_END_TIME))
          .map(value -> ZonedDateTime.parse(value).withZoneSameInstant(ZoneOffset.UTC).toInstant());
    }

    static Optional<URL> locationFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(
              job.getFacets().getAdditionalProperties().get(SOURCE_CODE_LOCATION))
          .map(facets -> facets.getAdditionalProperties())
          .map(facet -> (String) facet.get(URL))
          .map(value -> toUrl(value));
    }

    static Optional<String> descriptionFor(@NonNull final OpenLineage.Job job) {
      return Optional.ofNullable(job.getFacets().getAdditionalProperties().get(DOCUMENTATION))
          .map(facets -> facets.getAdditionalProperties())
          .map(facet -> (String) facet.get(DESCRIPTION));
    }

    static Optional<Metadata.Dataset.Schema> schemaFor(@NonNull final OpenLineage.Dataset dataset) {
      return Optional.ofNullable(dataset.getFacets().getAdditionalProperties().get(SCHEMA))
          .map(facets -> facets.getAdditionalProperties())
          .stream()
          .findFirst()
          .map(facet -> Collections.singletonList(facet.get(SCHEMA_FIELDS)))
          .stream()
          .forEach(value -> value.get);
    }
  }
}
