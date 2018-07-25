package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class JobRun {
  @NotNull private final Timestamp createdAt;
  @NotNull private final String jobRunId;
  @NotNull private final String jobRunArgs;
  @NotNull private final Timestamp startedAt;
  @NotNull private final Timestamp endedAt;
  @NotNull private final long jobVersionId;
  @NotNull private final long inputDatasetVersionId;
  @NotNull private final long outputDatasetVersionId;
  @NotNull private final Timestamp latestHeartbeat;

  @JsonCreator
  public JobRun(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("job_run_id") final String jobRunId,
      @JsonProperty("job_run_args") final String jobRunArgs,
      @JsonProperty("started_at") final Timestamp startedAt,
      @JsonProperty("ended_at") final Timestamp endedAt,
      @JsonProperty("job_version_id") final long jobVersionId,
      @JsonProperty("input_dataset_version_id") final long inputDatasetVersionId,
      @JsonProperty("output_dataset_version_id") final long outputDatasetVersionId,
      @JsonProperty("latest_heartbeat") final Timestamp latestHeartbeat) {
    this.createdAt = createdAt;
    this.jobRunId = jobRunId;
    this.jobRunArgs = jobRunArgs;
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

  public String getJobRunId() {
    return jobRunId;
  }

  public String getJobRunArgs() {
    return jobRunArgs;
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
        && Objects.equals(jobRunId, other.jobRunId)
        && Objects.equals(jobRunArgs, other.jobRunArgs)
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
        jobRunId,
        jobRunArgs,
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
    sb.append("jobRunId=").append(jobRunId);
    sb.append("jobRunArgs=").append(jobRunArgs);
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
