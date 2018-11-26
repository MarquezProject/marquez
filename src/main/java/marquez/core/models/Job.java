package marquez.core.models;

import java.util.UUID;
import lombok.Data;

@Data
public final class Job {
  private final UUID guid;
  private final String name;
  private final String description;
  private final String location;
  private final UUID namespaceGuid;

  public Job(final UUID guid, final String name, final String location, final UUID namespaceGuid) {
    this.guid = guid;
    this.name = name;
    this.description = null;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
  }

  public Job(
      final UUID guid,
      final String name,
      final String description,
      final String location,
      final UUID namespaceGuid) {
    this.guid = guid;
    this.name = name;
    this.description = description;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
  }
}
