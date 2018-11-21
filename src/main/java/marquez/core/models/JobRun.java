package marquez.core.models;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class JobRun {

  private final UUID guid;
  private final Timestamp startedAt;
  private final Timestamp endedAt;
  private final Integer currentState;
  private final UUID jobVersionGuid;
  private final String runArgsHexDigest;
  private final String runArgs;

  private static Map<JobRunState.State, Set<JobRunState.State>> validTransitions = new HashMap<>();

  static {
    validTransitions.put(
        JobRunState.State.NEW,
        new HashSet<JobRunState.State>() {
          {
            add(JobRunState.State.RUNNING);
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
      final Integer currentState,
      final UUID jobVersionGuid,
      final String runArgsHexDigest,
      final String runArgs) {
    this.guid = guid;
    this.startedAt = null;
    this.endedAt = null;
    this.currentState = currentState;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsHexDigest = runArgsHexDigest;
    this.runArgs = runArgs;
  }

  public JobRun(
      final UUID guid,
      final Timestamp startedAt,
      final Timestamp endedAt,
      final Integer currentState,
      final UUID jobVersionGuid,
      final String runArgsHexDigest,
      final String runArgs) {
    this.guid = guid;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.currentState = currentState;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsHexDigest = runArgsHexDigest;
    this.runArgs = runArgs;
  }

  public UUID getGuid() {
    return guid;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public Timestamp getEndedAt() {
    return endedAt;
  }

  public Integer getCurrentState() {
    return currentState;
  }

  public UUID getJobVersionGuid() {
    return jobVersionGuid;
  }
  
  public String runArgsHexDigest() {
    return runArgsHexDigest;
  }

  public String runArgs(){
    return runArgs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRun)) return false;

    final JobRun other = (JobRun) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(currentState, other.currentState)
        && Objects.equals(jobVersionGuid, other.jobVersionGuid)
        && Objects.equals(runArgsHexDigest, other.runArgsHexDigest);
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
    return Objects.hash(guid, startedAt, endedAt, currentState, jobVersionGuid, runArgsHexDigest);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRun{");
    sb.append("guid=").append(guid);
    sb.append("startedAt=").append(startedAt);
    sb.append("endedAt=").append(endedAt);
    sb.append("currentState=").append(currentState);
    sb.append("jobVersionGuid=").append(jobVersionGuid);
    sb.append("runArgsHexDigest=").append(runArgsHexDigest);
    sb.append("}");
    return sb.toString();
  }
}
