package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import javax.validation.constraints.NotNull;

public class JobRunState {

  @NotNull private final Timestamp transitionedAt;
  @NotNull private final Integer jobRunId;
  @NotNull private final String state;

  @JsonCreator
  public JobRunState(
      @JsonProperty("transitioned_at") final Timestamp transitionedAt,
      @JsonProperty("job_run_id") final Integer jobRunId,
      @JsonProperty("state") final String state) {
    this.transitionedAt = transitionedAt;
    this.jobRunId = jobRunId;
    this.state = state;
  }

  public Timestamp getTransitionedAt() {
    return transitionedAt;
  }

  public Integer getJobRunId() {
    return jobRunId;
  }

  public String getState() {
    return state;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof Owner)) return false;

    final JobRunState other = (JobRunState) o;

    return Objects.equals(transitionedAt, other.transitionedAt)
        && Objects.equals(jobRunId, other.jobRunId)
        && Objects.equals(state, other.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transitionedAt, jobRunId, state);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunState{");
    sb.append("transitionedAt=").append(transitionedAt);
    sb.append("jobRunId=").append(jobRunId);
    sb.append("state=").append(state);
    sb.append("}");
    return sb.toString();
  }
}

