package marquez.api;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public final class Job {

  private final UUID guid;
  private final String name;
  private final String ownerName;
  private final Timestamp nominalTime;
  private final String category;
  private final String description;

  public Job(
      final UUID guid,
      final String name,
      final String ownerName,
      final Timestamp nominalTime,
      final String category,
      final String description) {
    this.guid = guid;
    this.name = name;
    this.ownerName = ownerName;
    this.nominalTime = nominalTime;
    this.category = category;
    this.description = description;
  }

  public UUID getGuid() {
    return guid;
  }

  public String getName() {
    return name;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public Timestamp getNominalTime() {
    return nominalTime;
  }

  public String getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Job)) return false;

    final Job other = (Job) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(name, other.name)
        && Objects.equals(ownerName, other.ownerName)
        && Objects.equals(nominalTime, other.nominalTime)
        && Objects.equals(category, other.category)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, name, ownerName, nominalTime, category, description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Job{");
    sb.append("guid=").append(guid);
    sb.append(",name=").append(name);
    sb.append(",owner=").append(ownerName);
    sb.append(",nominalTime=").append(nominalTime);
    sb.append(",category=").append(category);
    sb.append(",description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
