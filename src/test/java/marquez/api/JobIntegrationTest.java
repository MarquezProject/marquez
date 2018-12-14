package marquez.api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.JobRunBaseTest;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore("TODO: Re-enable once namespace service implementation is checked in.")
public class JobIntegrationTest extends JobRunBaseTest {
  private static Logger LOG = LoggerFactory.getLogger(JobIntegrationTest.class);

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
}
