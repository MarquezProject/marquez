package marquez.dataset.resource.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.net.URI;
import java.sql.Timestamp;
import javax.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableVersion.class, name = "DB_TABLE_VERSION"),
  @JsonSubTypes.Type(value = IcebergTableVersion.class, name = "ICEBERG_TABLE_VERSION")
})
public abstract class DatasetVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final long datasetId;
  @NotNull private final URI schemaUri;

  @JsonCreator
  public DatasetVersion(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("datasetId") final long datasetId,
      @JsonProperty("schemaUri") final URI schemaUri) {
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

  public URI getSchemaUri() {
    return schemaUri;
  }
}
