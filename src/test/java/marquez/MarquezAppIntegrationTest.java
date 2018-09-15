package marquez;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.core.Response;
import marquez.api.entities.CreateJobRunDefinitionRequest;
import marquez.api.entities.CreateJobRunDefinitionResponse;
import org.junit.ClassRule;
import org.junit.Test;

public class MarquezAppIntegrationTest {
  protected static final ObjectMapper mapper = Jackson.newObjectMapper();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class, ResourceHelpers.resourceFilePath("config.test.yml"));

  @Test
  public void runAppTest() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/ping")
            .request()
            .get();
    assertEquals(res.getStatus(), 200);
    assertEquals(res.readEntity(String.class), "pong");
  }

  @Test
  public void createJobRunDefinition_OK() {
    CreateJobRunDefinitionRequest req =
        new CreateJobRunDefinitionRequest("job name", "{}", 0, 0, "http://foo.bar", "my owner");

    final Response res1 =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    final Response res2 =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    assertEquals(res1.readEntity(String.class), res2.readEntity(String.class));
    assertEquals(201, res1.getStatus());
    assertEquals(200, res2.getStatus());
  }

  @Test
  public void createJobRunDefinition_BadJson_Err() {
    CreateJobRunDefinitionRequest req =
        new CreateJobRunDefinitionRequest(
            "job name", "BAD_JSON", 0, 0, "http://foo.bar", "my owner");

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    assertEquals(400, res.getStatus());
  }

  @Test
  public void createJobRunDefinition_BadUri_Err() {
    CreateJobRunDefinitionRequest req =
        new CreateJobRunDefinitionRequest("job name", "{}", 0, 0, "BAD URI", "my owner");

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    assertEquals(400, res.getStatus());
  }

  @Test
  public void readJobRunDefinition_OK() {
    CreateJobRunDefinitionRequest req =
        new CreateJobRunDefinitionRequest("job name", "{}", 0, 0, "http://foo.bar", "my owner");

    final Response createRes =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    UUID jobRunDefId;
    try {
      CreateJobRunDefinitionResponse createResJrd =
          mapper.readValue(
              createRes.readEntity(String.class), CreateJobRunDefinitionResponse.class);
      jobRunDefId = createResJrd.getExternalGuid();
      final Response readRes =
          APP.client()
              .target(URI.create("http://localhost:" + APP.getLocalPort()))
              .path("/job_run_definition/" + jobRunDefId.toString())
              .request()
              .get();

      assertEquals(Response.Status.OK, readRes.getStatus());

    } catch (IOException e) {
      fail("failed to parse response.");
    }
  }
}
