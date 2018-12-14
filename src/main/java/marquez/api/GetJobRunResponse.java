package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetJobRunResponse {
  @JsonProperty("runId")
  @NotBlank
  private UUID guid;

  @JsonProperty("startedAt")
  private Timestamp startedAt;

  @JsonProperty("endedAt")
  private Timestamp endedAt;

  @JsonProperty("runState")
  private String state;
}
