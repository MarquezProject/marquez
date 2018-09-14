package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Ownership {
  private final UUID guid;
  private final Timestamp startedAt;
  private final Optional<Timestamp> endedAt;
  private final String jobName;
  private final String ownerName;

  @JsonCreator
  public Ownership(
      @JsonProperty("guid") final UUID guid,
      @JsonProperty("startedAt") final Timestamp startedAt,
      @JsonProperty("endedAt") final Optional<Timestamp> endedAt,
      @JsonProperty("jobName") final String jobName,
      @JsonProperty("ownerName") final String ownerName) {
    this.guid = guid;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobName = jobName;
    this.ownerName = ownerName;
  }

  @JsonIgnore
  public UUID getGuid() {
    return guid;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public Optional<Timestamp> getEndedAt() {
    return endedAt;
  }

  public String getJobName() {
    return jobName;
  }

  public String getOwnerName() {
    return ownerName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Ownership)) return false;

    final Ownership other = (Ownership) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(jobName, other.jobName)
        && Objects.equals(ownerName, other.ownerName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startedAt, endedAt, jobName, ownerName);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Ownership{");
    sb.append("guid=").append(guid);
    sb.append(",startedAt=").append(startedAt);
    sb.append(",endedAt=").append(endedAt);
    sb.append(",jobName=").append(jobName);
    sb.append(",ownerName=").append(ownerName);
    sb.append("}");
    return sb.toString();
  }
}
