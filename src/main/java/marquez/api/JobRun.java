package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class JobRun {
  @JsonProperty("runId")
  @NotBlank
  private UUID guid;

  /*
  TODO: Clean these up to be consistent when rebuilding the service.
  Right now just setting this for compatibility.
   */
  @JsonProperty("startedAt")
  private Timestamp nominalStartTime;

  @JsonProperty("endedAt")
  private Timestamp nominalEndTime;

  @JsonIgnore private UUID jobRunDefinitionGuid;

  @JsonProperty("runArgs")
  private String runArgs;

  @JsonProperty("runState")
  private String currentState;

  private static final Map<
          marquez.core.models.JobRunState.State, Set<marquez.core.models.JobRunState.State>>
      validTransitions = new HashMap<>();
}
