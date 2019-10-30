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

package marquez.db;

import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.db.models.ModelGenerator.newSourceRow;
import static marquez.db.models.ModelGenerator.newSourceRowWith;
import static marquez.db.models.ModelGenerator.newSourceRows;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.MarquezDb;
import marquez.common.models.SourceName;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class SourceDaoTest {
  private static final MarquezDb DB = MarquezDb.create();

  static {
    DB.start();
  }

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.externalPostgres(
              DB.getHost(), DB.getPort(), DB.getUsername(), DB.getPassword(), DB.getDatabaseName())
          .withPlugin(new SqlObjectPlugin())
          .withMigration(Migration.before().withDefaultPath());

  private static SourceDao sourceDao;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    sourceDao = jdbi.onDemand(SourceDao.class);
  }

  @Test
  public void testInsert() {
    final int rowsBefore = sourceDao.count();

    final SourceRow newRow = newSourceRow();
    sourceDao.insert(newRow);

    final int rowsAfter = sourceDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testExists() {
    final SourceName sourceName = newSourceName();
    final SourceRow newRow = newSourceRowWith(sourceName);
    sourceDao.insert(newRow);

    final boolean exists = sourceDao.exists(sourceName.getValue());
    assertThat(exists).isTrue();
  }

  @Test
  public void testFindBy_uuid() {
    final SourceName sourceName = newSourceName();
    final SourceRow newRow = newSourceRowWith(sourceName);
    sourceDao.insert(newRow);

    final Optional<SourceRow> row = sourceDao.findBy(newRow.getUuid());
    assertThat(row).isPresent();
    assertThat(row.get().getUuid()).isEqualTo(newRow.getUuid());
  }

  @Test
  public void testFindBy_name() {
    final SourceName sourceName = newSourceName();
    final SourceRow newRow = newSourceRowWith(sourceName);
    sourceDao.insert(newRow);

    final Optional<SourceRow> row = sourceDao.findBy(newRow.getName());
    assertThat(row).isPresent();
    assertThat(row.get().getUuid()).isEqualTo(newRow.getUuid());
  }

  @Test
  public void testFindAll() {
    final List<SourceRow> newRows = newSourceRows(4);
    newRows.forEach(newRow -> sourceDao.insert(newRow));

    final List<SourceRow> rows = sourceDao.findAll(4, 0);
    assertThat(rows).isNotNull();
    assertThat(rows).hasSize(4);
  }
}
