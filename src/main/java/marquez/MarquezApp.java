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

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.DatasetResource;
import marquez.api.JobResource;
import marquez.api.NamespaceResource;
import marquez.api.SourceResource;
import marquez.api.TagResource;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.FlywayFactory;
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
import marquez.db.TagDao;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.SourceService;
import marquez.service.TagService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@Slf4j
public final class MarquezApp extends Application<MarquezConfig> {
  private static final String APP_NAME = "MarquezApp";
  private static final String DB_SOURCE_NAME = APP_NAME + "-source";
  private static final String DB_POSTGRES = "postgresql";
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
  }

  @Override
  public void run(@NonNull MarquezConfig config, @NonNull Environment env) throws MarquezException {
    final DataSourceFactory sourceFactory = config.getDataSourceFactory();
    final DataSource source = sourceFactory.build(env.metrics(), DB_SOURCE_NAME);

    log.info("Running startup actions...");
    if (config.isMigrateOnStartup()) {
      migrateDbOrError(config, source);
    }
    registerResources(config, env, source);
    registerServlets(env);
  }

  private void migrateDbOrError(@NonNull MarquezConfig config, @NonNull DataSource source) {
    final FlywayFactory flywayFactory = config.getFlywayFactory();
    final Flyway flyway = flywayFactory.build(source);

    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before app termination.
    try {
      log.info("Migrating database...");
      final int migrations = flyway.migrate();
      log.info("Successfully applied '{}' migrations to database.", migrations);
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

  private void registerResources(
      @NonNull MarquezConfig config, @NonNull Environment env, @NonNull DataSource source)
      throws MarquezException {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(env, config.getDataSourceFactory(), (ManagedDataSource) source, DB_POSTGRES)
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());

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
    final TagDao tagDao = jdbi.onDemand(TagDao.class);

    final NamespaceService namespaceService =
        new NamespaceService(namespaceDao, ownerDao, namespaceOwnershipDao);
    final SourceService sourceService = new SourceService(sourceDao);
    final DatasetService datasetService =
        new DatasetService(
            namespaceDao, sourceDao, datasetDao, datasetFieldDao, datasetVersionDao, tagDao);
    final JobService jobService =
        new JobService(
            namespaceDao,
            datasetDao,
            datasetVersionDao,
            jobDao,
            jobVersionDao,
            jobContextDao,
            runDao,
            runArgsDao,
            runStateDao);
    final TagService tagService = new TagService(tagDao);
    tagService.init(config.getTags());

    log.debug("Registering resources...");
    env.jersey().register(new NamespaceResource(namespaceService));
    env.jersey().register(new SourceResource(sourceService));
    env.jersey()
        .register(new DatasetResource(namespaceService, datasetService, jobService, tagService));
    env.jersey().register(new JobResource(namespaceService, jobService));
    env.jersey().register(new TagResource(tagService));
    env.jersey().register(new MarquezServiceExceptionMapper());
  }

  private void registerServlets(@NonNull Environment env) {
    log.debug("Registering servlets...");

    // Expose metrics for monitoring.
    env.servlets().addServlet(PROMETHEUS, new MetricsServlet()).addMapping(PROMETHEUS_ENDPOINT);
  }
}
