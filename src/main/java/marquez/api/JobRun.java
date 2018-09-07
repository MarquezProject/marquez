package marquez.api;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public final class JobRun {

  private final UUID guid;
  private final Timestamp createdAt;
  private final Timestamp startedAt;
  private final Timestamp endedAt;
  private final UUID jobRunDefinitionGuid;
  private final JobRunState.State currentState;

  public JobRun(
      final UUID guid,
      final Timestamp createdAt,
      final Timestamp startedAt,
      final Timestamp endedAt,
      final UUID jobRunDefinitionGuid,
      final JobRunState.State currentState) {
    this.guid = guid;
    this.createdAt = createdAt;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobRunDefinitionGuid = jobRunDefinitionGuid;
    this.currentState = currentState;
  }

  public UUID getGuid() {
    return guid;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public Timestamp getEndedAt() {
    return endedAt;
  }

  public UUID getJobRunDefinitionGuid() {
    return jobRunDefinitionGuid;
  }

  public JobRunState.State getCurrentState() {
    return currentState;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRun)) return false;

    final JobRun other = (JobRun) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(jobRunDefinitionGuid, other.jobRunDefinitionGuid)
        && Objects.equals(currentState, other.currentState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, createdAt, startedAt, endedAt, jobRunDefinitionGuid, currentState);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRun{");
    sb.append("guid=").append(guid);
    sb.append("createdAt=").append(createdAt);
    sb.append("startedAt=").append(startedAt);
    sb.append("endedAt=").append(endedAt);
    sb.append("jobRunDefinitionGuid=").append(jobRunDefinitionGuid);
    sb.append("currentState=").append(currentState);
    sb.append("}");
    return sb.toString();
  }
}
