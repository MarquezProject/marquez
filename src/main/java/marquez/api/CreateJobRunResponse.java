package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import org.hibernate.validator.constraints.NotBlank;

public final class CreateJobRunResponse {
  private final UUID externalGuid;

  @JsonCreator
  public CreateJobRunResponse(@JsonProperty("run_id") @NotBlank final UUID externalGuid) {
    this.externalGuid = externalGuid;
  }

  @JsonProperty("run_id")
  public UUID getExternalGuid() {
    return externalGuid;
  }
}
