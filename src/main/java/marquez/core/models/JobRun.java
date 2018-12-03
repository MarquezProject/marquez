package marquez.core.models;

import java.sql.Timestamp;
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

  public JobRun(
      final UUID guid,
      final Integer currentState,
      final UUID jobVersionGuid,
      final String runArgsHexDigest,
      final String runArgs) {
    this.guid = guid;
    this.currentState = currentState;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsHexDigest = runArgsHexDigest;
    this.runArgs = runArgs;
    this.startedAt = null;
    this.endedAt = null;
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
