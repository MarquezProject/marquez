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

import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newNamespaceRowWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.common.models.NamespaceName;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class NamespaceDaoTest {
  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres()
          .withPlugin(new SqlObjectPlugin())
          .withMigration(Migration.before().withDefaultPath());

  private static NamespaceDao dao;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    dao = jdbi.onDemand(NamespaceDao.class);
  }

  @Test
  public void testInsert() {
    final int rowsBefore = dao.count();

    final NamespaceRow newRow = newNamespaceRow();
    dao.insert(newRow);

    final int rowsAfter = dao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testInsert_noThrownException_onDuplicateRow() {
    final int rowsBefore = dao.count();

    final NamespaceRow newRow = newNamespaceRow();
    dao.insert(newRow);

    // Reinsert row
    assertThatCode(() -> dao.insert(newRow)).doesNotThrowAnyException();

    final int rowsAfter = dao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore);
  }

  @Test
  public void testInsertAndGet() {
    final NamespaceRow newRow = newNamespaceRow();
    final NamespaceRow row = dao.insertAndGet(newRow).get();
    assertThat(row).isNotNull();
    assertThat(row.getUuid()).isEqualTo(newRow.getUuid());
  }

  @Test
  public void testExists() {
    final NamespaceName name = newNamespaceName();
    final NamespaceRow newRow = newNamespaceRowWith(name);
    dao.insert(newRow);

    final boolean exists = dao.exists(name);
    assertThat(exists).isTrue();
  }

  @Test
  public void testFindBy_uuid() {
    final NamespaceRow newRow = newNamespaceRow();
    dao.insert(newRow);

    final NamespaceRow row = dao.findBy(newRow.getUuid()).get();
    assertThat(row).isNotNull();
    assertThat(row.getUuid()).isEqualTo(newRow.getUuid());
  }

  @Test
  public void testFindBy_name() {
    final NamespaceName name = newNamespaceName();
    final NamespaceRow newRow = newNamespaceRowWith(name);
    dao.insert(newRow);

    final NamespaceRow row = dao.findBy(name).get();
    assertThat(row).isNotNull();
    assertThat(row.getName()).isEqualTo(name.getValue());
  }

  @Test
  public void testFindAll() {
    final List<NamespaceRow> newRows = newNamespaceRows(4);
    newRows.forEach(newRow -> dao.insert(newRow));

    final List<NamespaceRow> rows = dao.findAll(10, 0);
    assertThat(rows).isNotNull();
    assertThat(rows).hasSize(4);
  }
}
