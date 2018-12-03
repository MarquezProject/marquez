package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;

@Data
public final class JobRun {

  private final UUID guid;

  private final Integer currentState;
  private final UUID jobVersionGuid;
  private final String runArgsHexDigest;
  private final String runArgs;
  private final Timestamp nominalStartTime;
  private final Timestamp nominalEndTime;
  private final Timestamp createdAt;

  public JobRun(
      final UUID guid,
      final Integer currentState,
      final UUID jobVersionGuid,
      final String runArgsHexDigest,
      final String runArgs,
      final Timestamp nominalStartTime,
      final Timestamp nominalEndTime,
      final Timestamp createdAt) {
    this.guid = guid;
    this.currentState = currentState;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsHexDigest = runArgsHexDigest;
    this.runArgs = runArgs;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.createdAt = createdAt;
  }
}
