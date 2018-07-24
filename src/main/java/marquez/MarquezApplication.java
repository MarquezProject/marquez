package marquez;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import marquez.db.JobDAO;
import marquez.resources.JobResource;
import org.skife.jdbi.v2.DBI;

public class MarquezApplication extends Application<MarquezConfiguration> {

  @Override
  public void run(final MarquezConfiguration configuration, final Environment environment) {
    final DBIFactory factory = new DBIFactory();
    final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "h2");
    final JobDAO jobDAO = jdbi.onDemand(JobDAO.class);
    final JobResource jobResource = new JobResource(jobDAO);
    environment.jersey().register(jobResource);
  }

  @Override
  public String getName() {
    return "MarquezServer";
  }

  @Override
  public void initialize(Bootstrap<MarquezConfiguration> bootstrap) {
    bootstrap.addBundle(
        new MigrationsBundle<MarquezConfiguration>() {
          @Override
          public DataSourceFactory getDataSourceFactory(MarquezConfiguration configuration) {
            return configuration.getDataSourceFactory();
          }
        });
  }

  public static void main(final String[] args) throws Exception {
    final MarquezApplication application = new MarquezApplication();
    application.run(args);
  }
}
