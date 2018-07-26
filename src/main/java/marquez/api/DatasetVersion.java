package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.sql.Timestamp;
import javax.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableVersion.class, name = "DbTableVersion"),
  @JsonSubTypes.Type(value = IcebergTableVersion.class, name = "IcebergTableVersion")
})
public abstract class DatasetVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final long datasetId;
  @NotNull private final String schemaUri;

  @JsonCreator
  public DatasetVersion(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("dataset_id") final long datasetId,
      @JsonProperty("schema_uri") final String schemaUri) {
    this.createdAt = createdAt;
    this.datasetId = datasetId;
    this.schemaUri = schemaUri;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public long getDatasetId() {
    return datasetId;
  }

  public String getSchemaUri() {
    return schemaUri;
  }
}
