package marquez;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
import java.net.URL;

@Category(IntegrationTests.class)
public class MarquezAppIntegrationTest {
    private static final String CONFIG_FILE = "config.test.yml";
    private static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

    private static final MarquezDb DB = MarquezDb.create();

    static {
        DB.start();
    }

    @ClassRule
    public static final DropwizardAppRule<MarquezConfig> APP =
            new DropwizardAppRule<>(
                    MarquezApp.class,
                    CONFIG_FILE_PATH,
                    ConfigOverride.config("db.url", DB.getJdbcUrl()),
                    ConfigOverride.config("db.user", DB.getUsername()),
                    ConfigOverride.config("db.password", DB.getPassword()));

    private static final String BASE_API_PATH = "/api/v1";
    private static final URL BASE_URL = Utils.toUrl("http://localhost:" + APP.getLocalPort() + BASE_API_PATH);

    private static final MarquezClient client = new MarquezClient(BASE_URL);

    @BeforeClass
    public static void setUpOnce() {


    }

    @Test
    public void testApp_createNamespace() {

    }
}
