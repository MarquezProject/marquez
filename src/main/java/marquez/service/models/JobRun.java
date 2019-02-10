package marquez.service.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class JobRun {
  private final UUID guid;
  private final Integer currentState;
  private final UUID jobVersionGuid;
  private final String runArgsHexDigest;
  private final String runArgs;
  private final Timestamp nominalStartTime;
  private final Timestamp nominalEndTime;
  private final Timestamp createdAt;
}
