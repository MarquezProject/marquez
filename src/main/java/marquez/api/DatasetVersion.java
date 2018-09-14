package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableVersion.class, name = "DB_TABLE_VERSION"),
  @JsonSubTypes.Type(value = IcebergTableVersion.class, name = "ICEBERG_TABLE_VERSION")
})
public abstract class DatasetVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final UUID datasetGuid;
  @NotNull private final URI schemaUri;

  @JsonCreator
  public DatasetVersion(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("datasetGuid") final UUID datasetGuid,
      @JsonProperty("schemaUri") final URI schemaUri) {
    this.createdAt = createdAt;
    this.datasetGuid = datasetGuid;
    this.schemaUri = schemaUri;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public UUID getDatasetGuid() {
    return datasetGuid;
  }

  public URI getSchemaUri() {
    return schemaUri;
  }
}
