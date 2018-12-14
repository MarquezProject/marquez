package marquez.core.services;

import java.util.UUID;
import marquez.api.JobRun;
import marquez.api.JobRunState;
import marquez.core.exceptions.UnexpectedException;

public class JobRunService {
  public void insert(JobRun jobRun) throws UnexpectedException {
    // TODO: Access the DB and insert an entry into job run and job run currentState
    return;
  }

  public void updateJobRunState(String runId, JobRunState.State status) throws UnexpectedException {
    // TODO: Perform the requisite action to mark the job as succeeded
    // and also create an entry in the JobRunState table
  }

  public marquez.core.models.JobRun getJobRun(UUID runId) throws UnexpectedException {
    // TODO: Query the DB for this record
    return null;
  }
}
