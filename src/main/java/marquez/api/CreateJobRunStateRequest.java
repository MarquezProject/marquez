package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;

public final class CreateJobRunStateRequest {
  private final Timestamp transitionedAt;
  private final UUID jobRunGuid;
  private final JobRunState.State state;

  @JsonCreator
  public CreateJobRunStateRequest(
      @JsonProperty("transitioned_at") final Timestamp transitionedAt,
      @JsonProperty("job_run_guid") final UUID jobRunGuid,
      @JsonProperty("state") final JobRunState.State state) {
    this.transitionedAt = transitionedAt;
    this.jobRunGuid = jobRunGuid;
    this.state = state;
  }

  @JsonProperty("transitioned_at")
  public Timestamp getTransitionedAt() {
    return transitionedAt;
  }

  @JsonProperty("job_run_guid")
  public UUID getJobRunGuid() {
    return jobRunGuid;
  }

  @JsonProperty("state")
  public JobRunState.State getState() {
    return state;
  }
}
