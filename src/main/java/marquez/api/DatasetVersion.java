package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
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
}
