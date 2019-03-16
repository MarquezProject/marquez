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

import static org.junit.Assert.assertEquals;

import marquez.common.models.NamespaceName;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasourceRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.models.Generator;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class DatasetDaoTest {
  private static DatasetDao datasetDAO;
  private static marquez.service.models.Namespace namespace;
  private static NamespaceDao namespaceDAO;

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres().withPlugin(new SqlObjectPlugin()).migrateWithFlyway();

  @BeforeClass
  public static void setup() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDAO = jdbi.onDemand(NamespaceDao.class);
    datasetDAO = jdbi.onDemand(DatasetDao.class);
  }

  @Before
  public void setupTest() {
    namespace = Generator.genNamespace();
    namespaceDAO.insert(namespace);
  }

  private void insertRandomDataset() {
    final DatasourceRow datasourceRow = Generator.genDatasourceRow();
    final DatasetRow datasetRow =
        Generator.genDatasetRow(namespace.getGuid(), datasourceRow.getUuid());
    final DbTableInfoRow dbTableInfoRow = Generator.genDbTableInfowRow();
    final DbTableVersionRow dbTableVersionRow =
        Generator.genDbTableVersionRow(datasetRow.getUuid(), dbTableInfoRow.getUuid());
    datasetDAO.insertAll(datasourceRow, datasetRow, dbTableInfoRow, dbTableVersionRow);
  }

  @Test
  public void testFindAll() {
    assertEquals(
        0, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
    insertRandomDataset();
    insertRandomDataset();
    assertEquals(
        2, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), 10, 0).size());
  }

  @Test
  public void testFindAllWithLimit() {
    final int limit = 1;
    assertEquals(
        0, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), limit, 0).size());
    insertRandomDataset();
    insertRandomDataset();
    assertEquals(
        limit, datasetDAO.findAll(NamespaceName.fromString(namespace.getName()), limit, 0).size());
  }
}
