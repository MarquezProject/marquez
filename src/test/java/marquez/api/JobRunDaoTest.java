package marquez.api;

import static java.lang.String.format;
import static marquez.api.JobRunState.State.fromInt;
import static marquez.api.JobRunState.State.toInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.util.UUID;
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

  //@Test
  public void testJobRunCreationCreatesJobRunState() {
    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.NEW, returnedJobRunState.getState());
  }

  //@Test
  public void testJobRunUpdateCreatesJobRunState() {
    JobRun modifiedJobRun =
        new JobRun(
            NEW_JOB_RUN.getGuid(),
            new Timestamp(System.currentTimeMillis()),
            NEW_JOB_RUN.getEndedAt(),
            NEW_JOB_RUN.getJobRunDefinitionGuid(),
            toInt(JobRunState.State.RUNNING),
            null);
    jobRunDAO.update(modifiedJobRun);

    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(NEW_JOB_RUN.getGuid());
    assertEquals(JobRunState.State.RUNNING, returnedJobRunState.getState());
  }

  //@Test
  public void testJobRunGetter() {
    JobRun returnedJobRun = jobRunDAO.findJobRunById(NEW_JOB_RUN.getGuid());
    assertNull(returnedJobRun.getStartedAt());
    assertNull(returnedJobRun.getEndedAt());
    assertEquals(JobRunState.State.NEW, fromInt(returnedJobRun.getCurrentState()));
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
