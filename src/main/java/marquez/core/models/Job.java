package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public final class Job {
  @NonNull private final UUID guid;
  @NonNull private final String name;
  @NonNull private final String location;
  @NonNull private final UUID namespaceGuid;
  private String description;
  private Timestamp createdAt = null;

  public Job(
      final UUID guid,
      final String name,
      final String location,
      final UUID namespaceGuid,
      final String description) {
    this.guid = guid;
    this.name = name;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
    this.description = description;
  }
}
