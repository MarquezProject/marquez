package marquez.api;

import static marquez.api.JobRunState.State.fromInt;
import static marquez.api.JobRunState.State.toInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import org.junit.Test;

public class JobRunDaoTest extends JobRunBaseTest {

  @Test
  public void testJobRunCreationCreatesJobRunState() {
    JobRunState returnedJobRunState =
        jobRunStateDAO.findJobLatestJobRunStateByJobRun(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.NEW, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunUpdateCreatesJobRunState() {
    JobRun modifiedJobRun =
        new JobRun(
            NEW_JOB_RUN.getGuid(),
            new Timestamp(System.currentTimeMillis()),
            NEW_JOB_RUN.getEndedAt(),
            NEW_JOB_RUN.getJobRunDefinitionGuid(),
            toInt(JobRunState.State.RUNNING));
    jobRunDAO.update(modifiedJobRun);

    JobRunState returnedJobRunState =
        jobRunStateDAO.findJobLatestJobRunStateByJobRun(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.RUNNING, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunGetter() {
    JobRun returnedJobRun = jobRunDAO.findJobRunById(NEW_JOB_RUN.getGuid());
    assertNull(returnedJobRun.getStartedAt());
    assertNull(returnedJobRun.getEndedAt());
    assertEquals(JobRunState.State.NEW, fromInt(returnedJobRun.getCurrentState()));
  }
}
