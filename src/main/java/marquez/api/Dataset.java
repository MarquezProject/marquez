package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class Dataset {
  public enum Type {
    DB_TABLE,
    ICEBERG_TABLE
  }

  public enum Origin {
    EXTERNAL,
    INTERNAL
  }

  @NotNull private final String name;
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final Type type;
  @NotNull private final Origin origin;
  @NotNull private final UUID currentVersion;
  @NotNull private final String description;

  @JsonCreator
  public Dataset(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("updatedAt") final Timestamp updatedAt,
      @JsonProperty("type") final Type type,
      @JsonProperty("origin") final Origin origin,
      @JsonProperty("currentVersion") final UUID currentVersion,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.type = type;
    this.origin = origin;
    this.currentVersion = currentVersion;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Type getType() {
    return type;
  }

  public Origin getOrigin() {
    return origin;
  }

  public UUID getCurrentVersion() {
    return currentVersion;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Dataset)) return false;

    final Dataset other = (Dataset) o;

    return Objects.equals(name, other.name)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(updatedAt, other.updatedAt)
        && Objects.equals(type, other.type)
        && Objects.equals(origin, other.origin)
        && Objects.equals(currentVersion, other.currentVersion)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, type, name, currentVersion, description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Dataset{");
    sb.append("name=").append(name);
    sb.append("createdAt=").append(createdAt);
    sb.append("updatedAt=").append(updatedAt);
    sb.append("type=").append(type);
    sb.append("origin=").append(origin);
    sb.append("currentVersion=").append(currentVersion);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
