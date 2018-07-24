package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public abstract class DatasetVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final Integer datasetId;

  @JsonCreator
  public DatasetVersion(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("dataset_id") final Integer datasetId) {
    this.createdAt = createdAt;
    this.datasetId = datasetId;
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
    sb.append("DatasetVersion{");
    sb.append("createdAt=").append(createdAt);
    sb.append("datasetId=").append(datasetId);
    sb.append("}");
    return sb.toString();
  }
}
