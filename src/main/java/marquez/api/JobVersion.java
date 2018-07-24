package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class JobVersion {

  @NotNull private final Timestamp createdAt;
  @NotNull private final Timestamp updatedAt;
  @NotNull private final Integer jobId;
  @NotNull private final String gitRepoUri;
  @NotNull private final String gitSha;
  @NotNull private final Integer latestRun;

  @JsonCreator
  public JobVersion(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("updated_at") final Timestamp updatedAt,
      @JsonProperty("job_id") final Integer jobId,
      @JsonProperty("git_repo_uri") final String gitRepoUri,
      @JsonProperty("git_sha") final String gitSha,
      @JsonProperty("latest_run") final Integer latestRun) {
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.jobId = jobId;
    this.gitRepoUri = gitRepoUri;
    this.gitSha = gitSha;
    this.latestRun = latestRun;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Integer getJobId() {
    return jobId;
  }

  public String getGitRepoUri() {
    return gitRepoUri;
  }

  public String getGitSha() {
    return gitSha;
  }

  public Integer getLatestRun() {
    return latestRun;
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
        && Objects.equals(latestRun, other.latestRun);
  }

  @Override
  public int hashCode() {
    return Objects.hash(createdAt, updatedAt, jobId, gitRepoUri, gitSha, latestRun);
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
    sb.append("latestRun=").append(latestRun);
    sb.append("}");
    return sb.toString();
  }
}

