package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CreateJobRunResponse {
  @JsonProperty("runId")
  @NotBlank
  private UUID externalGuid;
}
