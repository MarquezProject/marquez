package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class Job {
  @NotNull private final String name;
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final long currentVersion;
  @NotNull private final long currentOwnership;
  @NotNull private final Timestamp nominalTime;
  @NotNull private final String category;
  @NotNull private final String description;

  @JsonCreator
  public Job(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("updatedAt") final Timestamp updatedAt,
      @JsonProperty("currentVersion") final long currentVersion,
      @JsonProperty("currentOwnership") final long currentOwnership,
      @JsonProperty("nominalTime") final Timestamp nominalTime,
      @JsonProperty("category") final String category,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.currentVersion = currentVersion;
    this.currentOwnership = currentOwnership;
    this.nominalTime = nominalTime;
    this.category = category;
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

  public long getCurrentVersion() {
    return currentVersion;
  }

  public long getCurrentOwnership() {
    return currentOwnership;
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

    return Objects.equals(name, other.name)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(updatedAt, other.updatedAt)
        && Objects.equals(currentVersion, other.currentVersion)
        && Objects.equals(currentOwnership, other.currentOwnership)
        && Objects.equals(nominalTime, other.nominalTime)
        && Objects.equals(category, other.category)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        createdAt,
        updatedAt,
        currentVersion,
        currentOwnership,
        nominalTime,
        category,
        description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Job{");
    sb.append("name=").append(name);
    sb.append("createdAt=").append(createdAt);
    sb.append("updatedAt=").append(updatedAt);
    sb.append("currentVersion=").append(currentVersion);
    sb.append("currentOwnership=").append(currentOwnership);
    sb.append("nominalTime=").append(nominalTime);
    sb.append("category=").append(category);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
