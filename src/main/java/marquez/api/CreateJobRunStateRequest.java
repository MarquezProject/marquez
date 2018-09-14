package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public final class CreateJobRunStateRequest {
  private final UUID jobRunGuid;
  private final JobRunState.State state;

  @JsonCreator
  public CreateJobRunStateRequest(
      @JsonProperty("job_run_guid") final UUID jobRunGuid,
      @JsonProperty("state") final JobRunState.State state) {
    this.jobRunGuid = jobRunGuid;
    this.state = state;
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
