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

import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.db.models.DbModelGenerator.newDatasetRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowsWith;
import static marquez.db.models.DbModelGenerator.newDatasourceRow;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.util.List;
import java.util.UUID;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.DatasourceRow;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class DatasetDaoTest {
  private static final int LIMIT = 100;
  private static final int OFFSET = 0;

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres().withPlugin(new SqlObjectPlugin()).migrateWithFlyway();

  private static NamespaceDao namespaceDao;
  private static DatasourceDao datasourceDao;
  private static DatasetDao datasetDao;

  private DatasourceRow datasourceRow;
  private NamespaceRow namespaceRow;
  private NamespaceName namespaceName;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    datasourceDao = jdbi.onDemand(DatasourceDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
  }

  @Before
  public void setUp() {
    datasourceRow = newDatasourceRow();
    namespaceRow = newNamespaceRow();

    namespaceDao.insert(namespaceRow);
    datasourceDao.insert(datasourceRow);
    namespaceName = NamespaceName.fromString(namespaceRow.getName());
  }

  @Test
  public void testInsert() {
    final int rowsBefore = datasetDao.count();

    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid());
    datasetDao.insert(newDatasetRow);

    final int rowsAfter = datasetDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testInsert_throwsException_onDuplicateRow() {
    final int rowsBefore = datasetDao.count();

    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid());
    datasetDao.insert(newDatasetRow);

    final int rowsAfter = datasetDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);

    assertThatExceptionOfType(UnableToExecuteStatementException.class)
        .isThrownBy(() -> datasetDao.insert(newDatasetRow));
  }

  @Test
  public void testInsertAndGet_throwsException_onDuplicateRow() {
    final int rowsBefore = datasetDao.count();

    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid());
    datasetDao.insert(newDatasetRow);

    final int rowsAfter = datasetDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);

    assertThatExceptionOfType(UnableToExecuteStatementException.class)
        .isThrownBy(() -> datasetDao.insertAndGet(newDatasetRow));
  }

  @Test
  public void testInsertAndGet() {
    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid());
    final DatasetRow datasetRow = datasetDao.insertAndGet(newDatasetRow).orElse(null);
    assertThat(datasetRow).isNotNull();
    assertThat(datasetRow.getUuid()).isEqualTo(newDatasetRow.getUuid());
  }

  @Test
  public void testUpdateCurrentVersionUuid() {
    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid());
    final DatasetRow datasetRow = datasetDao.insertAndGet(newDatasetRow).orElse(null);
    assertThat(datasetRow.getCurrentVersionUuid()).isNull();

    final UUID currentVersionUuid = UUID.randomUUID();
    datasetDao.updateCurrentVersionUuid(datasetRow.getUuid(), currentVersionUuid);

    final DatasetRowExtended datasetRowExtendedWithVersion =
        datasetDao.findBy(datasetRow.getUuid()).orElse(null);
    assertThat(datasetRowExtendedWithVersion.getCurrentVersionUuid()).isNotNull();
    assertThat(datasetRowExtendedWithVersion.getCurrentVersionUuid()).isEqualTo(currentVersionUuid);
    assertThat(datasetRowExtendedWithVersion.getUpdatedAt()).isAfter(datasetRow.getUpdatedAt());
  }

  @Test
  public void testExists() {
    final DatasetUrn datasetUrn = newDatasetUrn();
    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid(), datasetUrn);
    datasetDao.insert(newDatasetRow);

    final boolean exists = datasetDao.exists(datasetUrn);
    assertThat(exists).isTrue();
  }

  @Test
  public void testFindBy_uuid() {
    final UUID uuid = UUID.randomUUID();
    final DatasetRow newDatasetRow =
        newDatasetRowWith(uuid, namespaceRow.getUuid(), datasourceRow.getUuid());
    datasetDao.insert(newDatasetRow);

    final DatasetRowExtended datasetRowExtended = datasetDao.findBy(uuid).orElse(null);
    assertThat(datasetRowExtended).isNotNull();
    assertThat(datasetRowExtended.getUuid()).isEqualTo(uuid);
  }

  @Test
  public void testFindBy_uuid_noRowsFound() {
    assertThat(datasetDao.findBy(UUID.randomUUID())).isNotPresent();
  }

  @Test
  public void testFindBy_urn() {
    final DatasetUrn datasetUrn = newDatasetUrn();
    final DatasetRow newDatasetRow =
        newDatasetRowWith(namespaceRow.getUuid(), datasourceRow.getUuid(), datasetUrn);
    datasetDao.insert(newDatasetRow);

    final DatasetRowExtended datasetRowExtended = datasetDao.findBy(datasetUrn).orElse(null);
    assertThat(datasetRowExtended).isNotNull();
    assertThat(datasetRowExtended.getUrn()).isEqualTo(datasetUrn.getValue());
  }

  @Test
  public void testFindAll() {
    final int rowsToInsert = 4;
    final List<DatasetRow> newDatasetRows =
        newDatasetRowsWith(namespaceRow.getUuid(), datasourceRow.getUuid(), rowsToInsert);
    newDatasetRows.forEach(newDatasetRow -> datasetDao.insert(newDatasetRow));

    final List<DatasetRowExtended> datasetRowsExtended =
        datasetDao.findAll(namespaceName, LIMIT, OFFSET);
    assertThat(datasetRowsExtended).isNotNull();
    assertThat(datasetRowsExtended).hasSize(rowsToInsert);
  }

  @Test
  public void testFindAll_noRowsFound() {
    final List<DatasetRowExtended> datasetRowsExtended =
        datasetDao.findAll(namespaceName, LIMIT, OFFSET);
    assertThat(datasetRowsExtended).isEmpty();
  }

  @Test
  public void testFindAll_limitOnly() {
    final List<DatasetRow> newDatasetRows =
        newDatasetRowsWith(namespaceRow.getUuid(), datasourceRow.getUuid(), 4);
    newDatasetRows.forEach(newDatasetRow -> datasetDao.insert(newDatasetRow));

    final List<DatasetRowExtended> datasetRowsExtended =
        datasetDao.findAll(namespaceName, 2, OFFSET);
    assertThat(datasetRowsExtended).isNotNull();
    assertThat(datasetRowsExtended).hasSize(2);
    assertThat(datasetRowsExtended.get(0).getUuid()).isEqualTo(newDatasetRows.get(0).getUuid());
    assertThat(datasetRowsExtended.get(1).getUuid()).isEqualTo(newDatasetRows.get(1).getUuid());
  }

  @Test
  public void testFindAll_offsetOnly() {
    final List<DatasetRow> newDatasetRows =
        newDatasetRowsWith(namespaceRow.getUuid(), datasourceRow.getUuid(), 4);
    newDatasetRows.forEach(newDatasetRow -> datasetDao.insert(newDatasetRow));

    final List<DatasetRowExtended> datasetRowsExtended =
        datasetDao.findAll(namespaceName, LIMIT, 2);
    assertThat(datasetRowsExtended).isNotNull();
    assertThat(datasetRowsExtended).hasSize(2);
    assertThat(datasetRowsExtended.get(0).getUuid()).isEqualTo(newDatasetRows.get(2).getUuid());
    assertThat(datasetRowsExtended.get(1).getUuid()).isEqualTo(newDatasetRows.get(3).getUuid());
  }
}
