package marquez.core.mappers;

import marquez.api.JobRun;
import marquez.core.models.JobRunState;

public class CoreJobRunToApiJobRunMapper extends Mapper<marquez.core.models.JobRun, JobRun> {
  @Override
  public JobRun map(marquez.core.models.JobRun value) {
    return new JobRun(
        value.getGuid(),
        value.getNominalStartTime().toString(),
        value.getNominalEndTime().toString(),
        value.getRunArgs(),
        JobRunState.State.fromInt(value.getCurrentState()).name());
  }
}
