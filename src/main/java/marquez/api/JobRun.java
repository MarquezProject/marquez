package marquez.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
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

  /*
  TODO: Decide whether this should be a string or an actual RunArgs object
   */
  @JsonProperty("runArgs")
  private String runArgs;

  private Integer currentState;

  @JsonProperty("runState")
  public JobRunState.State getCurrentState() {
    return JobRunState.State.fromInt(currentState);
  }

  private static final Map<JobRunState.State, Set<JobRunState.State>> validTransitions =
      new HashMap<>();

  static {
    validTransitions.put(
        JobRunState.State.NEW,
        new HashSet<JobRunState.State>() {
          {
            add(JobRunState.State.RUNNING);
          }
        });
    validTransitions.put(
        JobRunState.State.RUNNING,
        new HashSet<JobRunState.State>() {
          {
            add(JobRunState.State.COMPLETED);
            add(JobRunState.State.FAILED);
            add(JobRunState.State.ABORTED);
          }
        });
    validTransitions.put(JobRunState.State.FAILED, new HashSet<>());
    validTransitions.put(JobRunState.State.COMPLETED, new HashSet<>());
    validTransitions.put(JobRunState.State.ABORTED, new HashSet<>());
  }

  public static boolean isValidJobTransition(
      JobRunState.State oldState, JobRunState.State newState) {
    return validTransitions.get(oldState).contains(newState);
  }

  public static boolean isValidJobTransition(Integer oldState, Integer newState) {
    return isValidJobTransition(
        JobRunState.State.fromInt(oldState), JobRunState.State.fromInt(newState));
  }
}
