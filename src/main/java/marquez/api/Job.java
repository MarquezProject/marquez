package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class Job {
  @NotNull private final String name;
  @NotNull private final String owner;
  @NotNull private final Timestamp nominalTime;
  @NotNull private final String category;
  @NotNull private final String description;

  @JsonCreator
  public Job(
      @JsonProperty("name") final String name,
      @JsonProperty("owner") final String owner,
      @JsonProperty("nominalTime") final Timestamp nominalTime,
      @JsonProperty("category") final String category,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.owner = owner;
    this.nominalTime = nominalTime;
    this.category = category;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return owner;
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
        && Objects.equals(owner, other.owner)
        && Objects.equals(nominalTime, other.nominalTime)
        && Objects.equals(category, other.category)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        owner,
        nominalTime,
        category,
        description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Job{");
    sb.append("name=").append(name);
    sb.append("owner=").append(owner);
    sb.append("nominalTime=").append(nominalTime);
    sb.append("category=").append(category);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
