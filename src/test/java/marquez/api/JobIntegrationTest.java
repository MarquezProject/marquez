package marquez.api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.JobRunBaseTest;
import marquez.core.models.Namespace;
import marquez.dao.NamespaceDAO;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore(
    "TODO: Job Run Definition was removed, disabling tests so they can be updated for new endpoints")
public class JobIntegrationTest extends JobRunBaseTest {
  private static Logger LOG = LoggerFactory.getLogger(JobIntegrationTest.class);

  static NamespaceDAO namespaceDAO = APP.onDemand(NamespaceDAO.class);

  static final String NAMESPACE_NAME = "nsName";
  static final String NAMESPACE_OWNER = "nsOwner";
  static final String NAMESPACE_DESC = "nsDesc";

  @BeforeClass
  public static void setUpNamespace() {
    namespaceDAO.insert(
        new Namespace(UUID.randomUUID(), NAMESPACE_NAME, NAMESPACE_OWNER, NAMESPACE_DESC));
  }

  @Test
  public void testJobRunCreationEndToEnd() {
    Job jobForJobCreationRequest = generateApiJob();

    Response res = createJobOnNamespace(NAMESPACE_NAME, jobForJobCreationRequest);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());

    Job returnedJob = res.readEntity(Job.class);
    assertEquals(returnedJob.getName(), jobForJobCreationRequest.getName());
    assertEquals(returnedJob.getDescription(), jobForJobCreationRequest.getDescription());
    assertEquals(returnedJob.getLocation(), jobForJobCreationRequest.getLocation());

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
