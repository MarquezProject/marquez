package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;
import org.hibernate.validator.constraints.NotBlank;

public class GetJobRunResponse {
  private final UUID guid;
  private final UUID runDefinitionId;
  private final Timestamp endedAt;
  private final Timestamp startedAt;
  private final JobRunState.State state;

  public GetJobRunResponse(
      @JsonProperty("guid") @NotBlank final UUID guid,
      @JsonProperty("started_at") Timestamp startedAt,
      @JsonProperty("ended_at") Timestamp endedAt,
      @JsonProperty("run_definition_id") UUID runDefinitionId,
      @JsonProperty("state") JobRunState.State state) {
    this.guid = guid;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.runDefinitionId = runDefinitionId;
    this.state = state;
  }

  @JsonIgnore
  public UUID getGuid() {
    return this.guid;
  }

  @JsonProperty("started_at")
  public Timestamp getStartedAt() {
    return startedAt;
  }

  @JsonProperty("ended_at")
  public Timestamp getEndedAt() {
    return endedAt;
  }

  @JsonProperty("run_definition_id")
  public UUID getRunDefinitionId() {
    return runDefinitionId;
  }

  @JsonProperty("state")
  public String getState() {
    return state.toString();
  }
}
