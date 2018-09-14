package marquez;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.URI;

import static org.junit.Assert.assertEquals;

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
    assertEquals(200, res.getStatus());
    assertEquals("pong", res.readEntity(String.class));
  }
}
