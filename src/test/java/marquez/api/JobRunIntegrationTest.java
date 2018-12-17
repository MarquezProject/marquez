package marquez.api;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.JobRunBaseTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunIntegrationTest extends JobRunBaseTest {
  private static Logger LOG = LoggerFactory.getLogger(JobRunIntegrationTest.class);

  @Test
  public void testJobRunCreationEndToEnd() throws JsonProcessingException {
    Entity createJobRunRequestEntity =
        Entity.json(
            MAPPER.writeValueAsString(new CreateJobRunRequest(null, null, "{'key': 'value'}")));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/api/v1/namespaces/" + NAMESPACE_NAME + "/jobs/" + TEST_JOB_NAME + "/runs")
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
    JobRun responseBody = getJobRunApiResponse(newJobRun.getGuid());

    assertEquals(marquez.core.models.JobRunState.State.NEW.name(), responseBody.getCurrentState());
    assertNull(responseBody.getNominalStartTime());
    assertNull(responseBody.getNominalEndTime());
  }

  @Test
  public void testJobRunAfterUpdateEndToEnd() throws JsonProcessingException {

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/api/v1/jobs/runs/%s/complete", newJobRun.getGuid()))
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
