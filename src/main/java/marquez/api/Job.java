package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class Job {

  @NotNull private final String name;
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final Integer currentVersion;
  @NotNull private final Integer currentOwnershipId;
  @NotNull private final Timestamp nominalTime;
  @NotNull private final String category;
  @NotNull private final String description;

  @JsonCreator
  public Job(
      @JsonProperty("name") final String name,
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("updated_at") final Timestamp updatedAt,
      @JsonProperty("current_version") final Integer currentVersion,
      @JsonProperty("current_ownership_id") final Integer currentOwnershipId,
      @JsonProperty("nominal_time") final Timestamp nominalTime,
      @JsonProperty("category") final String category,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.currentVersion = currentVersion;
    this.currentOwnershipId = currentOwnershipId;
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

  public Integer getCurrentVersion() {
    return currentVersion;
  }

  public Integer getCurrentOwnershipId() {
    return currentOwnershipId;
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
        && Objects.equals(currentOwnershipId, other.currentOwnershipId)
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
        currentOwnershipId,
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
    sb.append("currentOwnershipId=").append(currentOwnershipId);
    sb.append("nominalTime=").append(nominalTime);
    sb.append("category=").append(category);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
