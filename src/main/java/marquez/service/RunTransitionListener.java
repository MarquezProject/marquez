package marquez.service;

import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.RunState;
import marquez.service.models.DatasetVersionId;
import marquez.service.models.JobVersionId;
import marquez.service.models.RunMeta;

/**
 * To get notified of run transition events Every run should have at least one JobInputUpdate and
 * one JobOutputUpdate event. A normal lifecycle is have a runTransition(START) followed by
 * runTransition(COMPLETE|FAILED|ABORTED)
 */
public interface RunTransitionListener {

  /**
   * Typically called when a run is created or starts Can also be called as the job is running as
   * new inputs are discovered
   *
   * @param jobInputUpdate
   */
  void notify(JobInputUpdate jobInputUpdate);

  /**
   * Typically called when a run is Complete
   *
   * @param jobOutputUpdate
   */
  void notify(JobOutputUpdate jobOutputUpdate);

  /**
   * Called when the job transitions from one state to another
   *
   * @param transition
   */
  void notify(RunTransition transition);

  /** Job input update event lists all the input versions for a given run of a job */
  @Value
  public class JobInputUpdate {
    @NonNull UUID runId;
    @NonNull RunMeta runMeta;
    @NonNull JobVersionId jobVersion;
    @NonNull List<RunInput> inputs;
  }

  /** metadata for a specific input of a job. the version of the dataset consumed */
  @Value
  public class RunInput {
    @NonNull DatasetVersionId datasetVersion;
    // TODO(Julien): add metadata attached to an input (ex: range predicate)
  }

  /** Job output update event */
  @Value
  public class JobOutputUpdate {
    @NonNull UUID runId;
    @NonNull List<RunOutput> outputs;
  }

  /** metadata for a specific output of a job. the version of the dataset produced */
  @Value
  public class RunOutput {
    @NonNull DatasetVersionId datasetVersion;
    // TODO(Julien): add metadata attached to an output (ex: output partition key(s))
  }

  /** run state transition event */
  @Value
  public static class RunTransition {
    /** the unique ID of the run */
    @NonNull UUID runId;
    /** the new state */
    @NonNull RunState newState;
  }
}
