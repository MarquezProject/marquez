package marquez.service.models;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.Data;

@Data
public class Namespace {

  private final UUID guid;
  private final String name;
  private final String ownerName;
  private final String description;
  private final Timestamp createdAt;

  public Namespace(UUID guid, String name, String ownerName, String description) {
    this.guid = guid;
    this.name = name;
    this.ownerName = ownerName;
    this.description = description;
    this.createdAt = null;
  }

  public Namespace(
      UUID guid, Timestamp createdAt, String name, String ownerName, String description) {
    this.guid = guid;
    this.createdAt = createdAt;
    this.name = name;
    this.ownerName = ownerName;
    this.description = description;
  }
}
