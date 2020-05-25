package marquez;

import static marquez.Generator.newTimestamp;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URL;
import java.util.Optional;

import marquez.client.MarquezClient;
import marquez.client.Utils;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.JdbiRuleInit;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class MarquezAppIntegrationTest {
  private static final String CONFIG_FILE = "config.test.yml";
  private static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

  private static final MarquezDb DB = MarquezDb.create();

  static {
    DB.start();
  }

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class,
          CONFIG_FILE_PATH,
          ConfigOverride.config("db.url", DB.getJdbcUrl()),
          ConfigOverride.config("db.user", DB.getUsername()),
          ConfigOverride.config("db.password", DB.getPassword()));

  // NAMESPACE
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final OwnerName OWNER_NAME = newOwnerName();
  private static final String NAMESPACE_DESCRIPTION = newDescription();

  private static final String BASE_API_PATH = "/api/v1";
  private final URL BASE_URL =
      Utils.toUrl("http://localhost:" + APP.getLocalPort() + BASE_API_PATH);
  private final MarquezClient CLIENT = new MarquezClient(BASE_URL);

  @Test
  public void testApp_createNamespace() {
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder()
            .ownerName(OWNER_NAME.getValue())
            .description(NAMESPACE_DESCRIPTION)
            .build();

    final Namespace namespace = CLIENT.createNamespace(NAMESPACE_NAME.getValue(), namespaceMeta);
    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME.getValue());
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME.getValue());
    assertThat(namespace.getDescription()).isEqualTo(Optional.of(NAMESPACE_DESCRIPTION));
  }
}
