package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;

@Data
public final class Job {
  private final UUID guid;
  private final String name;
  private final String description;
  private final String location;
  private final UUID namespaceGuid;
  private final Timestamp createdAt;

  public Job(
      final UUID guid,
      final String name,
      final String description,
      final String location,
      final UUID namespaceGuid,
      final Timestamp createdAt) {
    this.guid = guid;
    this.name = name;
    this.description = description;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
    this.createdAt = createdAt;
  }
}
