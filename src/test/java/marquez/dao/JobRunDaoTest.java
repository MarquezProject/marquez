package marquez.dao;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import marquez.api.JobRunBaseTest;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Generator;
import marquez.core.models.JobRunState;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunDaoTest extends JobRunBaseTest {

  private static Logger LOG = LoggerFactory.getLogger(JobRunDaoTest.class);

  protected static String NAMESPACE_NAME;
  protected static String CREATED_JOB_NAME;
  protected static UUID CREATED_JOB_RUN_UUID;

  protected static final String JOB_RUN_ARGS = "{'key': 'value'}";

  protected static final NamespaceDAO namespaceDAO = APP.onDemand(NamespaceDAO.class);
  protected static final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  protected static final JobVersionDAO jobVersionDAO = APP.onDemand(JobVersionDAO.class);
  protected static final JobRunDAO jobRunDAO = APP.onDemand(JobRunDAO.class);
  protected static final RunArgsDAO runArgsDAO = APP.onDemand(RunArgsDAO.class);

  protected static final NamespaceService namespaceService = new NamespaceService(namespaceDAO);
  protected static final JobService jobService =
      new JobService(jobDAO, jobVersionDAO, jobRunDAO, runArgsDAO);

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

  @BeforeClass
  public static void setup() throws UnexpectedException {
    marquez.core.models.Namespace generatedNamespace =
        namespaceService.create(Generator.genNamespace());
    NAMESPACE_NAME = generatedNamespace.getName();

    marquez.core.models.Job job = Generator.genJob(generatedNamespace.getGuid());
    marquez.core.models.Job createdJob = jobService.createJob(NAMESPACE_NAME, job);

    CREATED_JOB_NAME = createdJob.getName();
    CREATED_JOB_RUN_UUID = createdJob.getNamespaceGuid();
  }

  @Before
  public void createJobRun() throws UnexpectedException {
    marquez.core.models.JobRun createdJobRun =
        jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    CREATED_JOB_RUN_UUID = createdJobRun.getGuid();
  }

  @Test
  public void testJobRunCreationCreatesJobRunState() {
    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID);
    assertEquals(JobRunState.State.NEW, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunUpdateCreatesJobRunState() {
    jobRunDAO.updateState(CREATED_JOB_RUN_UUID, JobRunState.State.toInt(JobRunState.State.RUNNING));

    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID);
    assertEquals(JobRunState.State.RUNNING, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunGetter() {
    marquez.core.models.JobRun returnedJobRun = jobRunDAO.findJobRunById(CREATED_JOB_RUN_UUID);
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
