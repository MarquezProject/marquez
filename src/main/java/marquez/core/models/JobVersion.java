package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;

@Data
public final class JobVersion {

  private final UUID guid;
  private final UUID jobGuid;
  private final String uri;
  private final UUID version;
  private final UUID latestJobRunGuid;
  private final Timestamp createdAt;
  private final Timestamp updatedAt;

  public JobVersion(
      final UUID guid,
      final UUID jobGuid,
      final String uri,
      final UUID version,
      final UUID latestJobRunGuid,
      final Timestamp createdAt,
      final Timestamp updatedAt) {
    this.guid = guid;
    this.jobGuid = jobGuid;
    this.uri = uri;
    this.latestJobRunGuid = latestJobRunGuid;
    this.version = version;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
