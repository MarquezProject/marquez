package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import org.junit.Test;

public class PingResourceTest {
  @Test
  public void testPing200() {
    final PingResource pingResource = new PingResource();
    final Response response = pingResource.ping();
    assertEquals(OK, response.getStatusInfo());
    assertEquals("pong", (String) response.getEntity());
  }
}
