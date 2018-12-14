package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;

public class Namespace {

  final String name;
  final Timestamp createdAt;
  final String owner;
  final String description;

  // TODO: Adjust to use Lombok

  @JsonCreator
  public Namespace(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") Timestamp createdAt,
      @JsonProperty("owner") final String owner,
      @JsonProperty("description") final String description) {
    this.name = name;
    this.createdAt = createdAt;
    this.owner = owner;
    this.description = description;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("createdAt")
  public Timestamp getCreatedAt() {
    return this.createdAt;
  }

  @JsonProperty("owner")
  public String getOwner() {
    return owner;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Namespace)) return false;

    final Namespace other = (Namespace) o;

    return Objects.equals(name, other.name)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(owner, other.owner)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, createdAt, owner, description);
  }
}
