package marquez.service;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobVersionId;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
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
   * @param jobInputUpdate - the job input update
   */
  void notify(JobInputUpdate jobInputUpdate);

  /**
   * Typically called when a run is Complete
   *
   * @param jobOutputUpdate - the job output update
   */
  void notify(JobOutputUpdate jobOutputUpdate);

  /**
   * Called when the job transitions from one state to another
   *
   * @param runTransition - the run transition
   */
  void notify(RunTransition runTransition);

  /** Job input update event lists all the input versions for a given run of a job */
  @Value
  class JobInputUpdate {
    @NonNull RunId runId;
    @NonNull RunMeta runMeta;
    @NonNull JobVersionId jobVersionId;
    @NonNull List<RunInput> inputs;
  }

  /** metadata for a specific input of a job. the version of the dataset consumed */
  @Value
  class RunInput {
    @NonNull DatasetVersionId datasetVersionId;
    // TODO(Julien): add metadata attached to an input (ex: range predicate)
  }

  /** Job output update event */
  @Value
  class JobOutputUpdate {
    @NonNull RunId runId;
    @NonNull JobVersionId jobVersionId;
    @NonNull List<RunOutput> outputs;
  }

  /** metadata for a specific output of a job. the version of the dataset produced */
  @Value
  class RunOutput {
    @NonNull DatasetVersionId datasetVersionId;
    // TODO(Julien): add metadata attached to an output (ex: output partition key(s))
  }

  /** The run state transition event. */
  @Value
  class RunTransition {
    /** the unique ID of the run. */
    @NonNull RunId runId;
    /** The old state of the run. */
    @Nullable RunState oldState;
    /** The new state of the run. */
    @NonNull RunState newState;

    public Optional<RunState> getOldState() {
      return Optional.ofNullable(oldState);
    }
  }
}
