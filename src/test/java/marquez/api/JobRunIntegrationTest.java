package marquez.api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Generator;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;
import marquez.dao.JobDAO;
import marquez.dao.JobRunDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.NamespaceDAO;
import marquez.dao.RunArgsDAO;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunIntegrationTest extends JobRunBaseTest {
  private static Logger LOG = LoggerFactory.getLogger(JobRunIntegrationTest.class);
  protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

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
  public void testJobRunCreationEndToEnd() throws JsonProcessingException {
    Entity createJobRunRequestEntity =
        Entity.json(MAPPER.writeValueAsString(new CreateJobRunRequest(null, null, JOB_RUN_ARGS)));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + NAMESPACE_NAME + "/jobs/" + CREATED_JOB_NAME + "/runs")
            .request(MediaType.APPLICATION_JSON)
            .post(createJobRunRequestEntity);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    JobRun responseBody = res.readEntity(JobRun.class);
    UUID returnedId = responseBody.getGuid();
    try {
      assertNotNull(returnedId);
      LOG.info("Returned id is: " + returnedId);
    } finally {
      APP.getJDBI()
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
    JobRun responseBody = getJobRunApiResponse(CREATED_JOB_RUN_UUID);

    assertEquals(marquez.core.models.JobRunState.State.NEW.name(), responseBody.getCurrentState());
    assertNull(responseBody.getNominalStartTime());
    assertNull(responseBody.getNominalEndTime());
  }

  @Test
  public void testJobRunAfterUpdateEndToEnd() throws JsonProcessingException {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/complete", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
  }

  private JobRun getJobRunApiResponse(UUID jobRunGuid) {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s", jobRunGuid))
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    return res.readEntity(JobRun.class);
  }
}
