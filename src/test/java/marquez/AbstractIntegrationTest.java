package marquez;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URL;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;

public abstract class AbstractIntegrationTest {
  protected static final String CONFIG_FILE = "config.test.yml";
  protected static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);
  protected static final PostgresContainer POSTGRES = createMarquezPostgres();

  static {
    POSTGRES.start();
  }

  protected final URL baseUrl = Utils.toUrl("http://localhost:" + APP.getLocalPort());
  protected final MarquezClient client = MarquezClient.builder().baseUrl(baseUrl).build();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class,
          CONFIG_FILE_PATH,
          ConfigOverride.config("db.url", POSTGRES.getJdbcUrl()),
          ConfigOverride.config("db.user", POSTGRES.getUsername()),
          ConfigOverride.config("db.password", POSTGRES.getPassword()));

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  protected static PostgresContainer createMarquezPostgres() {
    return PostgresContainer.create("marquez");
  }
}
