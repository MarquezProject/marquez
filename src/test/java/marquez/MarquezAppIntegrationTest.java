package marquez;

import static org.junit.Assert.assertEquals;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import javax.ws.rs.core.Response;
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
}
