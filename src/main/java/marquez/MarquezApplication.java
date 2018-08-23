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
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public final class MarquezApplication extends Application<MarquezConfiguration> {
  private static final String APP_NAME = "MarquezApp";
  private static final String POSTGRESQL_DB = "postgresql";

  public static void main(final String[] args) throws Exception {
    new MarquezApplication().run(args);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public void initialize(final Bootstrap<MarquezConfiguration> bootstrap) {
    bootstrap.addBundle(
        new FlywayBundle<MarquezConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(final MarquezConfiguration config) {
            return config.getDataSourceFactory();
          }

          @Override
          public FlywayFactory getFlywayFactory(final MarquezConfiguration config) {
            return config.getFlywayFactory();
          }
        });
  }

  @Override
  public void run(final MarquezConfiguration config, final Environment env) {
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
