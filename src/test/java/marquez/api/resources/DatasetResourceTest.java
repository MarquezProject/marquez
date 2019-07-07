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

package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.service.models.ServiceModelGenerator.newDatasetWith;
import static marquez.service.models.ServiceModelGenerator.newDatasets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.api.exceptions.DatasetUrnNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.DatasetResponseMapper;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

@Category(UnitTests.class)
public class DatasetResourceTest {
  private static final int LIMIT = 100;
  private static final int OFFSET = 0;

  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceName DATASOURCE_NAME = newDatasourceName();
  private static final DatasourceUrn DATASOURCE_URN =
      DatasourceUrn.of(CONNECTION_URL, DATASOURCE_NAME);
  private static final DatasetName DATASET_NAME = newDatasetName();
  private static final DatasetUrn DATASET_URN = DatasetUrn.of(DATASOURCE_NAME, DATASET_NAME);
  private static final Description DESCRIPTION = newDescription();
  private static final Dataset DATASET = newDatasetWith(DATASET_NAME, DATASET_URN, DESCRIPTION);
  private static final DatasetRequest DATASET_REQUEST =
      new DatasetRequest(
          DATASET_NAME.getValue(), DATASOURCE_URN.getValue(), DESCRIPTION.getValue());
  private static final DatasetMeta DATASET_META =
      DatasetMeta.builder()
          .name(DATASET_NAME)
          .datasourceUrn(DATASOURCE_URN)
          .description(DESCRIPTION)
          .build();

  @Rule public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
  @Mock private NamespaceService namespaceService;
  @Mock private DatasetService datasetService;
  private DatasetResource datasetResource;

  @Before
  public void setUp() {
    datasetResource = new DatasetResource(namespaceService, datasetService);
  }

  @Test
  public void testNewDatasetResource_throwsException_onNullNamespaceService() {
    final NamespaceService nullNamespaceService = null;
    assertThatNullPointerException()
        .isThrownBy(() -> new DatasetResource(nullNamespaceService, datasetService));
  }

  @Test
  public void testNewDatasetResource_throwsException_onNullDatasetService() {
    final DatasetService nullDatasetService = null;
    assertThatNullPointerException()
        .isThrownBy(() -> new DatasetResource(namespaceService, nullDatasetService));
  }

  @Test
  public void testCreate() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(datasetService.create(NAMESPACE_NAME, DATASET_META)).thenReturn(DATASET);

    final Response response = datasetResource.create(NAMESPACE_NAME.getValue(), DATASET_REQUEST);
    assertThat(response.getStatusInfo()).isEqualTo(OK);

    final DatasetResponse expected = DatasetResponseMapper.map(DATASET);
    final DatasetResponse actual = (DatasetResponse) response.getEntity();
    assertThat(actual).isEqualTo(expected);

    verify(datasetService, times(1)).create(NAMESPACE_NAME, DATASET_META);
  }

  @Test
  public void testCreate_throwsException_onNamespaceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(false);

    assertThatExceptionOfType(NamespaceNotFoundException.class)
        .isThrownBy(() -> datasetResource.create(NAMESPACE_NAME.getValue(), DATASET_REQUEST));

    verify(datasetService, never()).create(NAMESPACE_NAME, DATASET_META);
  }

  @Test
  public void testCreate_throwsException_onDatasourceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(datasetService.create(NAMESPACE_NAME, DATASET_META))
        .thenThrow(MarquezServiceException.class);

    assertThatExceptionOfType(MarquezServiceException.class)
        .isThrownBy(() -> datasetResource.create(NAMESPACE_NAME.getValue(), DATASET_REQUEST));

    verify(datasetService, times(1)).create(NAMESPACE_NAME, DATASET_META);
  }

  @Test
  public void testGet() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(datasetService.get(DATASET_URN)).thenReturn(Optional.of(DATASET));

    final Response response =
        datasetResource.get(NAMESPACE_NAME.getValue(), DATASET_URN.getValue());
    assertThat(response.getStatusInfo()).isEqualTo(OK);

    final DatasetResponse expected = DatasetResponseMapper.map(DATASET);
    final DatasetResponse actual = (DatasetResponse) response.getEntity();
    assertThat(actual).isEqualTo(expected);

    verify(datasetService, times(1)).get(DATASET_URN);
  }

  @Test
  public void testGet_throwsException_onNamespaceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(false);

    assertThatExceptionOfType(NamespaceNotFoundException.class)
        .isThrownBy(() -> datasetResource.get(NAMESPACE_NAME.getValue(), DATASET_URN.getValue()));

    verify(datasetService, never()).get(any(DatasetUrn.class));
  }

  @Test
  public void testGet_throwsException_onDatasetDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(datasetService.get(DATASET_URN)).thenReturn(Optional.empty());

    assertThatExceptionOfType(DatasetUrnNotFoundException.class)
        .isThrownBy(() -> datasetResource.get(NAMESPACE_NAME.getValue(), DATASET_URN.getValue()));

    verify(datasetService, times(1)).get(DATASET_URN);
  }

  @Test
  public void testList() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);

    final List<Dataset> datasets = newDatasets(4);
    when(datasetService.getAll(NAMESPACE_NAME, LIMIT, OFFSET)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE_NAME.getValue(), LIMIT, OFFSET);
    assertThat(response.getStatusInfo()).isEqualTo(OK);

    final DatasetsResponse expected = DatasetResponseMapper.toDatasetsResponse(datasets);
    final DatasetsResponse actual = (DatasetsResponse) response.getEntity();
    assertThat(actual).isEqualTo(expected);

    verify(datasetService, times(1)).getAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }

  @Test
  public void testList_throwsException_onNamespaceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(false);
    assertThatExceptionOfType(NamespaceNotFoundException.class)
        .isThrownBy(() -> datasetResource.list(NAMESPACE_NAME.getValue(), LIMIT, OFFSET));

    verify(datasetService, never()).getAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }
}
