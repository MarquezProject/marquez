/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import com.codahale.metrics.jdbi3.InstrumentedSqlLogger;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
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
import io.sentry.Sentry;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.filter.JobRedirectFilter;
import marquez.api.filter.exclusions.Exclusions;
import marquez.api.filter.exclusions.ExclusionsConfig;
import marquez.cli.DbMigrateCommand;
import marquez.cli.DbRetentionCommand;
import marquez.cli.MetadataCommand;
import marquez.cli.SeedCommand;
import marquez.common.Utils;
import marquez.db.DbMigration;
import marquez.jobs.DbRetentionJob;
import marquez.jobs.MaterializeViewRefresherJob;
import marquez.logging.DelegatingSqlLogger;
import marquez.logging.LabelledSqlLogger;
import marquez.logging.LoggingMdcFilter;
import marquez.service.DatabaseMetrics;
import marquez.tracing.SentryConfig;
import marquez.tracing.TracingContainerResponseFilter;
import marquez.tracing.TracingSQLLogger;
import marquez.tracing.TracingServletFilter;
import org.flywaydb.core.api.FlywayException;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
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
  private static final String PROMETHEUS_V2 = "prometheus_v2";
  private static final String PROMETHEUS_ENDPOINT = "/metrics";
  private static final String PROMETHEUS_ENDPOINT_V2 = "/v2beta/metrics";

  public static void main(final String[] args) throws Exception {
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
    DatabaseMetrics.registry.register(new DropwizardExports(bootstrap.getMetricRegistry()));
    DefaultExports.initialize(); // Add metrics for CPU, JVM memory, etc.
    DefaultExports.register(DatabaseMetrics.registry);

    // Enable variable substitution with environment variables.
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(),
            new EnvironmentVariableSubstitutor(ERROR_ON_UNDEFINED)));

    // Add CLI commands
    bootstrap.addCommand(new DbMigrateCommand());
    bootstrap.addCommand(new DbRetentionCommand());
    bootstrap.addCommand(new MetadataCommand());
    bootstrap.addCommand(new SeedCommand());

    bootstrap.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    Utils.addZonedDateTimeMixin(bootstrap.getObjectMapper());

    // Add graphql playground
    bootstrap.addBundle(
        new AssetsBundle(
            "/assets",
            "/graphql-playground",
            "graphql-playground/index.htm",
            "graphql-playground"));
  }

  @Override
  public void run(@NonNull MarquezConfig config, @NonNull Environment env) {
    final DataSourceFactory sourceFactory = config.getDataSourceFactory();
    final ManagedDataSource source = sourceFactory.build(env.metrics(), DB_SOURCE_NAME);

    log.info("Running startup actions...");

    try {
      DbMigration.migrateDbOrError(config.getFlywayFactory(), source, config.isMigrateOnStartup());
    } catch (FlywayException errorOnDbMigrate) {
      log.info("Stopping app...");
      // Propagate throwable up the stack.
      onFatalError(errorOnDbMigrate); // Signal app termination.
    }

    if (isSentryEnabled(config)) {
      Sentry.init(
          options -> {
            options.setTracesSampleRate(config.getSentry().getTracesSampleRate());
            options.setEnvironment(config.getSentry().getEnvironment());
            options.setDsn(config.getSentry().getDsn());
            options.setDebug(config.getSentry().isDebug());
          });

      env.servlets()
          .addFilter("tracing-filter", new TracingServletFilter())
          .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
      env.jersey().register(new TracingContainerResponseFilter());
    }

    final Jdbi jdbi = newJdbi(config, env, source);
    final MarquezContext marquezContext =
        MarquezContext.builder()
            .jdbi(jdbi)
            .searchConfig(config.getSearchConfig())
            .tags(config.getTags())
            .build();

    registerResources(config, env, marquezContext);
    registerServlets(env);
    registerFilters(env, marquezContext);

    // Add scheduled jobs to lifecycle.
    if (config.hasDbRetentionPolicy()) {
      // Add job to apply retention policy to database.
      env.lifecycle().manage(new DbRetentionJob(jdbi, config.getDbRetention()));
    }

    // Add job to refresh materialized views.
    env.lifecycle().manage(new MaterializeViewRefresherJob(jdbi));

    // set namespaceFilter
    ExclusionsConfig exclusions = config.getExclude();
    Exclusions.use(exclusions);
  }

  private boolean isSentryEnabled(MarquezConfig config) {
    return config.getSentry() != null
        && !config.getSentry().getDsn().equals(SentryConfig.DEFAULT_DSN);
  }

  /** Returns a new {@link Jdbi} object. */
  private Jdbi newJdbi(
      @NonNull MarquezConfig config, @NonNull Environment env, @NonNull ManagedDataSource source) {
    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(env, config.getDataSourceFactory(), source, DB_POSTGRES)
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin());
    SqlLogger sqlLogger =
        new DelegatingSqlLogger(new LabelledSqlLogger(), new InstrumentedSqlLogger(env.metrics()));
    if (isSentryEnabled(config)) {
      sqlLogger = new TracingSQLLogger(sqlLogger);
    }
    jdbi.setSqlLogger(sqlLogger);
    jdbi.getConfig(Jackson2Config.class).setMapper(Utils.getMapper());
    return jdbi;
  }

  public void registerResources(
      @NonNull MarquezConfig config, @NonNull Environment env, MarquezContext context) {

    if (config.getGraphql().isEnabled()) {
      env.servlets()
          .addServlet("api/v1-beta/graphql", context.getGraphqlServlet())
          .addMapping("/api/v1-beta/graphql", "/api/v1/schema.json");
    }

    log.debug("Registering resources...");
    for (final Object resource : context.getResources()) {
      env.jersey().register(resource);
    }
  }

  private void registerServlets(@NonNull Environment env) {
    log.debug("Registering servlets...");

    // Expose metrics for monitoring.
    env.servlets().addServlet(PROMETHEUS, new MetricsServlet()).addMapping(PROMETHEUS_ENDPOINT);
    env.servlets()
        .addServlet(PROMETHEUS_V2, new MetricsServlet(DatabaseMetrics.registry))
        .addMapping(PROMETHEUS_ENDPOINT_V2);
  }

  private void registerFilters(@NonNull Environment env, MarquezContext marquezContext) {
    env.jersey().getResourceConfig().register(new LoggingMdcFilter());
    env.jersey()
        .getResourceConfig()
        .register(new JobRedirectFilter(marquezContext.getJobService()));
  }
}
