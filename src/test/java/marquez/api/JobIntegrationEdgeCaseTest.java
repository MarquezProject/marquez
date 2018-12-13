package marquez.api;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.entity;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.ResourceExceptionMapper;
import marquez.core.models.Generator;
import marquez.core.services.JobService;
import marquez.core.services.NamespaceService;
import marquez.dao.JobDAO;
import marquez.dao.JobRunDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.RunArgsDAO;
import marquez.resources.JobResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobIntegrationEdgeCaseTest {

  private static final JobService JOB_SERVICE =
      new JobService(
          mock(JobDAO.class),
          mock(JobVersionDAO.class),
          mock(JobRunDAO.class),
          mock(RunArgsDAO.class));
  private static final NamespaceService MOCK_NAMESPACE_SERVICE = mock(NamespaceService.class);
  private static final JobResource JOB_RESOURCE =
      new JobResource(MOCK_NAMESPACE_SERVICE, JOB_SERVICE);
  private static final String NAMESPACE_NAME = "someNamespace";

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(JOB_RESOURCE)
          .addProvider(ResourceExceptionMapper.class)
          .build();

  @Before
  public void clearMocks() {
    reset(MOCK_NAMESPACE_SERVICE);
  }

  @Test
  public void testJobCreationWithValidNamespace() throws UnexpectedException {
    Job jobForJobCreationRequest = generateApiJob();
    CreateJobRequest createJobRequest = generateCreateJobRequest(jobForJobCreationRequest);

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.of(Generator.genNamespace()));

    String path =
        format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, jobForJobCreationRequest.getName());
    Response res =
        resources
            .client()
            .target(path)
            .request(MediaType.APPLICATION_JSON)
            .put(entity(createJobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
  }

  @Test
  public void testJobCreationWithInvalidNamespace() throws UnexpectedException {
    Job jobForJobCreationRequest = generateApiJob();
    CreateJobRequest createJobRequest =
        new CreateJobRequest(
            jobForJobCreationRequest.getLocation(),
            jobForJobCreationRequest.getDescription(),
            jobForJobCreationRequest.getInputDataSetUrns(),
            jobForJobCreationRequest.getOutputDataSetUrns());

    when(MOCK_NAMESPACE_SERVICE.get(any())).thenReturn(Optional.empty());

    String path =
        format("/api/v1/namespaces/%s/jobs/%s", NAMESPACE_NAME, jobForJobCreationRequest.getName());
    Response res =
        resources
            .client()
            .target(path)
            .request(MediaType.APPLICATION_JSON)
            .put(entity(createJobRequest, javax.ws.rs.core.MediaType.APPLICATION_JSON));
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
  }

  CreateJobRequest generateCreateJobRequest(Job job) {
    return new CreateJobRequest(
        job.getLocation(),
        job.getDescription(),
        job.getInputDataSetUrns(),
        job.getOutputDataSetUrns());
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
