package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.hibernate.validator.constraints.NotBlank;

public final class CreateJobRunDefinitionResponse {
  private final UUID guid;

  @JsonCreator
  public CreateJobRunDefinitionResponse(
      @JsonProperty("run_definition_id") @NotBlank final UUID guid) {
    this.guid = guid;
  }

  @JsonProperty("run_definition_id")
  public UUID getGuid() {
    return guid;
  }
}
