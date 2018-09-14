package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class JobRun {

  @JsonIgnore private final UUID guid;
  private final Timestamp createdAt;
  private final Timestamp startedAt;
  private final Timestamp endedAt;
  private final UUID jobRunDefinitionGuid;
  private final Integer currentState;

  private static Map<JobRunState.State, Set<JobRunState.State>> validTransitions = new HashMap<>();

  static {
    validTransitions.put(
        JobRunState.State.NEW,
        new HashSet<JobRunState.State>() {
          {
            add(JobRunState.State.RUNNING);
            add(JobRunState.State.ABORTED);
          }
        });
    validTransitions.put(
        JobRunState.State.RUNNING,
        new HashSet<JobRunState.State>() {
          {
            add(JobRunState.State.COMPLETED);
            add(JobRunState.State.FAILED);
            add(JobRunState.State.ABORTED);
          }
        });
    validTransitions.put(JobRunState.State.FAILED, new HashSet<>());
    validTransitions.put(JobRunState.State.COMPLETED, new HashSet<>());
    validTransitions.put(JobRunState.State.ABORTED, new HashSet<>());
  }

  public JobRun(
      final UUID guid,
      final Timestamp createdAt,
      final Timestamp startedAt,
      final Timestamp endedAt,
      final UUID jobRunDefinitionGuid,
      final Integer currentState) {
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

  public UUID getJobRunDefinitionGuid() {
    return jobRunDefinitionGuid;
  }

  public Integer getCurrentState() {
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

  public static boolean isValidJobTransition(
      JobRunState.State oldState, JobRunState.State newState) {
    return validTransitions.get(oldState).contains(newState);
  }

  public static boolean isValidJobTransition(Integer oldState, Integer newState) {
    return isValidJobTransition(
        JobRunState.State.fromInt(oldState), JobRunState.State.fromInt(newState));
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
