package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class JobRun {

  @JsonIgnore private final UUID guid;
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp startedAt;
  @NotNull private final Timestamp endedAt;
  private final UUID jobRunDefinitionGuid;
  private final JobRunState.State currentState;

  @JsonCreator
  public JobRun(
      final UUID guid,
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("startedAt") final Timestamp startedAt,
      @JsonProperty("endedAt") final Timestamp endedAt,
      @JsonProperty("jobRunDefinitionGuid") final UUID jobRunDefinitionGuid,
      @JsonProperty("currentState") final JobRunState.State currentState) {
    this.guid = guid;
    this.createdAt = createdAt;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobRunDefinitionGuid = jobRunDefinitionGuid;
    this.currentState = currentState;
  }

  @JsonIgnore
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRun)) return false;

    final JobRun other = (JobRun) o;

    return Objects.equals(createdAt, other.createdAt)
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
    sb.append("endedAt=").append(jobRunDefinitionGuid);
    sb.append("endedAt=").append(currentState);
    sb.append("}");
    return sb.toString();
  }
}
