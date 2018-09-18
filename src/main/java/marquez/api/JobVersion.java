package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import marquez.api.entities.*;

public final class JobVersion {

  @NotNull private final UUID guid;
  @NotNull private final UUID jobGuid;
  @NotNull private final String uri;
  @NotNull private final UUID version;
  @NotNull private final UUID latestJobRunGuid;
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;

  public JobVersion(
      final UUID guid,
      final UUID jobGuid,
      final String uri,
      final UUID version,
      final UUID latestJobRunGuid,
      final Timestamp createdAt,
      final Timestamp updatedAt) {
    this.guid = guid;
    this.jobGuid = jobGuid;
    this.uri = uri;
    this.latestJobRunGuid = latestJobRunGuid;
    this.version = version;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @JsonIgnore
  public UUID getGuid() {
    return guid;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public UUID getJobGuid() {
    return jobGuid;
  }

  public String getURI() {
    return uri;
  }

  public UUID getLatestRunGuid() {
    return latestJobRunGuid;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobVersion)) return false;

    final JobVersion other = (JobVersion) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(jobGuid, other.jobGuid)
        && Objects.equals(uri, other.uri)
        && Objects.equals(version, other.version)
        && Objects.equals(createdAt, other.createdAt)
        && Objects.equals(latestJobRunGuid, other.latestJobRunGuid)
        && Objects.equals(updatedAt, other.updatedAt);
  }

  public static JobVersion create(CreateJobRunDefinitionRequest request) {
    return new JobVersion(null, null, request.getURI(), null, null, null, null);
  }

  public UUID computeVersionGuid() {
    byte[] raw = String.format("%s:%s", jobGuid, uri).getBytes();
    return UUID.nameUUIDFromBytes(raw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, updatedAt, guid, jobGuid, uri, version);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobVersion{");
    sb.append("createdAt=").append(createdAt);
    sb.append("updatedAt=").append(updatedAt);
    sb.append("guid=").append(guid);
    sb.append("jobGuid=").append(jobGuid);
    sb.append("uri=").append(uri);
    sb.append("latestJobRunGuid=").append(latestJobRunGuid);
    sb.append("}");
    return sb.toString();
  }
}
