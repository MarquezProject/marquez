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

package marquez.service;

import static com.google.common.collect.Iterables.toArray;
import static marquez.Generator.newTimestamp;
import static marquez.common.models.ModelGenerator.newDatasetIdWith;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newFieldName;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.db.models.ModelGenerator.newDatasetVersionRowWith;
import static marquez.db.models.ModelGenerator.newNamespaceRowWith;
import static marquez.db.models.ModelGenerator.newRowUuid;
import static marquez.db.models.ModelGenerator.newSourceRowWith;
import static marquez.service.models.ModelGenerator.newVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.FieldName;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.Version;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DbTable;
import marquez.service.models.DbTableMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class DatasetServiceTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final Instant NOW = newTimestamp();

  private static final ImmutableList<UUID> NO_TAG_UUIDS = ImmutableList.of();
  private static final ImmutableList<TagRow> NO_TAG_ROWS = ImmutableList.of();
  private static final ImmutableList<DatasetFieldRow> NO_FIELD_ROWS = ImmutableList.of();

  // DB TABLE DATASET
  // DB TABLE DATASET
  private static final DatasetId DB_TABLE_ID = newDatasetIdWith(NAMESPACE_NAME);
  private static final DatasetName DB_TABLE_NAME = DB_TABLE_ID.getName();
  private static final DatasetName DB_TABLE_PHYSICAL_NAME = newDatasetName();
  private static final SourceName DB_TABLE_SOURCE_NAME = newSourceName();
  private static final String DB_TABLE_DESCRIPTION = newDescription();

  // DB TABLE ROW
  private static final NamespaceRow NAMESPACE_ROW = newNamespaceRowWith(NAMESPACE_NAME);
  private static final SourceRow SOURCE_ROW = newSourceRowWith(DB_TABLE_SOURCE_NAME);
  private static final ExtendedDatasetRow DATASET_ROW =
      new ExtendedDatasetRow(
          newRowUuid(),
          DatasetType.DB_TABLE.name(),
          NOW,
          NOW,
          NAMESPACE_ROW.getUuid(),
          NAMESPACE_NAME.getValue(),
          SOURCE_ROW.getUuid(),
          DB_TABLE_SOURCE_NAME.getValue(),
          DB_TABLE_NAME.getValue(),
          DB_TABLE_PHYSICAL_NAME.getValue(),
          NO_TAG_UUIDS,
          null,
          DB_TABLE_DESCRIPTION,
          null);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceDao namespaceDao;
  @Mock private SourceDao sourceDao;
  @Mock private DatasetDao datasetDao;
  @Mock private DatasetFieldDao datasetFieldDao;
  @Mock private DatasetVersionDao datasetVersionDao;
  @Mock private TagDao tagDao;
  @Mock private RunDao runDao;
  @Mock private RunService runService;
  private DatasetService datasetService;

  @Before
  public void setUp() {
    datasetService =
        new DatasetService(
            namespaceDao,
            sourceDao,
            datasetDao,
            datasetFieldDao,
            datasetVersionDao,
            tagDao,
            runDao,
            runService);
  }

  @Test
  public void testCreateOrUpdate() throws MarquezServiceException {
    when(datasetDao.exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue())).thenReturn(false);

    final NamespaceRow namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    when(namespaceDao.findBy(NAMESPACE_NAME.getValue())).thenReturn(Optional.of(namespaceRow));

    final SourceRow sourceRow = newSourceRowWith(DB_TABLE_SOURCE_NAME);
    when(sourceDao.findBy(DB_TABLE_SOURCE_NAME.getValue())).thenReturn(Optional.of(sourceRow));

    when(datasetDao.find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue()))
        .thenReturn(Optional.of(DATASET_ROW));
    when(tagDao.findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class)))
        .thenReturn(Lists.newArrayList());

    // Meta
    final DatasetMeta dbTableMeta =
        new DbTableMeta(
            DB_TABLE_PHYSICAL_NAME, DB_TABLE_SOURCE_NAME, null, null, DB_TABLE_DESCRIPTION, null);

    // Version
    final Version version = dbTableMeta.version(NAMESPACE_NAME, DB_TABLE_NAME);
    when(datasetVersionDao.exists(version.getValue())).thenReturn(false);

    final DatasetVersionRow datasetVersionRow =
        newDatasetVersionRowWith(DATASET_ROW.getUuid(), version, NO_TAG_UUIDS, null);
    when(datasetVersionDao.find(
            DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null)))
        .thenReturn(Optional.of(datasetVersionRow));
    when(datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)))
        .thenReturn(NO_FIELD_ROWS);

    // Dataset
    final Dataset dbTable =
        datasetService.createOrUpdate(NAMESPACE_NAME, DB_TABLE_NAME, dbTableMeta);
    assertThat(dbTable.getId()).isEqualTo(DB_TABLE_ID);
    assertThat(dbTable.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(dbTable.getPhysicalName()).isEqualTo(DB_TABLE_PHYSICAL_NAME);
    assertThat(dbTable.getCreatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getUpdatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getSourceName()).isEqualTo(DB_TABLE_SOURCE_NAME);
    assertThat(dbTable.getFields()).isEqualTo(NO_FIELD_ROWS);
    assertThat(dbTable.getTags()).isEmpty();
    assertThat(dbTable.getLastModifiedAt()).isEmpty();
    assertThat(dbTable.getDescription()).isEqualTo(Optional.of(DB_TABLE_DESCRIPTION));

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME.getValue());
    verify(sourceDao, times(1)).findBy(DB_TABLE_SOURCE_NAME.getValue());
    verify(datasetDao, times(1)).exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
    verify(datasetDao, times(2)).find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
    verify(datasetFieldDao, times(1))
        .findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class));
    verify(datasetVersionDao, times(1)).exists(version.getValue());
    verify(datasetVersionDao, times(1))
        .find(DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null));
    verify(tagDao, times(1)).findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class));
  }

  @Test
  public void testExists_dataset() throws MarquezServiceException {
    when(datasetDao.exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue())).thenReturn(true);

    final boolean exists = datasetService.exists(NAMESPACE_NAME, DB_TABLE_NAME);
    assertThat(exists).isTrue();

    verify(datasetDao, times(1)).exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
  }

  @Test
  public void testExists_datasetNotFound() throws MarquezServiceException {
    when(datasetDao.exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue())).thenReturn(false);

    final boolean exists = datasetService.exists(NAMESPACE_NAME, DB_TABLE_NAME);
    assertThat(exists).isFalse();

    verify(datasetDao, times(1)).exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
  }

  @Test
  public void testExists_field() throws MarquezServiceException {
    final FieldName fieldName = newFieldName();
    when(datasetFieldDao.exists(
            NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue(), fieldName.getValue()))
        .thenReturn(true);

    final boolean exists = datasetService.fieldExists(NAMESPACE_NAME, DB_TABLE_NAME, fieldName);
    assertThat(exists).isTrue();

    verify(datasetFieldDao, times(1))
        .exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue(), fieldName.getValue());
  }

  @Test
  public void testExists_fieldNotFound() throws MarquezServiceException {
    final FieldName fieldName = newFieldName();
    when(datasetFieldDao.exists(
            NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue(), fieldName.getValue()))
        .thenReturn(false);

    final boolean exists = datasetService.fieldExists(NAMESPACE_NAME, DB_TABLE_NAME, fieldName);
    assertThat(exists).isFalse();

    verify(datasetFieldDao, times(1))
        .exists(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue(), fieldName.getValue());
  }

  @Test
  public void testGet() throws MarquezServiceException {
    when(datasetDao.find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue()))
        .thenReturn(Optional.of(DATASET_ROW));
    when(tagDao.findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class))).thenReturn(NO_TAG_ROWS);

    // Version
    final Version version = newVersion();
    final DatasetVersionRow datasetVersionRow =
        newDatasetVersionRowWith(DATASET_ROW.getUuid(), version, NO_TAG_UUIDS, null);
    when(datasetVersionDao.find(
            DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null)))
        .thenReturn(Optional.of(datasetVersionRow));
    when(datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)))
        .thenReturn(NO_FIELD_ROWS);

    final DbTable dbTable = (DbTable) datasetService.get(NAMESPACE_NAME, DB_TABLE_NAME).get();
    assertThat(dbTable.getId()).isEqualTo(DB_TABLE_ID);
    assertThat(dbTable.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(dbTable.getPhysicalName()).isEqualTo(DB_TABLE_PHYSICAL_NAME);
    assertThat(dbTable.getCreatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getUpdatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getSourceName()).isEqualTo(DB_TABLE_SOURCE_NAME);
    assertThat(dbTable.getFields()).isEqualTo(NO_FIELD_ROWS);
    assertThat(dbTable.getTags()).isEmpty();
    assertThat(dbTable.getLastModifiedAt()).isEmpty();
    assertThat(dbTable.getDescription()).isEqualTo(Optional.of(DB_TABLE_DESCRIPTION));

    verify(datasetDao, times(1)).find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
    verify(datasetFieldDao, times(1))
        .findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class));
    verify(datasetVersionDao, times(1))
        .find(DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null));
    verify(tagDao, times(1)).findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class));
  }

  @Test
  public void testGetBy() throws MarquezServiceException {
    final UUID datasetVersionUuid = UUID.randomUUID();
    final DatasetVersionId datasetVersionId =
        new DatasetVersionId(NAMESPACE_NAME, DB_TABLE_NAME, datasetVersionUuid);

    when(datasetDao.find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue()))
        .thenReturn(Optional.of(DATASET_ROW));
    when(tagDao.findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class))).thenReturn(NO_TAG_ROWS);

    // Version
    final Version version = newVersion();
    final DatasetVersionRow datasetVersionRow =
        newDatasetVersionRowWith(DATASET_ROW.getUuid(), version, NO_TAG_UUIDS, null);
    when(datasetVersionDao.find(DATASET_ROW.getType(), datasetVersionUuid))
        .thenReturn(Optional.of(datasetVersionRow));
    when(datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)))
        .thenReturn(NO_FIELD_ROWS);

    final DbTable dbTable = (DbTable) datasetService.getBy(datasetVersionId).get();
    assertThat(dbTable.getId()).isEqualTo(DB_TABLE_ID);
    assertThat(dbTable.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(dbTable.getPhysicalName()).isEqualTo(DB_TABLE_PHYSICAL_NAME);
    assertThat(dbTable.getCreatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getUpdatedAt()).isEqualTo(NOW);
    assertThat(dbTable.getSourceName()).isEqualTo(DB_TABLE_SOURCE_NAME);
    assertThat(dbTable.getFields()).isEqualTo(NO_FIELD_ROWS);
    assertThat(dbTable.getTags()).isEmpty();
    assertThat(dbTable.getLastModifiedAt()).isEmpty();
    assertThat(dbTable.getDescription()).isEqualTo(Optional.of(DB_TABLE_DESCRIPTION));

    verify(datasetDao, times(1)).find(NAMESPACE_NAME.getValue(), DB_TABLE_NAME.getValue());
    verify(datasetFieldDao, times(1))
        .findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class));
    verify(datasetVersionDao, times(1)).find(DATASET_ROW.getType(), datasetVersionUuid);
    verify(tagDao, times(1)).findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class));
  }

  @Test
  public void testGetAll() throws MarquezServiceException {
    when(tagDao.findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class))).thenReturn(NO_TAG_ROWS);

    // Version
    final Version version = newVersion();
    final DatasetVersionRow datasetVersionRow =
        newDatasetVersionRowWith(DATASET_ROW.getUuid(), version, NO_TAG_UUIDS, null);
    when(datasetVersionDao.find(
            DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null)))
        .thenReturn(Optional.of(datasetVersionRow));
    when(datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)))
        .thenReturn(NO_FIELD_ROWS);

    final List<ExtendedDatasetRow> newDatasetRows = Lists.newArrayList(DATASET_ROW);
    when(datasetDao.findAll(NAMESPACE_NAME.getValue(), 4, 0)).thenReturn(newDatasetRows);

    final List<Dataset> datasets = datasetService.getAll(NAMESPACE_NAME, 4, 0);
    assertThat(datasets).isNotNull().hasSize(1);

    verify(datasetDao, times(1)).findAll(NAMESPACE_NAME.getValue(), 4, 0);
    verify(datasetFieldDao, times(1))
        .findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class));
    verify(datasetVersionDao, times(1))
        .find(DATASET_ROW.getType(), DATASET_ROW.getCurrentVersionUuid().orElse(null));
    verify(tagDao, times(1)).findAllIn(toArray(DATASET_ROW.getTagUuids(), UUID.class));
  }
}
