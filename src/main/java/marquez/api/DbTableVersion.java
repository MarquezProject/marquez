package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DbTableVersion extends DatasetVersion {
  @NotNull private final String name;
  @NotNull private final List<String> columns;
  @NotNull private final UUID databaseGuid;

  @JsonCreator
  public DbTableVersion(
      @JsonProperty("name") final String name,
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("datasetGuid") final UUID datasetGuid,
      @JsonProperty("schemaUri") final URI schemaUri,
      @JsonProperty("columns") final List<String> columns,
      @JsonProperty("databaseGuid") final UUID databaseGuid) {
    super(createdAt, datasetGuid, schemaUri);
    this.name = name;
    this.columns = columns;
    this.databaseGuid = databaseGuid;
  }

  public String getName() {
    return name;
  }

  public List<String> getColumns() {
    return columns;
  }

  public UUID getDatabaseGuid() {
    return databaseGuid;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DbTableVersion)) return false;

    final DbTableVersion other = (DbTableVersion) o;

    return Objects.equals(getCreatedAt(), other.getCreatedAt())
        && Objects.equals(getDatasetGuid(), other.getDatasetGuid())
        && Objects.equals(getSchemaUri(), other.getSchemaUri())
        && Objects.equals(name, other.name)
        && Objects.equals(columns, other.columns)
        && Objects.equals(databaseGuid, other.databaseGuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getCreatedAt(), getDatasetGuid(), getSchemaUri(), name, columns, databaseGuid);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DbTableVersion{");
    sb.append("createdAt=").append(getCreatedAt());
    sb.append("datasetGuid=").append(getDatasetGuid());
    sb.append("schemaUri=").append(getSchemaUri());
    sb.append("name=").append(name);
    sb.append("columns=").append(columns);
    sb.append("databaseGuid=").append(databaseGuid);
    sb.append("}");
    return sb.toString();
  }
}
