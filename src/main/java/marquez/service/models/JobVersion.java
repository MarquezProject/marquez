package marquez.service.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class JobVersion {
  private final UUID guid;
  private final UUID jobGuid;
  private final String uri;
  private final UUID version;
  private final UUID latestJobRunGuid;
  private final Timestamp createdAt;
  private final Timestamp updatedAt;
}
