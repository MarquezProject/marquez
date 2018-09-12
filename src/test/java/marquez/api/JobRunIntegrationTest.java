package marquez.api;

import static java.lang.String.format;
import static marquez.api.JobRunState.State.fromInt;
import static marquez.api.JobRunState.State.toInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.db.dao.JobRunDAO;
import marquez.db.dao.JobRunStateDAO;
import marquez.db.dao.fixtures.DAOSetup;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunIntegrationTest {
  private static Logger LOG = LoggerFactory.getLogger(JobRunIntegrationTest.class);

  static final String TEST_JOB_GUID = UUID.randomUUID().toString();
  static final String TEST_JOB_RUN_VERSION_GUID = UUID.randomUUID().toString();
  static final String TEST_JOB_RUN_DEFINITION_GUID = UUID.randomUUID().toString();

  @ClassRule public static final DAOSetup daoSetup = new DAOSetup();
  final JobRunDAO jobRunDAO = daoSetup.onDemand(JobRunDAO.class);
  final JobRunStateDAO jobRunStateDAO = daoSetup.onDemand(JobRunStateDAO.class);

  private JobRun NEW_JOB_RUN;

  @BeforeClass
  public static void setUp() {
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "merge into jobs (guid, name, category, description) values "
                          + "('%s', 'my_job', 'testing', 'fake job for reference');",
                      TEST_JOB_GUID));
              handle.execute(
                  format(
                      "merge into job_versions (guid, input_dataset, output_dataset, job_guid) values "
                          + "('%s', 'input_set1', 'output_set1', '%s');",
                      TEST_JOB_RUN_VERSION_GUID, TEST_JOB_GUID));
              handle.execute(
                  format(
                      "merge into job_run_definitions (guid, job_version_guid, run_args_json, nominal_time) values "
                          + "('%s', '%s', '--my-favorite-flag', '2018-09-06T05:24:50+00:00');",
                      TEST_JOB_RUN_DEFINITION_GUID, TEST_JOB_RUN_VERSION_GUID));
            });
  }

  @AfterClass
  public static void tearDown() {
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_definitions where guid = '%s'",
                      TEST_JOB_RUN_DEFINITION_GUID));
              handle.execute(
                  format("delete from job_versions where guid = '%s'", TEST_JOB_RUN_VERSION_GUID));
              handle.execute(format("delete from jobs where guid = '%s'", TEST_JOB_GUID));
            });
  }

  @Before
  public void setUpNewJobRun() {
    final UUID jobRunUUID = UUID.randomUUID();
    NEW_JOB_RUN =
        new JobRun(
            jobRunUUID,
            new Timestamp(System.currentTimeMillis()),
            null,
            null,
            UUID.fromString(TEST_JOB_RUN_DEFINITION_GUID),
            toInt(JobRunState.State.NEW));
    jobRunDAO.insert(NEW_JOB_RUN);
  }

  @After
  public void cleanup() {
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_states where job_run_guid = '%s'",
                      NEW_JOB_RUN.getGuid()));
              handle.execute(
                  format("delete from job_runs where guid = '%s'", NEW_JOB_RUN.getGuid()));
            });
  }

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
            NEW_JOB_RUN.getCreatedAt(),
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

  @Test
  public void testJobRunCreationEndToEnd() {
    final String jobRunRequestString =
        format("{\"job_run_definition_guid\": \"%s\"}", TEST_JOB_RUN_DEFINITION_GUID);
    Entity jobRunRequestJsonAsEntity = Entity.json(jobRunRequestString);
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path("/job_runs")
            .request(MediaType.APPLICATION_JSON)
            .post(jobRunRequestJsonAsEntity);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    CreateJobRunResponse responseBody = res.readEntity(CreateJobRunResponse.class);
    UUID returnedId = responseBody.getExternalGuid();
    try {
      assertNotNull(returnedId);
      LOG.info("Returned id is: " + returnedId);
    } finally {
      // TODO: This doesn't clean up correctly. Find out why.
      // Temp workaround would be disable the AfterClass section.
      daoSetup
          .getJDBI()
          .useHandle(
              handle -> {
                handle.execute(
                    format("delete from job_run_states where job_run_guid = '%s'", returnedId));
                handle.execute(format("delete from job_runs where guid = '%s'", returnedId));
              });
    }
  }

  @Test
  public void testJobRunGetterEndToEnd() {
    GetJobRunResponse responseBody = getJobRunResponse(NEW_JOB_RUN.getGuid());

    assertEquals(JobRunState.State.NEW, JobRunState.State.valueOf(responseBody.getCurrentState()));
    assertNotNull(responseBody.getCreatedAt());
    assertNull(responseBody.getStartedAt());
    assertNull(responseBody.getEndedAt());
  }

  @Test
  public void testJobRunAfterUpdateEndToEnd() {
    JobRunState jrs = jobRunStateDAO.findJobLatestJobRunStateByJobRun(NEW_JOB_RUN.getGuid());

    final String path = "/job_run_states/" + jrs.getGuid();
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    GetJobRunStateResponse jrsResponse = res.readEntity(GetJobRunStateResponse.class);
    assertEquals(JobRunState.State.NEW, JobRunState.State.valueOf(jrsResponse.getState()));
    assertEquals(jrs.getGuid(), jrsResponse.getGuid());
  }

  private GetJobRunResponse getJobRunResponse(UUID jobRunGuid) {
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path(format("/job_runs/%s", jobRunGuid))
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    return res.readEntity(GetJobRunResponse.class);
  }
}
