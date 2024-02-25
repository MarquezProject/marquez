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
      final Instant startOrEndTime = Instant.now();
      final RunState state = RunState.forType(event.getEventType());
      final Metadata.Run.RunBuilder builder =
          Run.builder()
              .id(RunId.of(event.getRun().getRunId()))
              .state(RunState.forType(event.getEventType()))
              .transitionedOn(
                  event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant())
              .nominalStartTime(Instant.now())
              .nominalEndTime(Instant.now())
              .rawMeta(toJson(event))
              .producer(event.getProducer());
      // ...
      if (state.isDone()) {
        builder.endedAt(startOrEndTime);
      } else {
        builder.startedAt(startOrEndTime);
      }
      return builder.build();
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
      final OpenLineage.JobFacets facets = job.getFacets();
      final NamespaceName namespaceName = NamespaceName.of(job.getNamespace());
      return Job.builder()
          .name(JobName.of(event.getJob().getName()))
          .namespace(namespaceName)
          .description(
              (String)
                  facets
                      .getAdditionalProperties()
                      .get(DOCUMENTATION)
                      .getAdditionalProperties()
                      .get(DESCRIPTION))
          .location(
              toUrl(
                  (String)
                      facets
                          .getAdditionalProperties()
                          .get(SOURCE_CODE_LOCATION)
                          .getAdditionalProperties()
                          .get(URL)))
          .version(newJobVersionFor())
          .build();
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
  }

  @Builder
  public static class Dataset {
    public static Dataset newInstanceFor(@NonNull final OpenLineage.DatasetEvent event) {
      return new Dataset();
    }
  }
}
