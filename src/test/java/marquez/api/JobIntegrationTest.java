package marquez.api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.net.URI;
import java.util.Collections;
import java.util.List;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobIntegrationTest extends JobRunBaseTest {

  protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  protected static String NAMESPACE_NAME;
  protected static UUID CREATED_NAMESPACE_UUID;

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
    CREATED_NAMESPACE_UUID = generatedNamespace.getGuid();

    marquez.core.models.Job job = Generator.genJob(generatedNamespace.getGuid());
    marquez.core.models.Job createdJob = jobService.createJob(NAMESPACE_NAME, job);

    CREATED_JOB_NAME = createdJob.getName();
    CREATED_JOB_RUN_UUID = createdJob.getNamespaceGuid();
  }

  @Test
  public void testJobCreationResponseEndToEnd() {
    Job jobForJobCreationRequest = generateApiJob();

    Response res = createJobOnNamespace(NAMESPACE_NAME, jobForJobCreationRequest);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    evaluateResponse(res, jobForJobCreationRequest);
  }

  @Test
  public void testJobGetterResponseEndToEnd() {
    Job jobForJobCreationRequest = generateApiJob();

    Response res = createJobOnNamespace(NAMESPACE_NAME, jobForJobCreationRequest);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());

    String path =
        format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, jobForJobCreationRequest.getName());
    Response returnedJobResponse =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(path)
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), returnedJobResponse.getStatus());
    evaluateResponse(returnedJobResponse, jobForJobCreationRequest);
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
    UUID returnedId = responseBody.getRunId();
    try {
      assertNotNull(returnedId);
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

    assertEquals(marquez.core.models.JobRunState.State.NEW.name(), responseBody.getRunState());
    assertNull(responseBody.getNominalStartTime());
    assertNull(responseBody.getNominalEndTime());
  }

  @Test
  public void testJobRunAfterUpdateEndToEnd() {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/complete", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
  }

  private void evaluateResponse(Response res, Job inputJob) {
    Job responseJob = res.readEntity(Job.class);
    assertEquals(responseJob.getName(), inputJob.getName());
    assertEquals(responseJob.getDescription(), inputJob.getDescription());
    assertEquals(responseJob.getLocation(), inputJob.getLocation());

    // TODO: Re-enable once marquez-188 is resolved
    // assertEquals(returnedJob.getInputDataSetUrns(), inputList);
    // assertEquals(returnedJob.getOutputDataSetUrns(), outputList);
    // assertNotNull(returnedJob.getCreatedAt());
  }

  private Response createJobOnNamespace(String namespace, Job job) {
    CreateJobRequest createJobRequest =
        new CreateJobRequest(
            job.getLocation(),
            job.getDescription(),
            job.getInputDataSetUrns(),
            job.getOutputDataSetUrns());

    String path = format("/api/v1/namespaces/%s/jobs/%s", namespace, job.getName());
    return APP.client()
        .target(URI.create("http://localhost:" + APP.getLocalPort()))
        .path(path)
        .request(MediaType.APPLICATION_JSON)
        .put(Entity.json(createJobRequest));
  }

  static Job generateApiJob() {
    String jobName = "myJob" + System.currentTimeMillis();
    final String location = "someLocation";
    final String description = "someDescription";
    final List<String> inputList = Collections.singletonList("input1");
    final List<String> outputList = Collections.singletonList("output1");
    return new Job(jobName, null, inputList, outputList, location, description);
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
