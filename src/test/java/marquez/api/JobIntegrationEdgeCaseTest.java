package marquez.api;

import io.dropwizard.testing.junit.ResourceTestRule;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.ResourceExceptionMapper;
import marquez.core.models.Generator;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;
import marquez.resources.JobResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobIntegrationEdgeCaseTest {

  private static final JobService MOCK_JOB_SERVICE = mock(JobService.class);
  private static final NamespaceService MOCK_NAMESPACE_SERVICE = mock(NamespaceService.class);
  private static final JobResource JOB_RESOURCE =
      new JobResource(MOCK_NAMESPACE_SERVICE, MOCK_JOB_SERVICE);
  private static final String NAMESPACE_NAME = "someNamespace";

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
  public void testJobCreationWithInvalidNamespace() throws UnexpectedException {
    Job jobForJobCreationRequest = generateApiJob();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(false);
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCreateJobInternalErrorHandling() throws UnexpectedException {
    when(MOCK_JOB_SERVICE.createJob(any(), any())).thenThrow(new UnexpectedException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Job jobForJobCreationRequest = generateApiJob();
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), res.getStatus());
  }

  @Test
  public void testCreateJobBadInputs() throws UnexpectedException {
    when(MOCK_JOB_SERVICE.createJob(any(), any())).thenThrow(new UnexpectedException());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Job jobForJobCreationRequest = generateApiJob();
    jobForJobCreationRequest.setLocation(null);

    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(UNPROCESSABLE_ENTRY_STATUS_CODE, res.getStatus());
  }

  @Test
  public void testDescriptionOptionalForCreateJobInputs() throws UnexpectedException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    Job jobForJobCreationRequest = generateApiJob();
    jobForJobCreationRequest.setDescription(null);

    insertJob(jobForJobCreationRequest);
    verify(MOCK_JOB_SERVICE, times(1)).createJob(any(), any());
  }

  @Test
  public void testGetJobNoSuchJob() throws UnexpectedException {
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(true);
    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    when(MOCK_JOB_SERVICE.getJob(any(), any())).thenReturn(Optional.empty());

    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, "nosuchjob");
    Response res = resources.client().target(path).request(MediaType.APPLICATION_JSON).get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }


  @Test
  public void testJobRunCreationWithInvalidNamespace() throws UnexpectedException {
    Job jobForJobCreationRequest = generateApiJob();

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());
    when(MOCK_NAMESPACE_SERVICE.exists(any())).thenReturn(false);
    Response res = insertJob(jobForJobCreationRequest);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  private Response insertJob(Job job) {
    CreateJobRequest createJobRequest =
        new CreateJobRequest(
            job.getLocation(),
            job.getDescription(),
            job.getInputDataSetUrns(),
            job.getOutputDataSetUrns());
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, job.getName());
    return resources
        .client()
        .target(path)
        .request(MediaType.APPLICATION_JSON)
        .put(entity(createJobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }

  private Response insertJobRun(JobRun jobRun) {
    CreateJobRunRequest createJobRequest =
            new CreateJobRunRequest(
                    jobRun.getNominalStartTime(),
                    jobRun.getNominalEndTime(),
                    jobRun.getRunArgs());
    String path = format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, "somejob");
    return resources
            .client()
            .target(path)
            .request(MediaType.APPLICATION_JSON)
            .put(entity(createJobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
  }

  Job generateApiJob() {
    String jobName = "myJob" + System.currentTimeMillis();
    final String location = "someLocation";
    final String description = "someDescription";
    final List<String> inputList = Collections.singletonList("input1");
    final List<String> outputList = Collections.singletonList("output1");
    return new Job(jobName, null, inputList, outputList, location, description);
  }
}
