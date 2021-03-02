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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.asList;
import static marquez.Generator.newTimestamp;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.db.models.ModelGenerator.newDatasetRow;
import static marquez.db.models.ModelGenerator.newDatasetRowWith;
import static marquez.db.models.ModelGenerator.newDatasetRowsWith;
import static marquez.db.models.ModelGenerator.newNamespaceRowWith;
import static marquez.db.models.ModelGenerator.newSourceRow;
import static marquez.db.models.ModelGenerator.newTagRow;
import static marquez.db.models.ModelGenerator.newTagRows;
import static marquez.db.models.ModelGenerator.toTagUuids;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class DatasetDaoTest {

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static List<Function<DatasetRow, ? extends Object>> DATASET_ROW_GETTERS =
      asList(
          DatasetRow::getUuid,
          DatasetRow::getType,
          DatasetRow::getName,
          DatasetRow::getCreatedAt,
          DatasetRow::getUpdatedAt,
          DatasetRow::getNamespaceUuid,
          DatasetRow::getSourceUuid,
          DatasetRow::getName,
          DatasetRow::getPhysicalName,
          DatasetRow::getTagUuids,
          DatasetRow::getLastModifiedAt,
          DatasetRow::getDescription,
          DatasetRow::getCurrentVersionUuid);

  private static void assertEquals(DatasetRow actual, DatasetRow expected) {
    for (Function<DatasetRow, ? extends Object> getter : DATASET_ROW_GETTERS) {
      assertThat(getter.apply(actual)).isEqualTo(getter.apply(expected));
    }
  }

  private static NamespaceDao namespaceDao;
  private static SourceDao sourceDao;
  private static DatasetDao datasetDao;
  private static DatasetVersionDao datasetVersionDao;
  private static TagDao tagDao;

  private static NamespaceRow namespaceRow;
  private static SourceRow sourceRow;
  private static List<TagRow> tagRows;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    sourceDao = jdbi.onDemand(SourceDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    tagDao = jdbi.onDemand(TagDao.class);

    namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    namespaceDao.insert(namespaceRow);

    sourceRow = newSourceRow();
    sourceDao.insert(sourceRow);

    tagRows = newTagRows(2);
    tagRows.forEach(tagRow -> tagDao.insert(tagRow));
  }

  @Test
  public void testInsert() {
    final int rowsBefore = datasetDao.count();

    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    final int rowsAfter = datasetDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testExists() {
    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    final boolean exists = datasetDao.exists(NAMESPACE_NAME.getValue(), newRow.getName());
    assertThat(exists).isTrue();
  }

  @Test
  public void testUpdateTags() {
    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    // Tag
    final TagRow newTagRow = newTagRow();
    tagDao.insert(newTagRow);

    final Instant taggedAt = newTimestamp();
    datasetDao.updateTags(newRow.getUuid(), newTagRow.getUuid(), taggedAt);

    final ExtendedDatasetRow row = datasetDao.findBy(newRow.getUuid()).get();
    assertThat(row).isNotNull();
    assertThat(row.getTagUuids()).contains(newTagRow.getUuid());
  }

  @Test
  public void testUpdateLastModifiedAt() {
    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    // Modified
    final Instant lastModifiedAt = newTimestamp();
    datasetDao.updateLastModifedAt(Lists.newArrayList(newRow.getUuid()), lastModifiedAt);

    final ExtendedDatasetRow row = datasetDao.findBy(newRow.getUuid()).get();
    assertThat(row.getLastModifiedAt()).isPresent().hasValue(lastModifiedAt);
  }

  @Test
  public void testFindBy() {
    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    final Optional<ExtendedDatasetRow> row = datasetDao.findBy(newRow.getUuid());
    assertThat(row).isPresent();
    assertEquals(newRow, row.get());
  }

  @Test
  public void testFindBy_notFound() {
    final DatasetRow newRow = newDatasetRow();

    final Optional<ExtendedDatasetRow> row = datasetDao.findBy(newRow.getUuid());
    assertThat(row).isEmpty();
  }

  @Test
  public void testFind() {
    final DatasetRow newRow =
        newDatasetRowWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows));
    datasetDao.insert(newRow);

    final Optional<ExtendedDatasetRow> row =
        datasetDao.find(NAMESPACE_NAME.getValue(), newRow.getName());
    assertThat(row).isPresent();
    ExtendedDatasetRow dsRow = row.get();
    assertEquals(newRow, dsRow);
  }

  @Test
  public void testFind_notFound() {
    final NamespaceName namespaceName = newNamespaceName();
    final DatasetName datasetName = newDatasetName();

    final Optional<ExtendedDatasetRow> row =
        datasetDao.find(namespaceName.getValue(), datasetName.getValue());
    assertThat(row).isEmpty();
  }

  @Test
  public void testFindAllIn_uuidList() {
    final List<DatasetRow> newRows =
        newDatasetRowsWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows), 4);
    newRows.forEach(newRow -> datasetDao.insert(newRow));

    final List<UUID> newRowUuids =
        newRows.stream().map(newRow -> newRow.getUuid()).collect(toImmutableList());

    final List<ExtendedDatasetRow> rows = datasetDao.findAllIn(newRowUuids);
    assertThat(rows).hasSize(4);

    final List<UUID> rowUuids = rows.stream().map(row -> row.getUuid()).collect(toImmutableList());
    assertThat(rowUuids).containsAll(newRowUuids);
  }

  @Test
  public void testFindAllIn_stringList() {
    final List<DatasetRow> newRows =
        newDatasetRowsWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows), 4);
    newRows.forEach(newRow -> datasetDao.insert(newRow));

    final List<String> newDatasetNames =
        newRows.stream().map(newRow -> newRow.getName()).collect(toImmutableList());

    final List<DatasetRow> rows = datasetDao.findAllIn(NAMESPACE_NAME.getValue(), newDatasetNames);
    assertThat(rows).hasSize(4);

    final List<String> datasetNames =
        rows.stream().map(row -> row.getName()).collect(toImmutableList());
    assertThat(datasetNames).containsAll(newDatasetNames);

    List<ExtendedDatasetRow> findAllExtendedIn =
        datasetDao.findAllIn(rows.stream().map(DatasetRow::getUuid).collect(toImmutableList()));

    assertThat(
            findAllExtendedIn.stream().map(ExtendedDatasetRow::getName).collect(toImmutableList()))
        .containsAll(newDatasetNames);
  }

  @Test
  public void testFindAll() {
    final List<DatasetRow> newRows =
        newDatasetRowsWith(namespaceRow.getUuid(), sourceRow.getUuid(), toTagUuids(tagRows), 4);
    newRows.forEach(newRow -> datasetDao.insert(newRow));

    final List<ExtendedDatasetRow> rows = datasetDao.findAll(NAMESPACE_NAME.getValue(), 4, 0);
    assertThat(rows).isNotNull().hasSize(4);
  }
}
