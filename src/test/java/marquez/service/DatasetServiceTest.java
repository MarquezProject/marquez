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

import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.db.models.DbModelGenerator.newDatasetRowExtendedWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowsExtended;
import static marquez.db.models.DbModelGenerator.newDatasourceRowWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRowWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.DatasourceDao;
import marquez.db.NamespaceDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.DatasourceRow;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.DatasetMapper;
import marquez.service.models.Dataset;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetServiceTest {
  private static final int LIMIT = 100;
  private static final int OFFSET = 0;

  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceName DATASOURCE_NAME = newDatasourceName();
  private static final DatasourceUrn DATASOURCE_URN =
      DatasourceUrn.from(CONNECTION_URL, DATASOURCE_NAME);
  private static final DatasetName DATASET_NAME = newDatasetName();
  private static final DatasetUrn DATASET_URN = DatasetUrn.from(DATASOURCE_NAME, DATASET_NAME);
  private static final Description DESCRIPTION = newDescription();
  final Dataset NEW_DATASET =
      Dataset.builder()
          .name(DATASET_NAME)
          .datasourceUrn(DATASOURCE_URN)
          .description(DESCRIPTION)
          .build();

  private NamespaceDao namespaceDao;
  private DatasourceDao datasourceDao;
  private DatasetDao datasetDao;
  private DatasetService datasetService;

  @Before
  public void setUp() {
    namespaceDao = mock(NamespaceDao.class);
    datasourceDao = mock(DatasourceDao.class);
    datasetDao = mock(DatasetDao.class);
    datasetService = new DatasetService(namespaceDao, datasourceDao, datasetDao);
  }

  @Test
  public void testNewDatasetService_throwsException_onNullNamespaceDao() {
    final NamespaceDao nullNamespaceDao = null;
    assertThatNullPointerException()
        .isThrownBy(() -> new DatasetService(nullNamespaceDao, datasourceDao, datasetDao));
  }

  @Test
  public void testNewDatasetService_throwsException_onNullDatasourceDao() {
    final DatasourceDao nullDatasourceDao = null;
    assertThatNullPointerException()
        .isThrownBy(() -> new DatasetService(namespaceDao, nullDatasourceDao, datasetDao));
  }

  @Test
  public void testNewDatasetService_throwsException_onNullDatasetDao() {
    final DatasetDao nullDatasetDao = null;
    assertThatNullPointerException()
        .isThrownBy(() -> new DatasetService(namespaceDao, datasourceDao, nullDatasetDao));
  }

  @Test
  public void testCreate_success() throws MarquezServiceException {
    final NamespaceRow namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    final DatasourceRow datasourceRow = newDatasourceRowWith(DATASOURCE_NAME, DATASOURCE_URN);
    when(namespaceDao.findBy(NAMESPACE_NAME)).thenReturn(Optional.of(namespaceRow));
    when(datasourceDao.findBy(DATASOURCE_URN)).thenReturn(Optional.of(datasourceRow));

    final DatasetRow datasetRow =
        DatasetRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(Instant.now())
            .namespaceUuid(namespaceRow.getUuid())
            .datasourceUuid(datasourceRow.getUuid())
            .name(NAMESPACE_NAME.getValue())
            .urn(DATASET_URN.getValue())
            .description(DESCRIPTION.getValue())
            .build();
    when(datasetDao.insertAndGet(any(DatasetRow.class))).thenReturn(Optional.of(datasetRow));

    final Dataset expected = DatasetMapper.map(DATASOURCE_URN, datasetRow);
    final Dataset actual = datasetService.create(NAMESPACE_NAME, NEW_DATASET);
    assertThat(actual).isEqualTo(expected);

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME);
    verify(datasourceDao, times(1)).findBy(DATASOURCE_URN);
    verify(datasetDao, times(1)).insertAndGet(any(DatasetRow.class));
  }

  @Test
  public void testCreate_failed() throws MarquezServiceException {
    final NamespaceRow namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    final DatasourceRow datasourceRow = newDatasourceRowWith(DATASOURCE_NAME, DATASOURCE_URN);
    when(namespaceDao.findBy(NAMESPACE_NAME)).thenReturn(Optional.of(namespaceRow));
    when(datasourceDao.findBy(DATASOURCE_URN)).thenReturn(Optional.of(datasourceRow));

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.create(NAMESPACE_NAME, NEW_DATASET));

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME);
    verify(datasourceDao, times(1)).findBy(DATASOURCE_URN);
    verify(datasetDao, times(1)).insertAndGet(any(DatasetRow.class));
  }

  @Test
  public void testCreate_throwsException_onDbError() throws MarquezServiceException {
    final NamespaceRow namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    final DatasourceRow datasourceRow = newDatasourceRowWith(DATASOURCE_NAME, DATASOURCE_URN);
    when(namespaceDao.findBy(NAMESPACE_NAME)).thenReturn(Optional.of(namespaceRow));
    when(datasourceDao.findBy(DATASOURCE_URN)).thenReturn(Optional.of(datasourceRow));
    when(datasetDao.insertAndGet(any(DatasetRow.class)))
        .thenThrow(UnableToExecuteStatementException.class);

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.create(NAMESPACE_NAME, NEW_DATASET));

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME);
    verify(datasourceDao, times(1)).findBy(DATASOURCE_URN);
    verify(datasetDao, times(1)).insertAndGet(any(DatasetRow.class));
  }

  @Test
  public void testCreate_throwsException_onNamespaceNotFound() throws MarquezServiceException {
    when(namespaceDao.findBy(NAMESPACE_NAME)).thenReturn(Optional.empty());

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.create(NAMESPACE_NAME, NEW_DATASET));

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME);
    verify(datasourceDao, never()).findBy(DATASOURCE_URN);
    verify(datasetDao, never()).insertAndGet(any(DatasetRow.class));
  }

  @Test
  public void testCreate_throwsException_onDatasourceNotFound() throws MarquezServiceException {
    final NamespaceRow namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    when(namespaceDao.findBy(NAMESPACE_NAME)).thenReturn(Optional.of(namespaceRow));
    when(datasourceDao.findBy(DATASOURCE_URN)).thenReturn(Optional.empty());

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.create(NAMESPACE_NAME, NEW_DATASET));

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME);
    verify(datasourceDao, times(1)).findBy(DATASOURCE_URN);
    verify(datasetDao, never()).insertAndGet(any(DatasetRow.class));
  }

  @Test
  public void testExists() throws MarquezServiceException {
    when(datasetDao.exists(DATASET_URN)).thenReturn(true);

    final boolean exists = datasetService.exists(DATASET_URN);
    assertThat(exists).isTrue();

    verify(datasetDao, times(1)).exists(DATASET_URN);
  }

  @Test
  public void testExists_throwsException_onDbError() {
    when(datasetDao.exists(DATASET_URN)).thenThrow(UnableToExecuteStatementException.class);

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.exists(DATASET_URN));

    verify(datasetDao, times(1)).exists(DATASET_URN);
  }

  @Test
  public void testGet() throws MarquezServiceException {
    final DatasetRowExtended datasetRowExtended =
        newDatasetRowExtendedWith(DATASET_URN, DATASOURCE_URN);
    when(datasetDao.findBy(DATASET_URN)).thenReturn(Optional.of(datasetRowExtended));

    final Dataset dataset = datasetService.get(DATASET_URN).orElse(null);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getUrn()).isEqualTo(DATASET_URN);

    verify(datasetDao, times(1)).findBy(DATASET_URN);
  }

  @Test
  public void testGet_notPresent() throws MarquezServiceException {
    when(datasetDao.findBy(DATASET_URN)).thenReturn(Optional.empty());

    assertThat(datasetService.get(DATASET_URN)).isNotPresent();
    verify(datasetDao, times(1)).findBy(DATASET_URN);
  }

  @Test
  public void testGet_throwsException_onDbError() {
    when(datasetDao.findBy(DATASET_URN)).thenThrow(UnableToExecuteStatementException.class);

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.get(DATASET_URN));

    verify(datasetDao, times(1)).findBy(DATASET_URN);
  }

  @Test
  public void testList() throws MarquezServiceException {
    final List<DatasetRowExtended> datasetRowsExtended = newDatasetRowsExtended(4);
    when(datasetDao.findAll(NAMESPACE_NAME, LIMIT, OFFSET)).thenReturn(datasetRowsExtended);

    final List<Dataset> datasets = datasetService.getAll(NAMESPACE_NAME, LIMIT, OFFSET);
    assertThat(datasets).isNotNull();
    assertThat(datasets).hasSize(4);

    verify(datasetDao, times(1)).findAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }

  @Test
  public void testList_throwsException_onDbError() throws MarquezServiceException {
    when(datasetDao.findAll(NAMESPACE_NAME, LIMIT, OFFSET))
        .thenThrow(UnableToExecuteStatementException.class);

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetService.getAll(NAMESPACE_NAME, LIMIT, OFFSET));

    verify(datasetDao, times(1)).findAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }
}
