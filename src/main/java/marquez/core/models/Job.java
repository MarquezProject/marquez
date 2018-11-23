package marquez.core.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;

@Data
public final class Job {
  private final UUID guid;
  private final String name;
  private final String ownerName;
  private final Timestamp nominalTime;
  private final String category;
  private final String description;
  private final String location;
  private final UUID namespaceGuid;

  public Job(
      final UUID guid,
      final String name,
      final String ownerName,
      final String location,
      final UUID namespaceGuid) {
    this.guid = guid;
    this.name = name;
    this.ownerName = ownerName;
    this.nominalTime = null;
    this.category = null;
    this.description = null;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
  }

  public Job(
      final UUID guid,
      final String name,
      final String ownerName,
      final Timestamp nominalTime,
      final String category,
      final String description,
      final String location,
      final UUID namespaceGuid) {
    this.guid = guid;
    this.name = name;
    this.ownerName = ownerName;
    this.nominalTime = nominalTime;
    this.category = category;
    this.description = description;
    this.location = location;
    this.namespaceGuid = namespaceGuid;
  }
}
