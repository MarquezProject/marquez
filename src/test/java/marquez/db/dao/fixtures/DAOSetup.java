package marquez.db.dao.fixtures;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import marquez.MarquezApp;
import marquez.MarquezConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/** Init code for testing DAOs against postgres */
public class DAOSetup extends DropwizardAppRule<MarquezConfig> {
  public static final String POSTGRES_TEST_CONFIG_FILE_NAME = "config.test.yml";
  public static final String POSTGRES_FULL_TEST_CONFIG_FILE_PATH =
      "src/test/resources/" + POSTGRES_TEST_CONFIG_FILE_NAME;

  private Jdbi jdbi;

  public DAOSetup() {
    super(MarquezApp.class, POSTGRES_FULL_TEST_CONFIG_FILE_PATH);
  }

  @Override
  protected void before() {
    super.before();
    // init DB
    JdbiFactory factory = new JdbiFactory();
    MarquezConfig config = this.getConfiguration();
    DataSourceFactory dataSourceFactory = config.getDataSourceFactory();
    ManagedDataSource dataSource =
        dataSourceFactory.build(this.getEnvironment().metrics(), "postgres");
    jdbi =
        factory
            .build(this.getEnvironment(), dataSourceFactory, dataSource, "postgres")
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());
  }

  public Jdbi getJDBI() {
    return jdbi;
  }

  /** @see Jdbi#onDemand(Class) */
  public <T> T onDemand(Class<T> classT) {
    return jdbi.onDemand(classT);
  }
}
