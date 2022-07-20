/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jdbi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/** JUnit Extension to manage a Jdbi instance pointed to external Postgres instance. */
public abstract class JdbiExternalPostgresExtension
    implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

  @Retention(RetentionPolicy.RUNTIME)
  public @interface FlywayTarget {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface FlywaySkipRepeatable {}

  protected final List<JdbiPlugin> plugins = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private volatile DataSource dataSource;
  private Jdbi jdbi;
  private boolean installPlugins;
  private Flyway flyway;
  private Handle handle;
  protected Migration migration;

  public JdbiExternalPostgresExtension() {}

  private DataSource getDataSource() {
    if (dataSource == null) {
      try {
        lock.lock();
        if (dataSource == null) {
          dataSource = createDataSource();
        }
      } finally {
        lock.unlock();
      }
    }
    return dataSource;
  }

  protected abstract DataSource createDataSource();

  /** Discover and install plugins from the classpath. */
  public JdbiExternalPostgresExtension withPlugins() {
    installPlugins = true;
    return this;
  }

  /** Install a plugin into JdbiRule. */
  public JdbiExternalPostgresExtension withPlugin(final JdbiPlugin plugin) {
    plugins.add(plugin);
    return this;
  }

  /** Run database migration. */
  public JdbiExternalPostgresExtension withMigration(final Migration newMigration) {
    this.migration = newMigration;
    return this;
  }

  /** Get Jdbi, in case you want to open additional handles to the same data source. */
  public Jdbi getJdbi() {
    return jdbi;
  }

  /** Get the single Handle instance opened for the duration of this test case. */
  public Handle getHandle() {
    return handle;
  }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    if (migration != null) {
      FluentConfiguration flywayConfig =
          Flyway.configure()
              .dataSource(getDataSource())
              .locations(migration.paths.toArray(new String[0]))
              .schemas(migration.schemas.toArray(new String[0]));

      FlywayTarget target = context.getRequiredTestClass().getAnnotation(FlywayTarget.class);
      if (target != null) {
        flywayConfig.target(target.value());
      }
      FlywaySkipRepeatable ignore =
          context.getRequiredTestClass().getAnnotation(FlywaySkipRepeatable.class);
      if (ignore != null) {
        // This would be preferable, but we don't have access to Flyway Teams edition, so...
        // flywayConfig.ignoreMigrationPatterns("repetable:*");
        flywayConfig.repeatableSqlMigrationPrefix("Z__");
      }

      flyway = flywayConfig.load();
      flyway.migrate();
    }

    jdbi = Jdbi.create(getDataSource());
    if (installPlugins) {
      jdbi.installPlugins();
    }
    plugins.forEach(jdbi::installPlugin);
    handle = jdbi.open();
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    if (flyway != null && migration.cleanAfter) {
      flyway.clean();
    }
    handle.close();
    jdbi = null;
    dataSource = null;
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType() == Jdbi.class;
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return jdbi;
  }
}
