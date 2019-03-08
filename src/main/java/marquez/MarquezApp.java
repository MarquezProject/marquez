/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez;

import com.codahale.metrics.jdbi3.InstrumentedSqlLogger;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.api.resources.DatasetResource;
import marquez.api.resources.HealthResource;
import marquez.api.resources.JobResource;
import marquez.api.resources.NamespaceResource;
import marquez.api.resources.PingResource;
import marquez.db.DatasetDao;
import marquez.db.JobDao;
import marquez.db.JobRunArgsDao;
import marquez.db.JobRunDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@Slf4j
public class MarquezApp extends Application<MarquezConfig> {
  private static final String APP_NAME = "MarquezApp";
  private static final String POSTGRES_DB = "postgresql";
  private static final boolean ERROR_ON_UNDEFINED = false;

  public static void main(String[] args) throws Exception {
    new MarquezApp().run(args);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public void initialize(@NonNull Bootstrap<MarquezConfig> bootstrap) {
    // Enable variable substitution with environment variables.
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor(ERROR_ON_UNDEFINED)));

    // Enable Flyway for database migrations.
    bootstrap.addBundle(
        new FlywayBundle<MarquezConfig>() {
          @Override
          public DataSourceFactory getDataSourceFactory(@NonNull MarquezConfig config) {
            return config.getDataSourceFactory();
          }

          @Override
          public FlywayFactory getFlywayFactory(@NonNull MarquezConfig config) {
            return config.getFlywayFactory();
          }
        });
  }

  @Override
  public void run(@NonNull MarquezConfig config, @NonNull Environment env) {
    migrateDbOrError(config);
    registerResources(config, env);
  }

  private void migrateDbOrError(@NonNull MarquezConfig config) {
    final Flyway flyway = new Flyway();
    final DataSourceFactory database = config.getDataSourceFactory();
    flyway.setDataSource(database.getUrl(), database.getUser(), database.getPassword());
    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before terminating.
    try {
      flyway.migrate();
    } catch (FlywayException e) {
      log.error("Failed to apply migration to database.", e.getMessage());
      log.info("Repairing failed database migration...", e.getMessage());
      flyway.repair();

      log.info("Successfully repaired database, stopping app...");
      // The throwable is not propagating up the stack.
      onFatalError(); // Signal app termination.
    }
  }

  private void registerResources(@NonNull MarquezConfig config, @NonNull Environment env) {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(env, config.getDataSourceFactory(), POSTGRES_DB)
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());
    jdbi.setSqlLogger(new InstrumentedSqlLogger(env.metrics()));

    final NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    final JobDao jobDao = jdbi.onDemand(JobDao.class);
    final JobVersionDao jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    final JobRunDao jobRunDao = jdbi.onDemand(JobRunDao.class);
    final JobRunArgsDao jobRunArgsDao = jdbi.onDemand(JobRunArgsDao.class);
    final DatasetDao datasetDao = jdbi.onDemand(DatasetDao.class);

    final NamespaceService namespaceService = new NamespaceService(namespaceDao);
    final JobService jobService = new JobService(jobDao, jobVersionDao, jobRunDao, jobRunArgsDao);

    env.jersey().register(new PingResource());
    env.jersey().register(new HealthResource());
    env.jersey().register(new NamespaceResource(namespaceService));
    env.jersey().register(new JobResource(namespaceService, jobService));
    env.jersey().register(new DatasetResource(namespaceService, new DatasetService(datasetDao)));

    env.jersey().register(new MarquezServiceExceptionMapper());
  }
}
