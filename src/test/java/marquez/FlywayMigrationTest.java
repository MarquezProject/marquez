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

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category(IntegrationTests.class)
public class FlywayMigrationTest {
    private static final String CONFIG_FILE = "config-flyway-initSql.test.yml";
    private static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

    private static final MarquezDb DB =
        MarquezDb.create();

    static {
        DB.withInitScript("db-init.sql").start();
    }

    @ClassRule
    public static final JdbiRule dbRule =
        JdbiRule.externalPostgres(
            DB.getHost(), DB.getPort(), DB.getUsername(), DB.getPassword(), DB.getDatabaseName());

    @ClassRule
    public static final DropwizardAppRule<MarquezConfig> APP =
        new DropwizardAppRule<>(
            MarquezApp.class,
            CONFIG_FILE_PATH,
            ConfigOverride.config("db.url", DB.getJdbcUrl()),
            ConfigOverride.config("db.user", DB.getUsername()),
            ConfigOverride.config("db.password", DB.getPassword()));

    @Test
    public void testRole() {
        int numMarquezTables =
            dbRule.getHandle()
                .createQuery("SELECT * FROM pg_tables WHERE tableowner = 'marquez';")
                .map(x -> x).list().size();
        assert numMarquezTables > 0;
    }
}
