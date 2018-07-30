package marquez;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import marquez.db.DbConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class MarquezApplication extends Application<MarquezConfiguration> {
  private static final String APP_NAME = "MarquezApp";

  public static void main(final String[] args) throws Exception {
    new MarquezApplication().run(args);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public void initialize(Bootstrap<MarquezConfiguration> bootstrap) {}

  @Override
  public void run(final MarquezConfiguration config, final Environment env) {
    final DbConfiguration dbConfig = config.getDbConfiguration();
    final String connectionURL = String.format(
        "jdbc:postgresql://%s:%d/%s", dbConfig.getHost(), dbConfig.getPort(), dbConfig.getName());
    final Jdbi jdbi =
        Jdbi.create(connectionURL, dbConfig.getUser(), dbConfig.getPassword())
            .installPlugin(new PostgresPlugin())
            .installPlugin(new SqlObjectPlugin());
  }
}
