package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Objects;

public final class Db {
  public enum Type {
    MYSQL,
    POSTGRESQL
  }

  @NotNull private final Timestamp createdAt;
  @NotNull private final Type type;
  @NotNull private final String name;
  @NotNull private final URL connectionUrl;
  @NotNull private final String description;

  @JsonCreator
  public Db(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("type") final Type type,
      @JsonProperty("name") final String name,
      @JsonProperty("connectionUrl") final URL connectionUrl,
      @JsonProperty("description") final String description) {
    this.createdAt = createdAt;
    this.type = type;
    this.name = name;
    this.connectionUrl = connectionUrl;
    this.description = description;
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

  public URL getConnectionUrl() {
    return connectionUrl;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Db)) return false;

    final Db other = (Db) o;

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(type, other.type)
        && Objects.equals(name, other.name)
        && Objects.equals(connectionUrl, other.connectionUrl)
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, type, name, description);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Db{");
    sb.append("createdAt=").append(createdAt);
    sb.append("type=").append(type);
    sb.append("name=").append(name);
    sb.append("connectionUrl=").append(connectionUrl);
    sb.append("description=").append(description);
    sb.append("}");
    return sb.toString();
  }
}
