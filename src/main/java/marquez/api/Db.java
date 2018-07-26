package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class Db {
  public enum Type {
    MYSQL,
    POSTGRESQL
  }

  @NotNull private final Timestamp createdAt;
  @NotNull private final Type type;
  @NotNull private final String name;
  @NotNull private final String uri;

  @JsonCreator
  public Db(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("type") final Type type,
      @JsonProperty("name") final String name,
      @JsonProperty("uri") final String uri) {
    this.createdAt = createdAt;
    this.type = type;
    this.name = name;
    this.uri = uri;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Type getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Db)) return false;

    final Db other = (Db) o;

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(type, other.type)
        && Objects.equals(name, other.name)
        && Objects.equals(uri, other.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, type, name, uri);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Db{");
    sb.append("createdAt=").append(createdAt);
    sb.append("type=").append(type);
    sb.append("name=").append(name);
    sb.append("uri=").append(uri);
    sb.append("}");
    return sb.toString();
  }
}
