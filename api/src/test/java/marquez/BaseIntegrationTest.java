package marquez;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;

public abstract class BaseIntegrationTest {
  protected static final String CONFIG_FILE = "config.test.yml";
  protected static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);
  protected static final PostgresContainer POSTGRES = createMarquezPostgres();
  protected final HttpClient http2 = HttpClient.newBuilder().version(Version.HTTP_2).build();

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

  protected CompletableFuture<HttpResponse<String>> sendLineage(String body) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/lineage"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(body))
            .build();

    return http2.sendAsync(request, BodyHandlers.ofString());
  }
}
