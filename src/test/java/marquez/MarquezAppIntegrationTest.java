package marquez;

import static org.junit.Assert.assertEquals;

import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import javax.ws.rs.core.Response;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MarquezAppIntegrationTest {
  @ClassRule public static final JdbiRule DB = JdbiRule.embeddedPostgres();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(MarquezApp.class, "config.yml");

  @ClassRule public static final RuleChain chain = RuleChain.outerRule(DB).around(APP);

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
