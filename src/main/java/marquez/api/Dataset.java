package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class Dataset {
  @NotNull private final Timestamp createdAt;
  @NotNull private final String type;
  @NotNull private final String name;
  @NotNull private final Integer currentVersion;
  @NotNull private final String description;

  @JsonCreator
  public Dataset(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("type") final String type,
      @JsonProperty("name") final String name,
      @JsonProperty("current_version") final Integer currentVersion,
      @JsonProperty("description") final String description) {
    this.createdAt = createdAt;
    this.type = type;
    this.name = name;
    this.currentVersion = currentVersion;
    this.description = description;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public Integer getCurrentVersion() {
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

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(type, other.type)
        && Objects.equals(name, other.name)
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
    sb.append("createdAt=").append(createdAt);
    sb.append("type=").append(type);
    sb.append("name=").append(name);
    sb.append("currentVersion=").append(currentVersion);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
