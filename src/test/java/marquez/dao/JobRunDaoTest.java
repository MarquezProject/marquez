package marquez.dao;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;
import marquez.api.JobRunBaseTest;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Generator;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;
import marquez.core.models.Namespace;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.junit.After;
import org.junit.AfterClass;
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

  protected static UUID CREATED_NAMESPACE_UUID;

  protected static final String JOB_RUN_ARGS = "{'key': 'value'}";

  protected static final NamespaceDAO namespaceDAO = APP.onDemand(NamespaceDAO.class);
  protected static final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  protected static final JobVersionDAO jobVersionDAO = APP.onDemand(JobVersionDAO.class);
  protected static final JobRunDAO jobRunDAO = APP.onDemand(JobRunDAO.class);
  protected static final JobRunStateDAO jobRunStateDAO = APP.onDemand(JobRunStateDAO.class);
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
    Namespace generatedNamespace = namespaceService.create(Generator.genNamespace());
    NAMESPACE_NAME = generatedNamespace.getName();
    CREATED_NAMESPACE_UUID = generatedNamespace.getGuid();

    marquez.core.models.Job job = Generator.genJob(generatedNamespace.getGuid());
    marquez.core.models.Job createdJob = jobService.createJob(NAMESPACE_NAME, job);

    CREATED_JOB_NAME = createdJob.getName();
    CREATED_JOB_RUN_UUID = createdJob.getNamespaceGuid();
  }

  @Before
  public void createJobRun() throws UnexpectedException {
    JobRun createdJobRun =
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
    JobRun returnedJobRun = jobRunDAO.findJobRunById(CREATED_JOB_RUN_UUID);
    assertNull(returnedJobRun.getNominalStartTime());
    assertNull(returnedJobRun.getNominalEndTime());
    assertEquals(
        JobRunState.State.NEW, JobRunState.State.fromInt(returnedJobRun.getCurrentState()));
  }

  @Test
  public void testLatestGetJobRunStateForJobId() {
    assertThat(jobRunStateDAO.findByLatestJobRun(CREATED_JOB_RUN_UUID))
        .isEqualTo(getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID));
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

  @After
  public void cleanup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_states where job_run_guid = '%s'",
                      CREATED_JOB_RUN_UUID));
              handle.execute(
                  format("delete from job_runs where guid = '%s'", CREATED_JOB_RUN_UUID));
            });
  }

  @AfterClass
  public static void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "DELETE from job_versions where guid in (select job_versions.guid as guid from jobs inner join job_versions on job_versions.job_guid=jobs.guid and jobs.namespace_guid='%s')",
                      CREATED_NAMESPACE_UUID));
              handle.execute(
                  format("delete from jobs where namespace_guid = '%s'", CREATED_NAMESPACE_UUID));
              handle.execute(
                  format("delete from namespaces where guid = '%s'", CREATED_NAMESPACE_UUID));
            });
  }
}
