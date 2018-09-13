package marquez;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import javax.ws.rs.core.Response;
import marquez.api.entities.CreateJobRunDefinitionRequest;
import org.junit.ClassRule;
import org.junit.Test;

public class MarquezAppIntegrationTest {
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

    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/job_run_definition")
            .request()
            .post(entity(req, APPLICATION_JSON));

    assertEquals(res.getStatus(), 201);
  }

  @Test
  public void createJobRunDefinition_Dedup_OK() {
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
    assertEquals(res2.getStatus(), 200);
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
}
