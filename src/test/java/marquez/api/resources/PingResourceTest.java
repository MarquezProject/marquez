package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import org.junit.Test;

public class PingResourceTest {
  @Test
  public void testPing200() {
    final String expected = "pong";

    final PingResource pingResource = new PingResource();
    final Response response = pingResource.ping();
    assertEquals(OK, response.getStatusInfo());

    final String actual = (String) response.getEntity();
    assertEquals(expected, actual);
  }
}
