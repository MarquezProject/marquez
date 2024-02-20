package marquez.api.models.v2;

import static marquez.common.Utils.toJson;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.Version;

@AllArgsConstructor
@Builder
public final class RunLevelLineageMetadata {
  @Getter final RunId runId;
  @Getter final RunState runState;
  @Getter final UUID runStateUuid = UUID.randomUUID();
  @Getter final Instant runTransitionedOn;
  @Getter final Instant runStartedAt;
  @Getter final Instant runEndedAt;
  @Getter final Instant runNominalStartTime;
  @Getter final Instant runNominalEndTime;
  final String runExternalId;

  @Getter final UUID jobUuid = UUID.randomUUID();
  @Getter final UUID jobNamespaceUuid = UUID.randomUUID();
  @Getter final NamespaceName jobNamespace;

  @Getter
  final Version jobVersion = Version.of(UUID.fromString("c83d94ca-9c56-499c-9dc7-85a20a411c02"));

  @Getter final JobName jobName;
  @Getter final JobType jobType;
  final String jobDescription;
  final URI jobLocation;

  @Getter final String rawData;
  @Getter final URI producer;

  public static RunLevelLineageMetadata newInstanceFor(@NotNull OpenLineage.RunEvent olRunEvent) {
    final OpenLineage.Run run = olRunEvent.getRun();
    final OpenLineage.Job job = olRunEvent.getJob();
    return RunLevelLineageMetadata.builder()
        .runId(RunId.of(run.getRunId()))
        .runState(RunState.from(olRunEvent.getEventType()))
        .runTransitionedOn(
            olRunEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant())
        .runEndedAt(Instant.now())
        .runNominalStartTime(Instant.now())
        .runNominalEndTime(Instant.now())
        .jobType(JobType.BATCH)
        .jobNamespace(NamespaceName.of(job.getNamespace()))
        .jobName(JobName.of(job.getName()))
        .rawData(toJson(olRunEvent))
        .producer(olRunEvent.getProducer())
        .build();
  }

  public Optional<Instant> getRunStartedAt() {
    return Optional.ofNullable(runStartedAt);
  }

  public Optional<Instant> getRunEndedAt() {
    return Optional.ofNullable(runEndedAt);
  }

  public Optional<String> getRunExternalId() {
    return Optional.ofNullable(runExternalId);
  }

  public Optional<String> getJobDescription() {
    return Optional.ofNullable(jobDescription);
  }

  public Optional<URI> getJobLocation() {
    return Optional.ofNullable(jobLocation);
  }
}
