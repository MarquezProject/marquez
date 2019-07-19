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
import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.models.JobRequest;
import marquez.api.models.JobResponse;
import marquez.api.models.JobRunRequest;
import marquez.api.models.JobRunResponse;
import marquez.api.models.JobRunsResponse;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Generator;
import marquez.service.models.JobRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobResourceTest {

  private static final JobService MOCK_JOB_SERVICE = mock(JobService.class);
  private static final NamespaceService MOCK_NAMESPACE_SERVICE = mock(NamespaceService.class);
  private static final JobResource JOB_RESOURCE =
      new JobResource(MOCK_NAMESPACE_SERVICE, MOCK_JOB_SERVICE);
  private static final NamespaceName NAMESPACE_NAME = NamespaceName.of("test");
  private static final Entity<?> EMPTY_PUT_BODY = Entity.json("");

  private static final int TEST_LIMIT = 0;
  private static final int TEST_OFFSET = 100;

  final int UNPROCESSABLE_ENTRY_STATUS_CODE = 422;

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(JOB_RESOURCE)
          .addProvider(MarquezServiceExceptionMapper.class)
          .build();

  @Before
  public void clearMocks() {
    reset(MOCK_NAMESPACE_SERVICE);
    reset(MOCK_JOB_SERVICE);
  }

  @Test
  public void testJobCreationWithInvalidNamespace() throws MarquezServiceException {
    JobResponse jobForJobCreationRequest = generateApiJob();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(false);
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCreateJobInternalErrorHandling() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.createJob(any(), any())).thenThrow(new MarquezServiceException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    JobResponse jobForJobCreationRequest = generateApiJob();
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetJobWithInvalidNamespace() throws MarquezServiceException {
    JobResponse jobForJobCreationRequest = generateApiJob();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(false);
    Response res = getJob(jobForJobCreationRequest.getName());
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetJobInternalErrorHandling() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenThrow(new MarquezServiceException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Response res = getJob("someName");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test(expected = NullPointerException.class)
  public void testCreateJobBadInputs() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.createJob(any(), any())).thenThrow(new MarquezServiceException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    JobResponse jobForJobCreationRequest = generateApiJobWithNullLocation();

    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(UNPROCESSABLE_ENTRY_STATUS_CODE, res.getStatus());
  }

  @Test
  public void testDescriptionOptionalForCreateJobInputs() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    JobResponse jobForJobCreationRequest = generateApiJobWithNullDescription();

    insertJob(jobForJobCreationRequest);
    verify(MOCK_JOB_SERVICE, times(1)).createJob(any(), any());
  }

  @Test
  public void testGetJobNoSuchJob() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME.getValue(), "nosuchjob");
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetJobRunNoSuchJob() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    Response res = getJobRun("abc123nojustjobid");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllJobsInNamespaceWithInvalidNamespace() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(false);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());

    String path = format("/api/v1/namespaces/%s/jobs", NAMESPACE_NAME.getValue());
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllJobsInNamespaceErrorHandling() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);

    when(MOCK_JOB_SERVICE.getAllJobsInNamespace(eq(NAMESPACE_NAME.getValue()), any(), any()))
        .thenThrow(new MarquezServiceException());

    String path = format("/api/v1/namespaces/%s/jobs", NAMESPACE_NAME.getValue());
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllJobsInNamespace() throws MarquezServiceException {
    marquez.service.models.Job job1 = Generator.genJob();
    marquez.service.models.Job job2 = Generator.genJob();
    List<marquez.service.models.Job> jobsList = Arrays.asList(job1, job2);

    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllJobsInNamespace(eq(NAMESPACE_NAME.getValue()), any(), any()))
        .thenReturn(jobsList);

    String path = format("/api/v1/namespaces/%s/jobs", NAMESPACE_NAME.getValue());
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());

    String jsonResponse = res.readEntity(String.class);
    assertNotNull(jsonResponse);
    assertThat(jsonResponse).contains(job1.getName());
    assertThat(jsonResponse).contains(job2.getName());
  }

  @Test
  public void testGetAllJobsInNamespaceVerifyInputs() throws MarquezServiceException {
    marquez.service.models.Job job1 = Generator.genJob();
    marquez.service.models.Job job2 = Generator.genJob();
    List<marquez.service.models.Job> jobsList = Arrays.asList(job1, job2);

    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllJobsInNamespace(eq(NAMESPACE_NAME.getValue()), any(), any()))
        .thenReturn(jobsList);

    JOB_RESOURCE.listJobs(NAMESPACE_NAME.getValue(), TEST_LIMIT, TEST_OFFSET);
    verify(MOCK_JOB_SERVICE, times(1))
        .getAllJobsInNamespace(NAMESPACE_NAME.getValue(), TEST_LIMIT, TEST_OFFSET);
  }

  @Test
  public void testCreateJobRunInternalErrorHandling() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.createJobRun(any(), any(), any(), any(), any()))
        .thenThrow(new MarquezServiceException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));
    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.of(Generator.genJob()));

    JobRunResponse jobForJobCreationRequest = generateApiJobRun();
    Response res = insertJobRun(jobForJobCreationRequest);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testStartJobRunInternalErrorHandling() throws MarquezServiceException, Exception {
    UUID externalRunId = UUID.randomUUID();
    JobRun generatedJobRun = Generator.genJobRun();

    when(MOCK_JOB_SERVICE.getJobRun(any())).thenReturn(Optional.of(generatedJobRun));
    when(MOCK_JOB_SERVICE.updateJobRunState(any(), any())).thenThrow(new MarquezServiceException());
    Response res = markJobRunAsRunning(externalRunId);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCompleteJobRunInternalErrorHandling() throws MarquezServiceException, Exception {
    UUID externalRunId = UUID.randomUUID();
    JobRun generatedJobRun = Generator.genJobRun();

    when(MOCK_JOB_SERVICE.getJobRun(any())).thenReturn(Optional.of(generatedJobRun));
    when(MOCK_JOB_SERVICE.updateJobRunState(any(), any())).thenThrow(new MarquezServiceException());
    Response res = markJobRunComplete(externalRunId);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testAbortJobRunInternalErrorHandling() throws MarquezServiceException, Exception {
    UUID externalRunId = UUID.randomUUID();
    JobRun generatedJobRun = Generator.genJobRun();

    when(MOCK_JOB_SERVICE.getJobRun(any())).thenReturn(Optional.of(generatedJobRun));
    when(MOCK_JOB_SERVICE.updateJobRunState(any(), any())).thenThrow(new MarquezServiceException());
    Response res = markJobRunAborted(externalRunId);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testFailJobRunInternalErrorHandling() throws MarquezServiceException, Exception {
    UUID externalRunId = UUID.randomUUID();
    JobRun generatedJobRun = Generator.genJobRun();

    when(MOCK_JOB_SERVICE.getJobRun(any())).thenReturn(Optional.of(generatedJobRun));
    when(MOCK_JOB_SERVICE.updateJobRunState(any(), any())).thenThrow(new MarquezServiceException());
    Response res = markJobRunFailed(externalRunId);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobRunCreationWithInvalidNamespace() throws MarquezServiceException {
    JobRunResponse jobRunForJobRunCreationRequest = generateApiJobRun();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(false);
    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.of(Generator.genJob()));

    Response res = insertJobRun(jobRunForJobRunCreationRequest);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobRunCreationWithInvalidJob() throws MarquezServiceException {
    JobRunResponse jobRunForJobRunCreationRequest = generateApiJobRun();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);

    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    Response res = insertJobRun(jobRunForJobRunCreationRequest);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobRunStartWithInvalidJob() throws MarquezServiceException, Exception {
    JobRunResponse jobRunForJobRunCreationRequest = generateApiJobRun();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);

    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    Response res = markJobRunAsRunning(jobRunForJobRunCreationRequest.getRunId());
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobRunStatusUpdateWithInvalidExternalRunId()
      throws MarquezServiceException, Exception {
    UUID externalRunId = UUID.randomUUID();

    when(MOCK_JOB_SERVICE.getJobRun(externalRunId)).thenReturn(Optional.empty());
    Response res = markJobRunComplete(externalRunId);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());

    res = markJobRunFailed(externalRunId);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());

    res = markJobRunAborted(externalRunId);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobRunCompleteWithUnparseableRunId() throws Exception {
    String externalRunId = "this cannot be parsed into a UUID";

    Response res = markJobRunComplete(externalRunId);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), res.getStatus());

    res = markJobRunFailed(externalRunId);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), res.getStatus());

    res = markJobRunAborted(externalRunId);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllRunsOfJob() throws MarquezServiceException {
    JobResponse job = generateApiJob();
    List<JobRun> jobRuns = Arrays.asList(Generator.genJobRun(), Generator.genJobRun());
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllRunsOfJob(NAMESPACE_NAME, job.getName(), TEST_LIMIT, TEST_OFFSET))
        .thenReturn(jobRuns);

    final Response response =
        JOB_RESOURCE.getRunsForJob(
            NAMESPACE_NAME.getValue(), job.getName(), TEST_LIMIT, TEST_OFFSET);
    final JobRunsResponse jobRunsResponse = (JobRunsResponse) response.getEntity();

    assertEquals(jobRuns.size(), jobRunsResponse.getRuns().size());
  }

  @Test
  public void testGetAllRunsOfJob_namespaceNotFound() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(false);

    assertThatExceptionOfType(NamespaceNotFoundException.class)
        .isThrownBy(
            () ->
                JOB_RESOURCE.getRunsForJob(
                    NAMESPACE_NAME.getValue(), "nonexistent_job", TEST_LIMIT, TEST_OFFSET));
  }

  @Test
  public void testGetAllRunsOfJob_noJobRuns() throws MarquezServiceException {
    JobResponse job = generateApiJob();
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    List<JobRun> noJobRuns = new ArrayList<JobRun>();
    when(MOCK_JOB_SERVICE.getAllRunsOfJob(NAMESPACE_NAME, job.getName(), TEST_LIMIT, TEST_OFFSET))
        .thenReturn(noJobRuns);

    final Response response =
        JOB_RESOURCE.getRunsForJob(
            NAMESPACE_NAME.getValue(), job.getName(), TEST_LIMIT, TEST_OFFSET);
    final JobRunsResponse jobRunsResponse = (JobRunsResponse) response.getEntity();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(0, jobRunsResponse.getRuns().size());
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetAllRunsOfJob_NamespaceService_Exception() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenThrow(MarquezServiceException.class);

    JOB_RESOURCE.getRunsForJob(NAMESPACE_NAME.getValue(), "some job", TEST_LIMIT, TEST_OFFSET);
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetAllRunsOfJob_JobService_Exception() throws MarquezServiceException {
    JobResponse job = generateApiJob();
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllRunsOfJob(NAMESPACE_NAME, job.getName(), TEST_LIMIT, TEST_OFFSET))
        .thenThrow(MarquezServiceException.class);

    JOB_RESOURCE.getRunsForJob(NAMESPACE_NAME.getValue(), job.getName(), TEST_LIMIT, TEST_OFFSET);
  }

  private Response getJobRun(String jobRunId) {
    String path = format("/api/v1/jobs/runs/%s", NAMESPACE_NAME.getValue(), jobRunId);
    return resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
  }

  private Response getJob(String jobName) {
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME.getValue(), jobName);
    return resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
  }

  private Response insertJob(JobResponse job) {
    JobRequest jobRequest =
        new JobRequest(
            job.getInputDatasetUrns(),
            job.getOutputDatasetUrns(),
            job.getLocation(),
            job.getDescription().orElse(null));
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME.getValue(), job.getName());
    return resources
        .client()
        .target(path)
        .request(MediaType.APPLICATION_JSON)
        .put(entity(jobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }

  private Response insertJobRun(JobRunResponse jobRun) {
    JobRunRequest jobRequest =
        new JobRunRequest(
            jobRun.getNominalStartTime().orElse(""),
            jobRun.getNominalEndTime().orElse(""),
            jobRun.getRunArgs().orElse(""));
    String path =
        format("/api/v1/namespaces/%s/jobs/%s/runs", NAMESPACE_NAME.getValue(), "somejob");
    return resources
        .client()
        .target(path)
        .request(MediaType.APPLICATION_JSON)
        .post(entity(jobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }

  private Response markJobRunAsRunning(UUID jobRunId) {
    return markJobRunAsRunning(jobRunId.toString());
  }

  private Response markJobRunAsRunning(String jobRunId) {
    return markJobRunWithState(jobRunId, "run");
  }

  private Response markJobRunComplete(UUID jobRunId) {
    return markJobRunComplete(jobRunId.toString());
  }

  private Response markJobRunComplete(String jobRunId) {
    return markJobRunWithState(jobRunId, "complete");
  }

  private Response markJobRunAborted(UUID jobRunId) {
    return markJobRunAborted(jobRunId.toString());
  }

  private Response markJobRunAborted(String jobRunId) {
    return markJobRunWithState(jobRunId, "abort");
  }

  private Response markJobRunFailed(UUID jobRunId) {
    return markJobRunFailed(jobRunId.toString());
  }

  private Response markJobRunFailed(String jobRunId) {
    return markJobRunWithState(jobRunId, "fail");
  }

  private Response markJobRunWithState(String jobRunId, String newState) {
    String path = format("/api/v1/jobs/runs/%s/%s", jobRunId, newState);
    return resources.client().target(path).request(MediaType.APPLICATION_JSON).put(EMPTY_PUT_BODY);
  }

  JobResponse generateApiJob() {
    return generateApiJob("someLocation", "someDescription");
  }

  JobResponse generateApiJobWithNullLocation() {
    return generateApiJob(null, "someDescription");
  }

  JobResponse generateApiJobWithNullDescription() {
    return generateApiJob("someLocation", null);
  }

  JobResponse generateApiJob(String location, String description) {
    String jobName = "myJob" + System.currentTimeMillis();
    final List<String> inputList = Collections.singletonList("input1");
    final List<String> outputList = Collections.singletonList("output1");
    String createdAt = ISO_INSTANT.format(Instant.now());
    return new JobResponse(
        jobName, createdAt, createdAt, inputList, outputList, location, description);
  }

  JobRunResponse generateApiJobRun() {
    return new JobRunResponse(
        UUID.randomUUID().toString(),
        "2018-07-14T19:43:37+0000",
        "2018-07-14T19:43:37+0000",
        "{'key': 'value'}",
        "NEW");
  }
}
