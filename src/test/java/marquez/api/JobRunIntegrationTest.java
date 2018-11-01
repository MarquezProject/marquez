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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunIntegrationTest extends JobRunBaseTest {
  private static Logger LOG = LoggerFactory.getLogger(JobRunIntegrationTest.class);

  // @Test
  public void testJobRunCreationEndToEnd() throws JsonProcessingException {
    Entity createJobRunRequestEntity =
        Entity.json(
            MAPPER.writeValueAsString(
                new CreateJobRunRequest(UUID.fromString(TEST_JOB_RUN_DEFINITION_GUID))));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_runs")
            .request(MediaType.APPLICATION_JSON)
            .post(createJobRunRequestEntity);
    assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
    CreateJobRunResponse responseBody = res.readEntity(CreateJobRunResponse.class);
    UUID returnedId = responseBody.getExternalGuid();
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

  // @Test
  public void testJobRunGetterEndToEnd() {
    GetJobRunResponse responseBody = getJobRunApiResponse(NEW_JOB_RUN.getGuid());

    assertEquals(JobRunState.State.NEW, JobRunState.State.valueOf(responseBody.getState()));
    assertNull(responseBody.getStartedAt());
    assertNull(responseBody.getEndedAt());
  }

  // @Test
  public void testJobRunAfterUpdateEndToEnd() throws JsonProcessingException {
    Entity jobRunUpdateRequestEntity =
        Entity.json(MAPPER.writeValueAsString(new UpdateJobRunRequest("RUNNING")));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/job_runs/%s", NEW_JOB_RUN.getGuid()))
            .request(MediaType.APPLICATION_JSON)
            .put(jobRunUpdateRequestEntity);

    assertEquals(Response.Status.ACCEPTED.getStatusCode(), res.getStatus());

    GetJobRunResponse responseBody = getJobRunApiResponse(NEW_JOB_RUN.getGuid());

    assertEquals(JobRunState.State.RUNNING, JobRunState.State.valueOf(responseBody.getState()));
    assertNotNull(responseBody.getStartedAt());
    assertNull(responseBody.getEndedAt());
  }

  // @Test
  public void testJobRunAfterForbiddenUpdateEndToEnd() throws JsonProcessingException {

    Entity jobRunUpdateRequestEntity =
        Entity.json(MAPPER.writeValueAsString(new UpdateJobRunRequest("FAILED")));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/job_runs/%s", NEW_JOB_RUN.getGuid()))
            .request(MediaType.APPLICATION_JSON)
            .put(jobRunUpdateRequestEntity);

    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), res.getStatus());
  }

  // @Test
  public void testJobRunAfterInvalidUpdateEndToEnd() throws JsonProcessingException {
    Entity jobRunRequestJsonAsEntity =
        Entity.json(MAPPER.writeValueAsString(new UpdateJobRunRequest("NO_SUCH_STATE")));
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/job_runs/%s", NEW_JOB_RUN.getGuid()))
            .request(MediaType.APPLICATION_JSON)
            .put(jobRunRequestJsonAsEntity);

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), res.getStatus());
  }

  private GetJobRunResponse getJobRunApiResponse(UUID jobRunGuid) {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path(format("/job_runs/%s", jobRunGuid))
            .request(MediaType.APPLICATION_JSON)
            .get();
    assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    return res.readEntity(GetJobRunResponse.class);
  }
}
