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

package marquez.db.fixtures;

import static org.junit.Assert.fail;

import com.opentable.db.postgres.embedded.FlywayPreparer;
import com.opentable.db.postgres.embedded.PreparedDbProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.sql.SQLException;
import javax.sql.DataSource;
import marquez.MarquezApp;
import marquez.MarquezConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class AppWithPostgresRule extends DropwizardAppRule<MarquezConfig> {

  private static final String MIGRATION_LOCATION = "db/migration";
  private static final String POSTGRES_DB_NAME = "postgres";

  private Jdbi jdbi;

  private static DataSource dataSource;
  private static String urlString;

  static {
    PreparedDbProvider provider =
        PreparedDbProvider.forPreparer(FlywayPreparer.forClasspathLocation(MIGRATION_LOCATION));
    try {
      dataSource = provider.createDataSource();
      urlString = getUrlString();
    } catch (Throwable throwable) {
      fail("Could not initialize DB. Error msg: " + throwable.getLocalizedMessage());
    }
  }

  public AppWithPostgresRule() {
    super(
        MarquezApp.class,
        "src/test/resources/config.postgres.test.yml",
        ConfigOverride.config("db.url", urlString));
  }

  @Override
  protected void before() {
    super.before();
    // init DB
    JdbiFactory factory = new JdbiFactory();
    DataSourceFactory dataSourceFactory = getConfiguration().getDataSourceFactory();
    ManagedDataSource managedDataSource =
        dataSourceFactory.build(this.getEnvironment().metrics(), POSTGRES_DB_NAME);
    jdbi =
        factory
            .build(this.getEnvironment(), dataSourceFactory, managedDataSource, POSTGRES_DB_NAME)
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

  private static String getUrlString() throws SQLException {
    String url = dataSource.getConnection().getMetaData().getURL();

    int port = Integer.valueOf(url.split("//")[1].split(":")[1].split("/")[0]);
    return String.format("jdbc:postgresql://localhost:%d/%s", port, POSTGRES_DB_NAME);
  }
}
