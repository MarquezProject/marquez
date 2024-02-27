package marquez.api.v2.models;

import static marquez.common.Utils.newJobVersionFor;
import static marquez.common.Utils.toJson;
import static marquez.common.Utils.toUrl;

import com.google.common.collect.ImmutableSet;
import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.Version;

public class Metadata {
  @Builder
  public static class Run {
    @Getter final RunId id;
    @Getter final RunState state;
    @Getter final Instant transitionedOn;
    @Nullable final Instant startedAt;
    @Nullable final Instant endedAt;
    @Getter final Instant nominalStartTime;
    @Getter final Instant nominalEndTime;
    @Nullable final String externalId;

    @Getter final Job job;
    @Getter final IO io;

    @Getter final String rawMeta;
    @Getter final URI producer;

    public static Run newInstanceFor(@NonNull final OpenLineage.RunEvent event) {
      final RunId runId = RunId.of(event.getRun().getRunId());
      final RunState runState = RunState.forType(event.getEventType());
      final Instant runStartOrEndTime = Instant.now();
      final Metadata.Run.RunBuilder runBuilder =
          Run.builder()
              .id(runId)
              .state(runState)
              .transitionedOn(toUtc(event.getEventTime()))
              .nominalStartTime(null)
              .nominalEndTime(null)
              .rawMeta(toJson(event))
              .producer(event.getProducer());
      // ...
      if (runState.isDone()) {
        runBuilder.endedAt(runStartOrEndTime);
      } else {
        runBuilder.startedAt(runStartOrEndTime);
      }

      runBuilder.job(Job.newInstanceFor(event.getJob()));
      runBuilder.io(IO.newInstanceFor(event.getInputs(), event.getInputs()));

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
      return transitionedOn.withZoneSameInstant(ZoneId.of("UTC")).toInstant();
    }
  }

  @Builder
  public static class Job {
    private static final String DOCUMENTATION = "documentation";
    private static final String DESCRIPTION = "description";
    private static final String SOURCE_CODE_LOCATION = "sourceCodeLocation";
    private static final String URL = "url";

    @Getter final JobType type;
    @Getter final JobName name;
    @Getter final NamespaceName namespace;
    @Nullable final String description;
    @Nullable final URL location;
    @Getter final Version version;

    public static Job newInstanceFor(@NonNull final OpenLineage.JobEvent event) {
      final OpenLineage.Job job = event.getJob();
      final OpenLineage.JobFacets jobFacets = job.getFacets();
      final NamespaceName jobNamespace = NamespaceName.of(job.getNamespace());
      final JobName jobName = JobName.of(event.getJob().getName());
      final URL jobLocation =
          toUrl(
              (String)
                  jobFacets
                      .getAdditionalProperties()
                      .get(SOURCE_CODE_LOCATION)
                      .getAdditionalProperties()
                      .get(URL));
      return Job.builder()
          .name(jobName)
          .namespace(jobNamespace)
          .description(
              (String)
                  jobFacets
                      .getAdditionalProperties()
                      .get(DOCUMENTATION)
                      .getAdditionalProperties()
                      .get(DESCRIPTION))
          .location(jobLocation)
          .version(newJobVersionFor(jobNamespace, jobName, null, null, null))
          .build();
    }

    public static Job newInstanceFor(@NonNull final OpenLineage.Job job) {
      return null;
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<URL> getLocation() {
      return Optional.ofNullable(location);
    }
  }

  public static class IO {
    @Getter ImmutableSet<Dataset> inputs;
    @Getter ImmutableSet<Dataset> outputs;

    IO(@NonNull final ImmutableSet<Dataset> inputs, @NonNull final ImmutableSet<Dataset> outputs) {
      this.inputs = inputs;
      this.outputs = outputs;
    }

    @Builder
    public static class Dataset {
      public static Dataset newInstanceFor(@NonNull final OpenLineage.DatasetEvent event) {
        return new Dataset();
      }
    }

    public static IO newInstanceFor(
        @NonNull final List<OpenLineage.InputDataset> inputs,
        @NonNull final List<OpenLineage.InputDataset> outputs) {
      final ImmutableSet.Builder<Dataset> inputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.InputDataset input : inputs) {
        inputsBuilder.add(new Dataset());
      }
      final ImmutableSet.Builder<Dataset> outputsBuilder = ImmutableSet.builder();
      for (final OpenLineage.InputDataset output : outputs) {
        outputsBuilder.add(new Dataset());
      }

      return new IO(inputsBuilder.build(), outputsBuilder.build());
    }
  }
}
