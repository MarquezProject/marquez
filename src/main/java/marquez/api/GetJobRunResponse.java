package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import java.sql.Timestamp;
import java.util.UUID;

public class GetJobRunResponse {
  private final UUID guid;
  private final UUID jobRunDefinitionGuid;
  private final Timestamp endedAt;
  private final Timestamp startedAt;
  private final Timestamp createdAt;
  private final JobRunState.State current_state;

  public GetJobRunResponse(
      @JsonProperty("guid") @NotBlank final UUID guid,
      @JsonProperty("created_at") Timestamp createdAt,
      @JsonProperty("started_at") Timestamp startedAt,
      @JsonProperty("ended_at") Timestamp endedAt,
      @JsonProperty("job_run_definition_guid") UUID jobRunDefinitionGuid,
      @JsonProperty("state") JobRunState.State current_state) {
    this.guid = guid;
    this.createdAt = createdAt;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.jobRunDefinitionGuid = jobRunDefinitionGuid;
    this.current_state = current_state;
  }

  @JsonIgnore
  public UUID getGuid() {
    return this.guid;
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

  @JsonProperty("state")
  public String getCurrentState() {
    return current_state.toString();
  }
}
