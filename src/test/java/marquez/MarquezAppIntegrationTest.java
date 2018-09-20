package marquez;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.core.Response;
import marquez.api.entities.CreateJobRunDefinitionRequest;
import marquez.api.entities.CreateJobRunDefinitionResponse;
import marquez.api.entities.GetJobRunDefinitionResponse;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class MarquezAppIntegrationTest {
  protected static final ObjectMapper mapper = Jackson.newObjectMapper();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class, ResourceHelpers.resourceFilePath("config.test.yml"));

  protected static Jdbi jdbi;

  @BeforeClass
  public static void makeJdbi() {
    JdbiFactory factory = new JdbiFactory();
    MarquezConfig config = APP.getConfiguration();
    DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
    ManagedDataSource dataSource = dataSourceFactory.build(APP.getEnvironment().metrics(), "h2");
    jdbi =
        factory
            .build(APP.getEnvironment(), dataSourceFactory, dataSource, "h2")
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new H2DatabasePlugin());
  }

  @After
  public void teardown() {
    jdbi.useHandle(
        handle -> {
          handle.execute("DELETE FROM job_run_definitions;");
          handle.execute("DELETE FROM job_versions;");
          handle.execute("DELETE FROM jobs;");
          handle.execute("DELETE FROM owners;");
          handle.execute("DELETE FROM ownerships;");
        });
  }

  @Test
  public void runAppTest() {
    final Response res =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/ping")
            .request()
            .get();
    assertEquals(200, res.getStatus());
    assertEquals("pong", res.readEntity(String.class));
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
    assertEquals(200, res1.getStatus());
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
        new CreateJobRunDefinitionRequest(
            "job name", "{\"test\":\"readJrd\"}", 5, 10, "http://foo2.bar2", "my owner");
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
      jobRunDefId = createResJrd.getGuid();
      final Response readRes =
          APP.client()
              .target(URI.create("http://localhost:" + APP.getLocalPort()))
              .path("/job_run_definition/" + jobRunDefId.toString())
              .request()
              .get();

      GetJobRunDefinitionResponse res =
          mapper.readValue(readRes.readEntity(String.class), GetJobRunDefinitionResponse.class);
      assertEquals(Response.Status.OK.getStatusCode(), readRes.getStatus());
      assertNotNull(res.getGuid());
      assertEquals(req.getName(), res.getName());
      assertEquals(req.getOwnerName(), res.getOwnerName());
      assertEquals(req.getRunArgsJson(), res.getRunArgsJson());
      assertEquals(req.getURI(), res.getURI());
      assertEquals(req.getNominalStartTime(), res.getNominalTimeStart());

    } catch (IOException e) {
      fail("failed to parse GET /job_run_definition/{id} response");
    }
  }
}
