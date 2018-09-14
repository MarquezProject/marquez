package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public final class CreateJobRunRequest {
  private final UUID runDefinitionId;

  @JsonCreator
  public CreateJobRunRequest(@JsonProperty("run_definition_id") final UUID runDefinitionId) {
    this.runDefinitionId = runDefinitionId;
  }

  @JsonProperty("run_definition_id")
  public UUID getRunDefinitionId() {
    return runDefinitionId;
  }
}
