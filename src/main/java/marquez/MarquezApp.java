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
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.DatasetResource;
import marquez.api.JobResource;
import marquez.api.NamespaceResource;
import marquez.api.SourceResource;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.NamespaceOwnershipDao;
import marquez.db.OwnerDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.SourceDao;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.SourceService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@Slf4j
public final class MarquezApp extends Application<MarquezConfig> {
  private static final String APP_NAME = "MarquezApp";
  private static final String POSTGRES_DB = "postgresql";
  private static final boolean ERROR_ON_UNDEFINED = false;

  // Monitoring
  private static final String PROMETHEUS = "prometheus";
  private static final String PROMETHEUS_ENDPOINT = "/metrics";

  public static void main(String[] args) throws Exception {
    new MarquezApp().run(args);
  }

  @Override
  public String getName() {
    return APP_NAME;
  }

  @Override
  public void initialize(@NonNull Bootstrap<MarquezConfig> bootstrap) {
    // Enable metric collection for prometheus.
    CollectorRegistry.defaultRegistry.register(
        new DropwizardExports(bootstrap.getMetricRegistry()));
    DefaultExports.initialize(); // Add metrics for CPU, JVM memory, etc.

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
  public void run(@NonNull MarquezConfig config, @NonNull Environment env) throws MarquezException {
    log.info("Running startup actions...");
    migrateDbOrError(config);
    registerResources(config, env);

    // Expose metrics for monitoring.
    env.servlets().addServlet(PROMETHEUS, new MetricsServlet()).addMapping(PROMETHEUS_ENDPOINT);
  }

  private void migrateDbOrError(@NonNull MarquezConfig config) {
    final Flyway flyway = new Flyway();
    final DataSourceFactory db = config.getDataSourceFactory();
    flyway.setDataSource(db.getUrl(), db.getUser(), db.getPassword());
    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before app termination.
    try {
      log.info("Migrating database...");
      flyway.migrate();
      log.info("Successfully migrated database.");
    } catch (FlywayException errorOnDbMigrate) {
      log.error("Failed to apply migration to database.", errorOnDbMigrate);
      try {
        log.info("Repairing failed database migration...");
        flyway.repair();
        log.info("Successfully repaired database.");
      } catch (FlywayException errorOnDbRepair) {
        log.error("Failed to apply repair to database.", errorOnDbRepair);
      }

      log.info("Stopping app...");
      // The throwable is not propagating up the stack.
      onFatalError(); // Signal app termination.
    }
  }

  private void registerResources(@NonNull MarquezConfig config, @NonNull Environment env)
      throws MarquezException {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(env, config.getDataSourceFactory(), POSTGRES_DB)
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());
    jdbi.setSqlLogger(new InstrumentedSqlLogger(env.metrics()));

    final NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    final OwnerDao ownerDao = jdbi.onDemand(OwnerDao.class);
    final NamespaceOwnershipDao namespaceOwnershipDao = jdbi.onDemand(NamespaceOwnershipDao.class);
    final SourceDao sourceDao = jdbi.onDemand(SourceDao.class);
    final DatasetDao datasetDao = jdbi.onDemand(DatasetDao.class);
    final DatasetFieldDao datasetFieldDao = jdbi.onDemand(DatasetFieldDao.class);
    final DatasetVersionDao datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    final JobDao jobDao = jdbi.onDemand(JobDao.class);
    final JobVersionDao jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    final JobContextDao jobContextDao = jdbi.onDemand(JobContextDao.class);
    final RunDao runDao = jdbi.onDemand(RunDao.class);
    final RunArgsDao runArgsDao = jdbi.onDemand(RunArgsDao.class);
    final RunStateDao runStateDao = jdbi.onDemand(RunStateDao.class);

    final NamespaceService namespaceService =
        new NamespaceService(namespaceDao, ownerDao, namespaceOwnershipDao);
    final SourceService sourceService = new SourceService(sourceDao);
    final DatasetService datasetService =
        new DatasetService(namespaceDao, sourceDao, datasetDao, datasetFieldDao, datasetVersionDao);
    final JobService jobService =
        new JobService(
            namespaceDao,
            datasetDao,
            jobDao,
            jobVersionDao,
            jobContextDao,
            runDao,
            runArgsDao,
            runStateDao);

    env.jersey().register(new NamespaceResource(namespaceService));
    env.jersey().register(new SourceResource(sourceService));
    env.jersey().register(new DatasetResource(namespaceService, datasetService, jobService));
    env.jersey().register(new JobResource(namespaceService, jobService));

    env.jersey().register(new MarquezServiceExceptionMapper());
  }
}
