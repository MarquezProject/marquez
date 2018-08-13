package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class IcebergTableVersion extends DatasetVersion {
  @NotNull private final long previousSnapshotId;
  @NotNull private final long currentSnapshotId;
  @NotNull private final String metadataLocation;

  @JsonCreator
  public IcebergTableVersion(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("datasetId") final Integer datasetId,
      @JsonProperty("schemaLocation") final URI schemaUri,
      @JsonProperty("previousSnapshotId") final long previousSnapshotId,
      @JsonProperty("currentSnapshotId") final long currentSnapshotId,
      @JsonProperty("metadataLocation") final String metadataLocation) {
    super(createdAt, datasetId, schemaUri);
    this.previousSnapshotId = previousSnapshotId;
    this.currentSnapshotId = currentSnapshotId;
    this.metadataLocation = metadataLocation;
  }

  public Long getPreviousSnapshotId() {
    return previousSnapshotId;
  }

  public Long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public String getMetadataLocation() {
    return metadataLocation;
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
        && Objects.equals(metadataLocation, other.metadataLocation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getCreatedAt(),
        getDatasetId(),
        getSchemaUri(),
        previousSnapshotId,
        currentSnapshotId,
        metadataLocation);
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
    sb.append("metadataLocation=").append(metadataLocation);
    sb.append("}");
    return sb.toString();
  }
}
