package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;

public final class CreateJobRunRequest {
  private final Timestamp createdAt;
  private final Timestamp startedAt;
  private final Timestamp endedAt;
  private final UUID jobRunDefinitionGuid;
  private final JobRunState.State currentState;

  @JsonCreator
  public CreateJobRunRequest(
      @JsonProperty("created_at") final Timestamp createdAt,
      @JsonProperty("started_at") final Timestamp startedAt,
      @JsonProperty("ended_at") final Timestamp endedAt,
      @JsonProperty("job_run_definition_guid") final UUID jobRunDefinitionGuid,
      @JsonProperty("current_state") final String currentState) {
    this.createdAt = createdAt;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobRunDefinitionGuid = jobRunDefinitionGuid;
    this.currentState = JobRunState.State.valueOf(currentState);
  }

  @JsonProperty("created_at")
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("started_at")
  public Timestamp getStartedAt() {
    return startedAt;
  }

  @JsonProperty("ended_at")
  public Timestamp getEndedAt() {
    return endedAt;
  }

  @JsonProperty("job_run_definition_guid")
  public UUID getJobRunDefinitionGuid() {
    return jobRunDefinitionGuid;
  }

  @JsonProperty("current_state")
  public Integer getCurrentState() {
    return JobRunState.State.toInt(currentState);
  }
}
