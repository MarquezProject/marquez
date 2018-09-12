package marquez.api;

import static java.lang.String.format;
import static marquez.api.JobRunState.State.fromInt;
import static marquez.api.JobRunState.State.toInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
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

  static final String TEST_JOB_GUID = "063d736b-8232-422b-9a39-20310f72cbdd";
  static final String TEST_JOB_RUN_VERSION_GUID = "ba53171c-9e19-4c1b-a7cd-592b7400dea9";
  static final String TEST_JOB_RUN_DEFINITION_GUID = "0322c4e4-7b9f-4082-a583-26f6ffa4d78b";

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
    //TODO: Fix this.
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

  // @Ignore("in development")
  @Test
  public void testJobRunCreationEndToEnd() throws IOException {
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
    assertEquals(201, res.getStatus());
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
                    format(
                        "delete from job_run_states where job_run_guid = '%s'",
                            returnedId));
                handle.execute(format("delete from job_runs where guid = '%s'", returnedId));
              });
    }
  }

  @Ignore("in development")
  @Test
  public void testJobRunStateCreatedUponJobRunCreation() {
    final String path = "/job_runs";
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path(path)
            .request()
            .post(Entity.json("{'k':'v'}"));
    assertEquals(res.getStatus(), 200);
    assertEquals(res.readEntity(String.class), "pong");
  }

  @Ignore("in development")
  @Test
  public void testJobRunGetterEndToEnd() {
    final String path = "/job_runs";
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path(path)
            .request()
            .post(Entity.json("{'k':'v'}"));
    assertEquals(200, res.getStatus());
    assertEquals(res.readEntity(String.class), "pong");
  }

  @Ignore("in development")
  @Test
  public void testJobRunStateGetter() {
    final String path = "/job_run_states";
    final Response res =
        daoSetup
            .client()
            .target(URI.create("http://localhost:" + daoSetup.getLocalPort()))
            .path(path)
            .request()
            .post(Entity.json("{'k':'v'}"));
    assertEquals(res.getStatus(), 200);
    assertEquals(res.readEntity(String.class), "pong");
  }
}
