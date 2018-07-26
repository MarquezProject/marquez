package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class IcebergTableVersion extends DatasetVersion {
  @NotNull private final Long previousSnapshotId;
  @NotNull private final Long currentSnapshotId;
  @NotNull private final String metadataUri;

  @JsonCreator
  public IcebergTableVersion(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("dataset_id") final Integer datasetId,
      @JsonProperty("schema_uri") final String schemaUri,
      @JsonProperty("previous_snapshot_id") final Long previousSnapshotId,
      @JsonProperty("current_snapshot_id") final Long currentSnapshotId,
      @JsonProperty("metadata_uri") final String metadataUri) {
    super(createdAt, datasetId, schemaUri);
    this.previousSnapshotId = previousSnapshotId;
    this.currentSnapshotId = currentSnapshotId;
    this.metadataUri = metadataUri;
  }

  public Long getPreviousSnapshotId() {
    return previousSnapshotId;
  }

  public Long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public String getMetadataUri() {
    return metadataUri;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof IcebergTableVersion)) return false;

    final IcebergTableVersion other = (IcebergTableVersion) o;

    return Objects.equals(getCreatedAt(), other.getCreatedAt())
        && Objects.equals(getDatasetId(), other.getDatasetId())
        && Objects.equals(getSchemaUri(), other.getSchemaUri())
        && Objects.equals(previousSnapshotId, other.previousSnapshotId)
        && Objects.equals(currentSnapshotId, other.currentSnapshotId)
        && Objects.equals(metadataUri, other.metadataUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getCreatedAt(),
        getDatasetId(),
        getSchemaUri(),
        previousSnapshotId,
        currentSnapshotId,
        metadataUri);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DbTableVersion{");
    sb.append("createdAt=").append(getCreatedAt());
    sb.append("datasetId=").append(getDatasetId());
    sb.append("schemaUri=").append(getSchemaUri());
    sb.append("previousSnapshotId=").append(previousSnapshotId);
    sb.append("currentSnapshotId=").append(currentSnapshotId);
    sb.append("metadataUri=").append(metadataUri);
    sb.append("}");
    return sb.toString();
  }
}
