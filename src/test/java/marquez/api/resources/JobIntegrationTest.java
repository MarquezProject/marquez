/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.api.resources;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.service.models.ServiceModelGenerator.newJobWithNameSpaceId;
import static marquez.service.models.ServiceModelGenerator.newNamespace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.models.JobRequest;
import marquez.api.models.JobResponse;
import marquez.api.models.JobRunRequest;
import marquez.api.models.JobRunResponse;
import marquez.db.JobDao;
import marquez.db.JobRunArgsDao;
import marquez.db.JobRunDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import marquez.service.models.Namespace;
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

  protected static final NamespaceDao namespaceDao = APP.onDemand(NamespaceDao.class);
  protected static final JobDao jobDao = APP.onDemand(JobDao.class);
  protected static final JobVersionDao jobVersionDao = APP.onDemand(JobVersionDao.class);
  protected static final JobRunDao jobRunDao = APP.onDemand(JobRunDao.class);
  protected static final JobRunArgsDao jobRunArgsDao = APP.onDemand(JobRunArgsDao.class);

  protected static NamespaceService namespaceService;
  protected static final JobService jobService =
      new JobService(jobDao, jobVersionDao, jobRunDao, jobRunArgsDao);

  @BeforeClass
  public static void setup() throws MarquezServiceException {
    namespaceService = new NamespaceService(namespaceDao);
    Namespace generatedNamespace = namespaceService.createOrUpdate(newNamespace());
    NAMESPACE_NAME = generatedNamespace.getName();
    CREATED_NAMESPACE_UUID = generatedNamespace.getUuid();

    marquez.service.models.Job job = newJobWithNameSpaceId(generatedNamespace.getUuid());
    marquez.service.models.Job createdJob = jobService.createJob(NAMESPACE_NAME, job);

    CREATED_JOB_NAME = createdJob.getName();
  }

  @Test
  public void testJobCreationResponseEndToEnd() {
    JobResponse jobForJobCreationRequest = generateApiJob();

    Response res = createJobOnNamespace(NAMESPACE_NAME, jobForJobCreationRequest);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    evaluateResponse(res, jobForJobCreationRequest);
  }

  @Test
  public void testJobGetterResponseEndToEnd() {
    JobResponse jobForJobCreationRequest = generateApiJob();

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
  public void createJobRun() throws MarquezServiceException {
    JobRun createdJobRun =
        jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    CREATED_JOB_RUN_UUID = createdJobRun.getUuid();
  }

  @Test
  public void testJobRunCreationEndToEnd() throws JsonProcessingException {
    Entity createJobRunRequestEntity =
        Entity.json(MAPPER.writeValueAsString(new JobRunRequest(null, null, JOB_RUN_ARGS)));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + NAMESPACE_NAME + "/jobs/" + CREATED_JOB_NAME + "/runs")
            .request(MediaType.APPLICATION_JSON)
            .post(createJobRunRequestEntity);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    JobRunResponse responseBody = res.readEntity(JobRunResponse.class);
    UUID returnedId =
        responseBody.getRunId() != null ? UUID.fromString(responseBody.getRunId()) : null;
    try {
      assertNotNull(returnedId);
    } finally {
      APP.getJDBI()
          .useHandle(
              handle -> {
                handle.execute(
                    format("delete from job_run_states where job_run_uuid = '%s'", returnedId));
                handle.execute(format("delete from job_runs where uuid = '%s'", returnedId));
              });
    }
  }

  @Test
  public void testJobRunGetterEndToEnd() {
    JobRunResponse responseBody = getJobRunApiResponse(CREATED_JOB_RUN_UUID);

    assertEquals(JobRunState.State.NEW.name(), responseBody.getRunState());
    assertNull(responseBody.getNominalStartTime().orElse(null));
    assertNull(responseBody.getNominalEndTime().orElse(null));
  }

  @Test
  public void testJobRunRetrievalWithMultipleJobRuns() throws MarquezServiceException {
    JobRun secondCreatedJobRun =
        jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    final UUID secondJobRunUUID = secondCreatedJobRun.getUuid();

    try {
      assertThat(jobService.getJobRun(CREATED_JOB_RUN_UUID).get().getUuid())
          .isEqualByComparingTo(CREATED_JOB_RUN_UUID);
      assertThat(jobService.getJobRun(secondJobRunUUID).get().getUuid())
          .isEqualByComparingTo(secondJobRunUUID);
    } finally {
      APP.getJDBI()
          .useHandle(
              handle -> {
                handle.execute(
                    format(
                        "delete from job_run_states where job_run_uuid = '%s'", secondJobRunUUID));
                handle.execute(format("delete from job_runs where uuid = '%s'", secondJobRunUUID));
              });
    }
  }

  @Test
  public void testJobRunAfterCompletionEndToEnd() {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/complete", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    final JobRunResponse getJobRunResponse = getJobRunApiResponse(CREATED_JOB_RUN_UUID);
    assertThat(getJobRunResponse.getRunState()).isEqualTo(JobRunState.State.COMPLETED.name());
  }

  @Test
  public void testJobRunAfterMarkedStartedEndToEnd() {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/run", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    final JobRunResponse getJobRunResponse = getJobRunApiResponse(CREATED_JOB_RUN_UUID);
    assertThat(getJobRunResponse.getRunState()).isEqualTo(JobRunState.State.RUNNING.name());
  }

  @Test
  public void testJobRunAfterMarkedFailedEndToEnd() {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/fail", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    final JobRunResponse getJobRunResponse = getJobRunApiResponse(CREATED_JOB_RUN_UUID);
    assertThat(getJobRunResponse.getRunState()).isEqualTo(JobRunState.State.FAILED.name());
  }

  @Test
  public void testJobRunAfterMarkedAbortedEndToEnd() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/abort", CREATED_JOB_RUN_UUID))
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(""));

    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    final JobRunResponse getJobRunResponse = getJobRunApiResponse(CREATED_JOB_RUN_UUID);
    assertThat(getJobRunResponse.getRunState()).isEqualTo(JobRunState.State.ABORTED.name());
  }

  private void evaluateResponse(Response res, JobResponse inputJob) {
    JobResponse responseJob = res.readEntity(JobResponse.class);
    assertEquals(inputJob.getName(), responseJob.getName());
    assertEquals(inputJob.getDescription(), responseJob.getDescription());
    assertEquals(inputJob.getLocation(), responseJob.getLocation());

    assertEquals(inputJob.getInputDatasetUrns(), responseJob.getInputDatasetUrns());
    assertEquals(inputJob.getOutputDatasetUrns(), responseJob.getOutputDatasetUrns());

    assertNotNull(responseJob.getCreatedAt());
  }

  private Response createJobOnNamespace(String namespace, JobResponse job) {
    JobRequest jobRequest =
        new JobRequest(
            job.getInputDatasetUrns(),
            job.getOutputDatasetUrns(),
            job.getLocation(),
            job.getDescription().orElse(null));

    String path = format("/api/v1/namespaces/%s/jobs/%s", namespace, job.getName());
    return APP.client()
        .target(URI.create("http://localhost:" + APP.getLocalPort()))
        .path(path)
        .request(MediaType.APPLICATION_JSON)
        .put(Entity.json(jobRequest));
  }

  static JobResponse generateApiJob() {
    String jobName = "myJob" + System.currentTimeMillis();
    final String location = "someLocation";
    final String description = "someDescription";
    final List<String> inputList = Collections.singletonList("input1");
    final List<String> outputList = Collections.singletonList("output1");
    String createdAt = ISO_INSTANT.format(Instant.now());
    return new JobResponse(
        jobName, createdAt, createdAt, inputList, outputList, location, description);
  }

  private JobRunResponse getJobRunApiResponse(UUID jobRunUuid) {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s", jobRunUuid))
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    return res.readEntity(JobRunResponse.class);
  }

  @After
  public void cleanup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_states where job_run_uuid = '%s'",
                      CREATED_JOB_RUN_UUID));
              handle.execute(
                  format("delete from job_runs where uuid = '%s'", CREATED_JOB_RUN_UUID));
            });
  }

  @AfterClass
  public static void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "DELETE from job_versions where uuid in (select job_versions.uuid as uuid from jobs inner join job_versions on job_versions.job_uuid=jobs.uuid and jobs.namespace_uuid='%s')",
                      CREATED_NAMESPACE_UUID));
              handle.execute(
                  format("delete from jobs where namespace_uuid = '%s'", CREATED_NAMESPACE_UUID));
              handle.execute(
                  format("delete from namespaces where uuid = '%s'", CREATED_NAMESPACE_UUID));
            });
  }
}
