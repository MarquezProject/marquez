package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import org.junit.Test;

public class HealthResourceTest {
  @Test
  public void testHealth200() {
    final String expected = "OK";

    final HealthResource healthResource = new HealthResource();
    final Response response = healthResource.checkHealth();
    assertEquals(OK, response.getStatusInfo());

    final String actual = (String) response.getEntity();
    assertEquals(expected, actual);
  }
}
