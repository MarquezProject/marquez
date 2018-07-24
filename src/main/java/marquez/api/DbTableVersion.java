package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class DbTableVersion extends DatasetVersion {
  @NotNull private final String name;
  @NotNull private final List<String> columns;
  @NotNull private final Integer databaseId;

  @JsonCreator
  public DbTableVersion(
      @JsonProperty("name") final String name,
      @JsonProperty("columns") final List<String> columns,
      @JsonProperty("database_id") final Integer databaseId) {
    this.name = name;
    this.columns = columns;
    this.databaseId = databaseId;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Integer getDatasetId() {
    return datasetId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DatasetVersion)) return false;

    final DatasetVersion other = (DatasetVersion) o;

    return Objects.equals(createdAt, other.createdAt) && Objects.equals(datasetId, other.datasetId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, datasetId);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DbTableVersion{");
    sb.append("createdAt=").append(createdAt);
    sb.append("datasetId=").append(datasetId);
    sb.append("}");
    return sb.toString();
  }
}

