package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class JobRun {
  @NotNull private final Timestamp createdAt;
  @NotNull private final UUID runGuid;
  @NotNull private final List<String> runArgs;
  @NotNull private final Timestamp startedAt;
  @NotNull private final Timestamp endedAt;
  @NotNull private final UUID jobVersionGuid;
  @NotNull private final UUID inputDatasetVersionGuid;
  @NotNull private final UUID outputDatasetVersionGuid;
  @NotNull private final Timestamp latestHeartbeat;

  @JsonCreator
  public JobRun(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("runGuid") final UUID runGuid,
      @JsonProperty("runArgs") final List<String> runArgs,
      @JsonProperty("startedAt") final Timestamp startedAt,
      @JsonProperty("endedAt") final Timestamp endedAt,
      @JsonProperty("jobVersionGuid") final UUID jobVersionGuid,
      @JsonProperty("inputDatasetVersionGuid") final UUID inputDatasetVersionGuid,
      @JsonProperty("outputDatasetVersionGuid") final UUID outputDatasetVersionGuid,
      @JsonProperty("latestHeartbeat") final Timestamp latestHeartbeat) {
    this.createdAt = createdAt;
    this.runGuid = runGuid;
    this.runArgs = runArgs;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobVersionGuid = jobVersionGuid;
    this.inputDatasetVersionGuid = inputDatasetVersionGuid;
    this.outputDatasetVersionGuid = outputDatasetVersionGuid;
    this.latestHeartbeat = latestHeartbeat;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public UUID getRunGuid() {
    return runGuid;
  }

  public List<String> getRunArgs() {
    return runArgs;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public Timestamp getEndedAt() {
    return endedAt;
  }

  public UUID getJobVersionGuid() {
    return jobVersionGuid;
  }

  public UUID getInputDatasetVersionGuid() {
    return inputDatasetVersionGuid;
  }

  public UUID getOutputDatasetVersionGuid() {
    return outputDatasetVersionGuid;
  }

  public Timestamp getLatestHeartbeat() {
    return latestHeartbeat;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRun)) return false;

    final JobRun other = (JobRun) o;

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(runGuid, other.runGuid)
        && Objects.equals(runArgs, other.runArgs)
        && Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(jobVersionGuid, other.jobVersionGuid)
        && Objects.equals(inputDatasetVersionGuid, other.inputDatasetVersionGuid)
        && Objects.equals(outputDatasetVersionGuid, other.outputDatasetVersionGuid)
        && Objects.equals(latestHeartbeat, other.latestHeartbeat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        createdAt,
        runGuid,
        runArgs,
        startedAt,
        endedAt,
        jobVersionGuid,
        inputDatasetVersionGuid,
        outputDatasetVersionGuid,
        latestHeartbeat);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRun{");
    sb.append("createdAt=").append(createdAt);
    sb.append("runGuid=").append(runGuid);
    sb.append("runArgs=").append(runArgs);
    sb.append("startedAt=").append(startedAt);
    sb.append("endedAt=").append(endedAt);
    sb.append("jobVersionGuid=").append(jobVersionGuid);
    sb.append("inputDatasetVersionGuid=").append(inputDatasetVersionGuid);
    sb.append("outputDatasetVersionGuid=").append(outputDatasetVersionGuid);
    sb.append("latestHeartbeat=").append(latestHeartbeat);
    sb.append("}");
    return sb.toString();
  }
}
