package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class JobVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final UUID jobGuid;
  @NotNull private final String gitRepoUri;
  @NotNull private final String gitSha;
  @NotNull private final UUID latestRunGuid;

  @JsonCreator
  public JobVersion(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("updatedAt") final Timestamp updatedAt,
      @JsonProperty("jobGuid") final UUID jobGuid,
      @JsonProperty("gitRepoUri") final String gitRepoUri,
      @JsonProperty("gitSha") final String gitSha,
      @JsonProperty("latestRunGuid") final UUID latestRunGuid) {
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.jobGuid = jobGuid;
    this.gitRepoUri = gitRepoUri;
    this.gitSha = gitSha;
    this.latestRunGuid = latestRunGuid;
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

  public String getGitRepoUri() {
    return gitRepoUri;
  }

  public String getGitSha() {
    return gitSha;
  }

  public UUID getLatestRunGuid() {
    return latestRunGuid;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobVersion)) return false;

    final JobVersion other = (JobVersion) o;

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(updatedAt, other.updatedAt)
        && Objects.equals(jobGuid, other.jobGuid)
        && Objects.equals(gitRepoUri, other.gitRepoUri)
        && Objects.equals(gitSha, other.gitSha)
        && Objects.equals(latestRunGuid, other.latestRunGuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, updatedAt, jobGuid, gitRepoUri, gitSha, latestRunGuid);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobVersion{");
    sb.append("createdAt=").append(createdAt);
    sb.append("updatedAt=").append(updatedAt);
    sb.append("jobGuid=").append(jobGuid);
    sb.append("gitRepoUri=").append(gitRepoUri);
    sb.append("gitSha=").append(gitSha);
    sb.append("latestRunGuid=").append(latestRunGuid);
    sb.append("}");
    return sb.toString();
  }
}
