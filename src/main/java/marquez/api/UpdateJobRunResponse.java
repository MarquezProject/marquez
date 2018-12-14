package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UpdateJobRunResponse {
  @NotBlank
  @JsonProperty("job_run_id")
  private UUID externalGuid;
}
