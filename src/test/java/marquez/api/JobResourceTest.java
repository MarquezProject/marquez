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

package marquez.api;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.api.exceptions.ResourceExceptionMapper;
import marquez.api.models.Job;
import marquez.api.models.JobRequest;
import marquez.api.models.JobRunRequest;
import marquez.api.models.JobRunResponse;
import marquez.api.models.JobsResponse;
import marquez.api.resources.JobResource;
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
  private static final String NAMESPACE_NAME = "someNamespace";
  private static final Entity<?> EMPTY_PUT_BODY = Entity.json("");

  final int UNPROCESSABLE_ENTRY_STATUS_CODE = 422;

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(JOB_RESOURCE)
          .addProvider(ResourceExceptionMapper.class)
          .build();

  @Before
  public void clearMocks() {
    reset(MOCK_NAMESPACE_SERVICE);
    reset(MOCK_JOB_SERVICE);
  }

  @Test
  public void testJobCreationWithInvalidNamespace() throws MarquezServiceException {
    Job jobForJobCreationRequest = generateApiJob();

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

    Job jobForJobCreationRequest = generateApiJob();
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetJobWithInvalidNamespace() throws MarquezServiceException {
    Job jobForJobCreationRequest = generateApiJob();

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

  @Test
  public void testCreateJobBadInputs() throws MarquezServiceException {
    when(MOCK_JOB_SERVICE.createJob(any(), any())).thenThrow(new MarquezServiceException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Job jobForJobCreationRequest = generateApiJob();
    jobForJobCreationRequest.setLocation(null);

    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(UNPROCESSABLE_ENTRY_STATUS_CODE, res.getStatus());
  }

  @Test
  public void testDescriptionOptionalForCreateJobInputs() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Job jobForJobCreationRequest = generateApiJob();
    jobForJobCreationRequest.setDescription(null);

    insertJob(jobForJobCreationRequest);
    verify(MOCK_JOB_SERVICE, times(1)).createJob(any(), any());
  }

  @Test
  public void testGetJobNoSuchJob() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, "nosuchjob");
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

    String path = format("/api/v1/namespaces/%s/jobs/", NAMESPACE_NAME);
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllJobsInNamespaceErrorHandling() throws MarquezServiceException {
    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllJobsInNamespace(NAMESPACE_NAME))
        .thenThrow(new MarquezServiceException());

    String path = format("/api/v1/namespaces/%s/jobs/", NAMESPACE_NAME);
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testGetAllJobsInNamespace() throws MarquezServiceException {
    marquez.service.models.Job job1 = Generator.genJob();
    marquez.service.models.Job job2 = Generator.genJob();
    List<marquez.service.models.Job> jobsList = Arrays.asList(job1, job2);

    when(MOCK_NAMESPACE_SERVICE.exists(NAMESPACE_NAME)).thenReturn(true);
    when(MOCK_JOB_SERVICE.getAllJobsInNamespace(NAMESPACE_NAME)).thenReturn(jobsList);

    String path = format("/api/v1/namespaces/%s/jobs/", NAMESPACE_NAME);
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    List<Job> returnedJobs = res.readEntity(JobsResponse.class).getJobs();
    assertThat(returnedJobs).hasSize(jobsList.size());
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

  private Response getJobRun(String jobRunId) {
    String path = format("/api/v1/jobs/runs/%s", NAMESPACE_NAME, jobRunId);
    return resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
  }

  private Response getJob(String jobName) {
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, jobName);
    return resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
  }

  private Response insertJob(Job job) {
    JobRequest jobRequest =
        new JobRequest(
            job.getInputDataSetUrns(),
            job.getOutputDataSetUrns(),
            job.getLocation(),
            job.getDescription());
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, job.getName());
    return resources
        .client()
        .target(path)
        .request(MediaType.APPLICATION_JSON)
        .put(entity(jobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }

  private Response insertJobRun(JobRunResponse jobRun) {
    JobRunRequest jobRequest =
        new JobRunRequest(
            jobRun.getNominalStartTime(), jobRun.getNominalEndTime(), jobRun.getRunArgs());
    String path = format("/api/v1/namespaces/%s/jobs/%s/runs", NAMESPACE_NAME, "somejob");
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

  Job generateApiJob() {
    String jobName = "myJob" + System.currentTimeMillis();
    final String location = "someLocation";
    final String description = "someDescription";
    final List<String> inputList = Collections.singletonList("input1");
    final List<String> outputList = Collections.singletonList("output1");
    return new Job(jobName, null, inputList, outputList, location, description);
  }

  JobRunResponse generateApiJobRun() {
    return new JobRunResponse(
        UUID.randomUUID(),
        "2018-07-14T19:43:37+0000",
        "2018-07-14T19:43:37+0000",
        "{'key': 'value'}",
        "NEW");
  }
}
