package marquez.job.resource.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public final class JobVersion {
  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final long jobId;
  @NotNull private final String gitRepoUri;
  @NotNull private final String gitSha;
  @NotNull private final long latestRunId;

  @JsonCreator
  public JobVersion(
      @JsonProperty("createdAt") final Timestamp createdAt,
      @JsonProperty("updatedAt") final Timestamp updatedAt,
      @JsonProperty("jobId") final long jobId,
      @JsonProperty("gitRepoUri") final String gitRepoUri,
      @JsonProperty("gitSha") final String gitSha,
      @JsonProperty("latestRunId") final long latestRunId) {
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.jobId = jobId;
    this.gitRepoUri = gitRepoUri;
    this.gitSha = gitSha;
    this.latestRunId = latestRunId;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public long getJobId() {
    return jobId;
  }

  public String getGitRepoUri() {
    return gitRepoUri;
  }

  public String getGitSha() {
    return gitSha;
  }

  public long getLatestRunId() {
    return latestRunId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobVersion)) return false;

    final JobVersion other = (JobVersion) o;

    return Objects.equals(createdAt, other.createdAt)
        && Objects.equals(updatedAt, other.updatedAt)
        && Objects.equals(jobId, other.jobId)
        && Objects.equals(gitRepoUri, other.gitRepoUri)
        && Objects.equals(gitSha, other.gitSha)
        && Objects.equals(latestRunId, other.latestRunId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, updatedAt, jobId, gitRepoUri, gitSha, latestRunId);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobVersion{");
    sb.append("createdAt=").append(createdAt);
    sb.append("updatedAt=").append(updatedAt);
    sb.append("jobId=").append(jobId);
    sb.append("gitRepoUri=").append(gitRepoUri);
    sb.append("gitSha=").append(gitSha);
    sb.append("latestRunId=").append(latestRunId);
    sb.append("}");
    return sb.toString();
  }
}
