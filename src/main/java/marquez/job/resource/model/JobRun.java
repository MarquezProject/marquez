package marquez.job.resource.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.List;
import javax.validation.constraints.NotNull;

public final class JobRun {
  @NotNull private final Timestamp createdAt;
  @NotNull private final String runId;
  @NotNull private final List<String> runArgs;
  @NotNull private final Timestamp startedAt;
  @NotNull private final Timestamp endedAt;
  @NotNull private final long jobVersionId;
  @NotNull private final long inputDatasetVersionId;
  @NotNull private final long outputDatasetVersionId;
  @NotNull private final Timestamp latestHeartbeat;

  @JsonCreator
  public JobRun(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("runId") final String runId,
      @JsonProperty("runArgs") final List<String> runArgs,
      @JsonProperty("startedAt") final Timestamp startedAt,
      @JsonProperty("endedAt") final Timestamp endedAt,
      @JsonProperty("jobVersionId") final long jobVersionId,
      @JsonProperty("inputDatasetVersionId") final long inputDatasetVersionId,
      @JsonProperty("outputDatasetVersionId") final long outputDatasetVersionId,
      @JsonProperty("latestHeartbeat") final Timestamp latestHeartbeat) {
    this.createdAt = createdAt;
    this.runId = runId;
    this.runArgs = runArgs;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobVersionId = jobVersionId;
    this.inputDatasetVersionId = inputDatasetVersionId;
    this.outputDatasetVersionId = outputDatasetVersionId;
    this.latestHeartbeat = latestHeartbeat;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public String getRunId() {
    return runId;
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

  public long getJobVersionId() {
    return jobVersionId;
  }

  public long getInputDatasetVersionId() {
    return inputDatasetVersionId;
  }

  public long getOutputDatasetVersionId() {
    return outputDatasetVersionId;
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
        && Objects.equals(runId, other.runId)
        && Objects.equals(runArgs, other.runArgs)
        && Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(jobVersionId, other.jobVersionId)
        && Objects.equals(inputDatasetVersionId, other.inputDatasetVersionId)
        && Objects.equals(outputDatasetVersionId, other.outputDatasetVersionId)
        && Objects.equals(latestHeartbeat, other.latestHeartbeat);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        createdAt,
        runId,
        runArgs,
        startedAt,
        endedAt,
        jobVersionId,
        inputDatasetVersionId,
        outputDatasetVersionId,
        latestHeartbeat);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRun{");
    sb.append("createdAt=").append(createdAt);
    sb.append("runId=").append(runId);
    sb.append("runArgs=").append(runArgs);
    sb.append("startedAt=").append(startedAt);
    sb.append("endedAt=").append(endedAt);
    sb.append("jobVersionId=").append(jobVersionId);
    sb.append("inputDatasetVersionId=").append(inputDatasetVersionId);
    sb.append("outputDatasetVersionId=").append(outputDatasetVersionId);
    sb.append("latestHeartbeat=").append(latestHeartbeat);
    sb.append("}");
    return sb.toString();
  }
}
