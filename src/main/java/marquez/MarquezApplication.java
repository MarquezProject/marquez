package marquez;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import marquez.db.dao.DatasetDAO;
import marquez.db.dao.JobDAO;
import marquez.db.dao.OwnerDAO;
import marquez.resources.DatasetResource;
import marquez.resources.HealthResource;
import marquez.resources.JobResource;
import marquez.resources.OwnerResource;
import marquez.resources.PingResource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class MarquezApplication extends Application<MarquezConfiguration> {
  private static final String APP_NAME = "MarquezApp";
  private static final String POSTGRESQL_DB = "postgresql";

  public static void main(String[] args) throws Exception {
    new MarquezApplication().run(args);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public void initialize(Bootstrap<MarquezConfiguration> bootstrap) {
    bootstrap.addBundle(
        new FlywayBundle<MarquezConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(MarquezConfiguration config) {
            return config.getDataSourceFactory();
          }

          @Override
          public FlywayFactory getFlywayFactory(MarquezConfiguration config) {
            return config.getFlywayFactory();
          }
        });
  }

  @Override
  public void run(MarquezConfiguration config, Environment env) {
    migrateDb(config, env);
    registerResources(config, env);
  }

  private void migrateDb(MarquezConfiguration config, Environment env) throws FlywayException {
    final Flyway flyway = new Flyway();
    final DataSourceFactory database = config.getDataSourceFactory();
    flyway.setDataSource(database.getUrl(), database.getUser(), database.getPassword());
    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before rethrowing the exception.
    try {
      flyway.migrate();
    } catch (FlywayException e) {
      flyway.repair();
      throw e;
    }
  }

  private void registerResources(MarquezConfiguration config, Environment env) {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(env, config.getDataSourceFactory(), POSTGRESQL_DB)
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());

    env.jersey().register(new PingResource());
    env.jersey().register(new HealthResource());

    final OwnerDAO ownerDAO = jdbi.onDemand(OwnerDAO.class);
    env.jersey().register(new OwnerResource(ownerDAO));

    final JobDAO jobDAO = jdbi.onDemand(JobDAO.class);
    env.jersey().register(new JobResource(jobDAO));

    final DatasetDAO datasetDAO = jdbi.onDemand(DatasetDAO.class);
    env.jersey().register(new DatasetResource(datasetDAO));
  }
}
