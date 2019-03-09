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

import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import marquez.UnitTests;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetResourceTest {
  private static final String LOCATION_PATH = "/namespaces/{namespace}/datasets/{urn}";

  private static final NamespaceName NAMESPACE_NAME = NamespaceName.fromString("test");
  private static final DatasourceUrn DATASOURCE_URN =
      DatasourceUrn.fromString("urn:datasource:a:b");

  private static final DatasetName NAME = DatasetName.fromString("b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final DatasetUrn URN =
      DatasetUrn.fromString(String.format("urn:dataset:a:%s", NAME.getValue()));
  private static final Description DESCRIPTION = Description.fromString("test description");
  private static final Dataset DATASET =
      Dataset.builder().name(NAME).createdAt(CREATED_AT).urn(URN).description(DESCRIPTION).build();

  private static final int LIMIT = 100;
  private static final int OFFSET = 0;

  private final NamespaceService namespaceService = mock(NamespaceService.class);
  private final DatasetService datasetService = mock(DatasetService.class);
  private final DatasetResource datasetResource =
      new DatasetResource(namespaceService, datasetService);

  @Test(expected = NullPointerException.class)
  public void testNewDatasetResource_throwsException_onNullNamespaceService() {
    final NamespaceService nullNamespaceService = null;
    new DatasetResource(nullNamespaceService, datasetService);
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasetResource_throwsException_onNullDatasetService() {
    final DatasetService nullDatasetService = null;
    new DatasetResource(namespaceService, nullDatasetService);
  }

  @Test
  public void testCreate() throws MarquezServiceException {
    final URI expectedLocation =
        UriBuilder.fromPath(LOCATION_PATH).build(NAMESPACE_NAME.getValue(), DATASET.getUrn());

    final Optional<String> expectedDescription = Optional.of(DESCRIPTION.getValue());

    when(namespaceService.exists(NAMESPACE_NAME.getValue())).thenReturn(true);
    when(datasetService.create(NAMESPACE_NAME, NAME, DATASOURCE_URN, DESCRIPTION))
        .thenReturn(DATASET);

    final DatasetRequest datasetRequest =
        new DatasetRequest(NAME.getValue(), DATASOURCE_URN.getValue(), DESCRIPTION.getValue());

    final Response response = datasetResource.create(NAMESPACE_NAME, datasetRequest);
    assertEquals(CREATED, response.getStatusInfo());
    assertTrue(response.getHeaders().containsKey(LOCATION));
    assertEquals(expectedLocation, URI.create(response.getHeaderString(LOCATION)));

    final DatasetResponse datasetResponse = (DatasetResponse) response.getEntity();
    assertEquals(NAME.getValue(), datasetResponse.getName());
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(URN.getValue(), datasetResponse.getUrn());
    assertEquals(expectedDescription, datasetResponse.getDescription());
  }

  @Test
  public void testGet() throws MarquezServiceException {
    final Optional<String> expectedDescription = Optional.of(DESCRIPTION.getValue());

    when(namespaceService.exists(NAMESPACE_NAME.getValue())).thenReturn(true);
    when(datasetService.get(URN)).thenReturn(Optional.of(DATASET));

    final Response response = datasetResource.get(NAMESPACE_NAME, URN);
    assertEquals(OK, response.getStatusInfo());

    final DatasetResponse datasetResponse = (DatasetResponse) response.getEntity();
    assertEquals(NAME.getValue(), datasetResponse.getName());
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(URN.getValue(), datasetResponse.getUrn());
    assertEquals(expectedDescription, datasetResponse.getDescription());
  }

  @Test
  public void testList() throws MarquezServiceException {
    final Optional<String> expectedDescription = Optional.of(DESCRIPTION.getValue());

    when(namespaceService.exists(NAMESPACE_NAME.getValue())).thenReturn(true);

    final List<Dataset> datasets = Arrays.asList(DATASET);
    when(datasetService.getAll(NAMESPACE_NAME, LIMIT, OFFSET)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE_NAME, LIMIT, OFFSET);
    assertEquals(OK, response.getStatusInfo());

    final DatasetsResponse datasetsResponse = (DatasetsResponse) response.getEntity();
    final List<DatasetResponse> datasetsResponses = datasetsResponse.getDatasets();
    assertEquals(1, datasetsResponses.size());
    assertEquals(NAME.getValue(), datasetsResponses.get(0).getName());
    assertEquals(CREATED_AT.toString(), datasetsResponses.get(0).getCreatedAt());
    assertEquals(URN.getValue(), datasetsResponses.get(0).getUrn());
    assertEquals(expectedDescription, datasetsResponses.get(0).getDescription());

    verify(datasetService, times(1)).getAll(NAMESPACE_NAME, LIMIT, OFFSET);
  }

  @Test(expected = NamespaceNotFoundException.class)
  public void testList_throwsException_onNamespaceNotFound() throws MarquezServiceException {
    when(namespaceService.exists(NAMESPACE_NAME.getValue())).thenReturn(false);
    datasetResource.list(NAMESPACE_NAME, LIMIT, OFFSET);
  }
}
