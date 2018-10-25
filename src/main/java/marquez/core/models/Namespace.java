package marquez.core.models;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class Namespace {

  private final UUID guid;
  private final String name;
  private final String ownerName;
  private final String description;
  private final Timestamp createdAt;

  public Namespace(
      UUID guid, Timestamp createdAt, String name, String ownerName, String description) {
    this.guid = guid;
    this.createdAt = createdAt;
    this.name = name;
    this.ownerName = ownerName;
    this.description = description;
  }

  public UUID getGuid() {
    return guid;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public String getName() {
    return name;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Namespace)) return false;

    final Namespace other = (Namespace) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(name, other.name)
        && Objects.equals(ownerName, other.ownerName)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, createdAt, name, ownerName, description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Namespace{");
    sb.append("guid=").append(guid);
    sb.append(",createdAt=").append(createdAt);
    sb.append(",name=").append(name);
    sb.append(",owner=").append(ownerName);
    sb.append(",description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
