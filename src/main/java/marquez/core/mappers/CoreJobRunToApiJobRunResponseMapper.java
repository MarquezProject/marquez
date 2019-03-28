package marquez.core.mappers;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import marquez.api.models.JobRunResponse;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;

public class CoreJobRunToApiJobRunResponseMapper extends Mapper<JobRun, JobRunResponse> {
  @Override
  public JobRunResponse map(JobRun value) {
    return new JobRunResponse(
        value.getGuid(),
        value.getNominalStartTime() == null ? null : value.getNominalStartTime().toString(),
        value.getNominalEndTime() == null ? null : value.getNominalEndTime().toString(),
        value.getRunArgs(),
        JobRunState.State.fromInt(value.getCurrentState()).name());
  }

  public List<JobRunResponse> map(List<JobRun> jobRuns) {
    return Collections.unmodifiableList(
        jobRuns.stream().map(jobRun -> map(jobRun)).collect(toList()));
  }
}
