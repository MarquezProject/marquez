package marquez;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.net.URI;
import javax.ws.rs.core.Response;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

public class MarquezAppIntegrationTest {
  protected static final ObjectMapper mapper = Jackson.newObjectMapper();

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  @After
  public void teardown() {
    APP.getJDBI()
        .useHandle(
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
    final Response response =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getLocalPort()))
            .path("/ping")
            .request()
            .get();
    assertEquals(OK, response.getStatus());
    assertEquals("pong", response.readEntity(String.class));
  }
}
