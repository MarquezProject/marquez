package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;

public final class Ownership {
  @NotNull private final Timestamp startedAt;
  @NotNull private final Optional<Timestamp> endedAt;
  @NotNull private final int jobId;
  @NotNull private final int ownerId;

  @JsonCreator
  public Ownership(
      @JsonProperty("startedAt") final Timestamp startedAt,
      @JsonProperty("endedAt") final Optional<Timestamp> endedAt,
      @JsonProperty("jobId") final int jobId,
      @JsonProperty("ownerId") final int ownerId) {
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobId = jobId;
    this.ownerId = ownerId;
  }

  public Timestamp getStartedAt() {
    return startedAt;
  }

  public Optional<Timestamp> getEndedAt() {
    return endedAt;
  }

  public long getJobId() {
    return jobId;
  }

  public long getOwnerId() {
    return ownerId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Ownership)) return false;

    final Ownership other = (Ownership) o;

    return Objects.equals(startedAt, other.startedAt)
        && Objects.equals(endedAt, other.endedAt)
        && Objects.equals(jobId, other.jobId)
        && Objects.equals(ownerId, other.ownerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startedAt, endedAt, jobId, ownerId);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Ownership{");
    sb.append(",startedAt=").append(startedAt);
    sb.append(",endedAt=").append(endedAt);
    sb.append(",jobId=").append(jobId);
    sb.append(",ownerId=").append(ownerId);
    sb.append("}");
    return sb.toString();
  }
}
