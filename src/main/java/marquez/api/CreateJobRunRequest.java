package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CreateJobRunRequest {

  @JsonProperty("nominalStartTime")
  private Timestamp nominalStartTime;

  @JsonProperty("nominalEndTime")
  private Timestamp nominalEndTime;

  @JsonProperty("runArgs")
  private String runArgs;
}
