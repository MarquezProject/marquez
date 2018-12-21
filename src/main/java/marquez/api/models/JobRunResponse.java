package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class JobRunResponse {
  @JsonProperty("runId")
  @NotBlank
  private UUID runId;

  @JsonProperty("nominalStartTime")
  private String nominalStartTime;

  @JsonProperty("nominalEndTime")
  private String nominalEndTime;

  @JsonProperty("runArgs")
  private String runArgs;

  @JsonProperty("runState")
  private String runState;
}
