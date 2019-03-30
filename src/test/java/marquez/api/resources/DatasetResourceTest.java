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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static marquez.api.models.ApiModelGenerator.newDatasetRequest;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrnWith;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.service.models.ServiceModelGenerator.newDataset;
import static marquez.service.models.ServiceModelGenerator.newDatasets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.mappers.DatasetMapper;
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
import marquez.service.DatasourceService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.Datasource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetResourceTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static final int LIMIT = 100;
  private static final int OFFSET = 0;

  private static final DatasourceName DATASOURCE_NAME = newDatasourceName();
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceUrn DATASOURCE_URN = newDatasourceUrn();
  private static final Datasource DATASOURCE = null;

  private static final DatasetName DATASET_NAME = newDatasetName();
  private static final DatasetUrn DATASET_URN = newDatasetUrnWith(DATASOURCE_NAME, DATASET_NAME);
  private static final Description DESCRIPTION = newDescription();
  private static final Dataset DATASET =
      Dataset.builder()
          .name(DATASET_NAME)
          .createdAt(Instant.now())
          .urn(DATASET_URN)
          .description(DESCRIPTION)
          .build();

  private final NamespaceService namespaceService = mock(NamespaceService.class);
  private final DatasetService datasetService = mock(DatasetService.class);
  private final DatasourceService datasourceService = mock(DatasourceService.class);
  private final DatasetResource datasetResource =
      new DatasetResource(namespaceService, datasourceService, datasetService);

  public void testNewDatasetResource_throwsException_onNullNamespaceService() {
    final NamespaceService nullNamespaceService = null;
    assertThatNullPointerException()
        .isThrownBy(
            () -> new DatasetResource(nullNamespaceService, datasourceService, datasetService));
  }

  public void testNewDatasetResource_throwsException_onNullDatasourceService() {
    final DatasourceService nullDatasourceService = null;
    assertThatNullPointerException()
        .isThrownBy(
            () -> new DatasetResource(namespaceService, nullDatasourceService, datasetService));
  }

  public void testNewDatasetResource_throwsException_onNullDatasetService() {
    final DatasetService nullDatasetService = null;
    assertThatNullPointerException()
        .isThrownBy(
            () -> new DatasetResource(namespaceService, datasourceService, nullDatasetService));
  }

  @Test
  public void testCreate() throws MarquezServiceException {
    final DatasetRequest request = new DatasetRequest(DATASET_NAME, DATASOURCE_URN, DESCRIPTION);
    final Dataset newDataset = DatasetMapper.map(request);
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(datasourceService.get(DATASOURCE_URN)).thenReturn(Optional.of(DATASOURCE));
    when(datasetService.create(NAMESPACE_NAME, DATASOURCE_NAME, DATASOURCE_URN, newDataset))
        .thenReturn(DATASET);

    final Response httpResponse = datasetResource.create(NAMESPACE_NAME, request);
    assertThat(httpResponse.getStatusInfo()).isEqualTo(OK);
    assertThat(httpResponse.getMediaType()).isEqualTo(APPLICATION_JSON);

    final DatasetResponse expected = DatasetResponseMapper.map(DATASET);
    final DatasetResponse actual = (DatasetResponse) httpResponse.getEntity();
    assertThat(actual).isEqualTo(expected);
  }

  public void testCreate_throwsException_onNamespaceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(false);
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.create(newNamespaceName(), newDatasetRequest()));
  }

  public void testCreate_throwsException_onNullNamespaceName() throws MarquezServiceException {
    final NamespaceName nullNamespaceName = null;
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.create(nullNamespaceName, newDatasetRequest()));
  }

  public void testCreate_throwsException_onNullDatasetRequest() throws MarquezServiceException {
    final DatasetRequest nullDatasetRequest = null;
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.create(newNamespaceName(), nullDatasetRequest));
  }

  @Test
  public void testList() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);

    final List<Dataset> datasets = newDatasets(5);
    when(datasetService.getAll(NAMESPACE_NAME, LIMIT, OFFSET)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE_NAME, LIMIT, OFFSET);
    assertThat(response.getStatusInfo()).isEqualTo(response.getStatusInfo());

    final DatasetsResponse expected = DatasetResponseMapper.toDatasetsResponse(datasets);
    final DatasetsResponse actual = (DatasetsResponse) response.getEntity();
    assertThat(actual).isEqualTo(expected);

    verify(datasetService, times(1)).getAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }

  @Test(expected = NamespaceNotFoundException.class)
  public void testList_throwsException_onNamespaceDoesNotExist() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(false);
    datasetResource.list(NAMESPACE_NAME, LIMIT, OFFSET);
  }

  public void testList_throwsException_onNullNamespaceName() throws MarquezServiceException {
    final NamespaceName nullNamespaceName = null;
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.list(nullNamespaceName, LIMIT, OFFSET));
  }

  public void testList_throwsException_onNullLimit() throws MarquezServiceException {
    final Integer nullLimit = null;
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.list(NAMESPACE_NAME, nullLimit, OFFSET));
  }

  public void testList_throwsException_onNullOffset() throws MarquezServiceException {
    final Integer nullOffset = null;
    assertThatNullPointerException()
        .isThrownBy(() -> datasetResource.list(NAMESPACE_NAME, LIMIT, nullOffset));
  }
}
