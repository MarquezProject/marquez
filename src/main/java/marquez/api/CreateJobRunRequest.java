package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CreateJobRunRequest {

  @JsonProperty("nominalStartTime")
  private String nominalStartTime;

  @JsonProperty("nominalEndTime")
  private String nominalEndTime;

  @JsonProperty("runArgs")
  @NotNull
  private String runArgs;
}
