package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public final class CreateJobRunRequest {
  private final UUID jobRunDefinitionGuid;
  private final JobRunState.State currentState;

  @JsonCreator
  public CreateJobRunRequest(
      @JsonProperty("job_run_definition_guid") final UUID jobRunDefinitionGuid) {
    this.jobRunDefinitionGuid = jobRunDefinitionGuid;
    this.currentState = JobRunState.State.NEW;
  }

  @JsonProperty("job_run_definition_guid")
  public UUID getJobRunDefinitionGuid() {
    return jobRunDefinitionGuid;
  }

  @JsonProperty("state")
  public Integer getState() {
    return JobRunState.State.toInt(currentState);
  }
}
