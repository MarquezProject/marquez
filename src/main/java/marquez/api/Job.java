package marquez.api;

import org.hibernate.validator.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;

public final class Job {
  private final int id;
  private final String name;
  private final String ownerName;
  private final Timestamp nominalTime;
  private final String category;
  private final String description;

  @JsonCreator
  public Job(
      @JsonProperty("id") final int id,
      @JsonProperty("name") @NotBlank final String name,
      @JsonProperty("ownerName") final String ownerName,
      @JsonProperty("nominalTime") final Timestamp nominalTime,
      @JsonProperty("category") final String category,
      @JsonProperty("description") final String description) {
    this.id = id;
    this.name = name;
    this.ownerName = ownerName;
    this.nominalTime = nominalTime;
    this.category = category;
    this.description = description;
  }

  @JsonIgnore
  public int getId() {
    return id;
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

    return Objects.equals(id, other.id)
        && Objects.equals(name, other.name)
        && Objects.equals(ownerName, other.ownerName)
        && Objects.equals(nominalTime, other.nominalTime)
        && Objects.equals(category, other.category)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, ownerName, nominalTime, category, description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Job{");
    sb.append("id=").append(id);
    sb.append(",name=").append(name);
    sb.append(",owner=").append(ownerName);
    sb.append(",nominalTime=").append(nominalTime);
    sb.append(",category=").append(category);
    sb.append(",description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
