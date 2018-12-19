package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import org.junit.Test;

public class HealthResourceTest {
  @Test
  public void testHealth200() {
    final HealthResource healthResource = new HealthResource();
    final Response response = healthResource.checkHealth();
    assertEquals(OK, response.getStatusInfo());
    assertEquals("OK", (String) response.getEntity());
  }
}
