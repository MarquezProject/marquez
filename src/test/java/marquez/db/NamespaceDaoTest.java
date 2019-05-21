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

  private static NamespaceDao namespaceDao;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
  }

  @Test
  public void testInsert() {
    final int rowsBefore = namespaceDao.count();

    final NamespaceRow newNamespaceRow = newNamespaceRow();
    namespaceDao.insert(newNamespaceRow);

    final int rowsAfter = namespaceDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testInsertAndGet() {
    final NamespaceRow newNamespaceRow = newNamespaceRow();
    final NamespaceRow namespaceRow = namespaceDao.insertAndGet(newNamespaceRow).orElse(null);
    assertThat(namespaceRow).isNotNull();
    assertThat(namespaceRow.getUuid()).isEqualTo(newNamespaceRow.getUuid());
  }

  @Test
  public void testExists() {
    final NamespaceName namespaceName = newNamespaceName();
    final NamespaceRow newNamespaceRow = newNamespaceRowWith(namespaceName);
    namespaceDao.insert(newNamespaceRow);

    final boolean exists = namespaceDao.exists(namespaceName);
    assertThat(exists).isTrue();
  }

  @Test
  public void testFindBy_uuid() {
    final NamespaceName namespaceName = newNamespaceName();
    final NamespaceRow newNamespaceRow = newNamespaceRowWith(namespaceName);
    namespaceDao.insert(newNamespaceRow);

    final NamespaceRow namespaceRow = namespaceDao.findBy(newNamespaceRow.getUuid()).orElse(null);
    assertThat(namespaceRow).isNotNull();
    assertThat(namespaceRow.getUuid()).isEqualTo(newNamespaceRow.getUuid());
  }

  @Test
  public void testFindBy_name() {
    final NamespaceName namespaceName = newNamespaceName();
    final NamespaceRow newNamespaceRow = newNamespaceRowWith(namespaceName);
    namespaceDao.insert(newNamespaceRow);

    final NamespaceRow namespaceRow = namespaceDao.findBy(namespaceName).orElse(null);
    assertThat(namespaceRow).isNotNull();
    assertThat(namespaceRow.getName()).isEqualTo(namespaceName.getValue());
  }

  @Test
  public void testFindAll() {
    final List<NamespaceRow> newNamespaceRows = newNamespaceRows(4);
    newNamespaceRows.forEach(newNamespaceRow -> namespaceDao.insert(newNamespaceRow));

    final List<NamespaceRow> namespaceRows = namespaceDao.findAll();
    assertThat(namespaceRows).isNotNull();
    assertThat(namespaceRows).hasSize(4);
  }
}
