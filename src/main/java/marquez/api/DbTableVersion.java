package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class DbTableVersion extends DatasetVersion {
  @NotNull private final String name;
  @NotNull private final List<String> columns;
  @NotNull private final Integer databaseId;

  @JsonCreator
  public DbTableVersion(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("dataset_id") final Integer datasetId,
      @JsonProperty("schema_uri") final String schemaUri,
      @JsonProperty("name") final String name,
      @JsonProperty("columns") final List<String> columns,
      @JsonProperty("database_id") final Integer databaseId) {
    super(createdAt, datasetId, schemaUri);
    this.name = name;
    this.columns = columns;
    this.databaseId = databaseId;
  }

  public String getName() {
    return name;
  }

  public List<String> getColumns() {
    return columns;
  }

  public Integer getDatabaseId() {
    return databaseId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DbTableVersion)) return false;

    final DbTableVersion other = (DbTableVersion) o;

    return Objects.equals(getCreatedAt(), other.getCreatedAt())
        && Objects.equals(getDatasetId(), other.getDatasetId())
        && Objects.equals(getSchemaUri(), other.getSchemaUri())
        && Objects.equals(name, other.name)
        && Objects.equals(columns, other.columns)
        && Objects.equals(databaseId, other.databaseId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCreatedAt(), getDatasetId(), getSchemaUri(), name, columns, databaseId);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DbTableVersion{");
    sb.append("createdAt=").append(getCreatedAt());
    sb.append("datasetId=").append(getDatasetId());
    sb.append("schemaUri=").append(getSchemaUri());
    sb.append("name=").append(name);
    sb.append("columns=").append(columns);
    sb.append("databaseId=").append(databaseId);
    sb.append("}");
    return sb.toString();
  }
}
