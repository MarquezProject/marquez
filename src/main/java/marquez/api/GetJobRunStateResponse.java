package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;

public class GetJobRunStateResponse {
  @JsonProperty("guid")
  private final UUID guid;

  @JsonProperty("transitioned_at")
  private final Timestamp transitionedAt;

  @JsonProperty("job_run_guid")
  private final UUID jobRunGuid;

  @JsonProperty("state")
  private final JobRunState.State state;

  public GetJobRunStateResponse(
      UUID guid, Timestamp transitionedAt, UUID jobRunGuid, JobRunState.State state) {
    this.guid = guid;
    this.transitionedAt = transitionedAt;
    this.jobRunGuid = jobRunGuid;
    this.state = state;
  }

  @JsonIgnore
  public UUID getGuid() {
    return this.guid;
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
  public String getState() {
    return state.toString();
  }
}
