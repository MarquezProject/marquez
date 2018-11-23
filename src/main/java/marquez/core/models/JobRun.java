package marquez.core.models;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
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
}
