package marquez.api.v2.models;

import static marquez.common.Utils.toJson;

import com.google.common.collect.ImmutableSet;
import io.openlineage.server.OpenLineage;
import java.net.URI;
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

    @Getter final String rawData;
    @Getter final URI producer;

    public static Run newInstanceFor(@NonNull final OpenLineage.RunEvent event) {
      return Run.builder()
          .id(RunId.of(event.getRun().getRunId()))
          .state(RunState.from(event.getEventType()))
          .transitionedOn(event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant())
          .startedAt(Instant.now())
          .endedAt(Instant.now())
          .nominalStartTime(Instant.now())
          .nominalEndTime(Instant.now())
          .rawData(toJson(event))
          .producer(event.getProducer())
          .build();
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
    @Getter final JobType type;
    @Getter final JobName name;
    @Getter final NamespaceName namespace;
    @Nullable final String description;
    @Nullable final URI location;
    @Getter final Version version;

    public static Job newInstanceFor(@NonNull final OpenLineage.JobEvent event) {
      return Job.builder()
          .name(JobName.of(event.getJob().getName()))
          .namespace(NamespaceName.of(event.getJob().getNamespace()))
          .build();
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(description);
    }

    public Optional<URI> getLocation() {
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
