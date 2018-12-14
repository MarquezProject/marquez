package marquez.dao;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import marquez.JobRunBaseTest;
import marquez.api.JobRunState;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobRunDaoTest extends JobRunBaseTest {

  @BeforeClass
  public static void setUpRowMapper() {
    APP.getJDBI()
        .registerRowMapper(
            JobRunState.class,
            (rs, ctx) ->
                new JobRunState(
                    UUID.fromString(rs.getString("guid")),
                    rs.getTimestamp("transitioned_at"),
                    UUID.fromString(rs.getString("job_run_guid")),
                    JobRunState.State.fromInt(rs.getInt("state"))));
  }

  @Test
  public void testJobRunCreationCreatesJobRunState() {
    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.NEW, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunUpdateCreatesJobRunState() {
    jobRunDAO.updateState(
        NEW_JOB_RUN.getGuid(), JobRunState.State.toInt(JobRunState.State.RUNNING));

    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.RUNNING, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunGetter() {
    marquez.core.models.JobRun returnedJobRun = jobRunDAO.findJobRunById(NEW_JOB_RUN.getGuid());
    assertNull(returnedJobRun.getNominalStartTime());
    assertNull(returnedJobRun.getNominalEndTime());
    assertEquals(
        JobRunState.State.NEW, JobRunState.State.fromInt(returnedJobRun.getCurrentState()));
  }

  private JobRunState getLatestJobRunStateForJobId(UUID jobRunId) {
    Handle handle = APP.getJDBI().open();
    Query qr =
        handle.select(
            format(
                "SELECT * FROM job_run_states WHERE job_run_guid = '%s' ORDER by transitioned_at DESC",
                jobRunId.toString()));
    return qr.mapTo(JobRunState.class).stream().findFirst().get();
  }
}
