/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package marquez.jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/** JUnit Extension to manage a Jdbi instance pointed to external Postgres instance. */
public abstract class JdbiExternalPostgresExtension
    implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

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

  /**
   * Discover and install plugins from the classpath.
   *
   * @see JdbiRule#withPlugin(JdbiPlugin) we recommend installing plugins explicitly instead
   */
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
      flyway =
          Flyway.configure()
              .dataSource(getDataSource())
              .locations(migration.paths.toArray(new String[0]))
              .schemas(migration.schemas.toArray(new String[0]))
              .load();
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
