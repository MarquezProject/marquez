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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.api.exceptions.DatasourceUrnNotFoundException;
import marquez.api.exceptions.MarquezServiceExceptionMapper;
import marquez.api.models.DatasourceRequest;
import marquez.api.models.DatasourceResponse;
import marquez.api.models.DatasourcesResponse;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.service.DatasourceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Datasource;
import marquez.service.models.Generator;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class DatasourceResourceTest {

  private static final DatasourceService mockDatasourceService = mock(DatasourceService.class);

  private static final DatasourceResource datasourceResource =
      new DatasourceResource(mockDatasourceService);

  private static final String TEST_DATASOURCE_CONNECTION_STR =
      "jdbc:redshift://localhost:5431/finance";
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString(TEST_DATASOURCE_CONNECTION_STR);

  private static final String TEST_DATASOURCE_NAME_STR = "finance_team_mysql_server_1";
  private static final DatasourceName TEST_DATASOURCE_NAME =
      DatasourceName.fromString(TEST_DATASOURCE_NAME_STR);

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(datasourceResource)
          .addProvider(MarquezServiceExceptionMapper.class)
          .build();

  @Test(expected = NullPointerException.class)
  public void testDatasourceServiceNull() {
    final DatasourceService nullDatasourceService = null;
    new DatasourceResource(nullDatasourceService);
  }

  @Before
  public void setup() {
    reset(mockDatasourceService);
  }

  @Test
  public void testListDatasources200_emptyset() {
    when(mockDatasourceService.getAll(any(), any())).thenReturn(Collections.emptyList());
    final Response datasourceResponse = datasourceResource.list(100, 0);
    assertThat(datasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());

    final DatasourcesResponse datasourcesResponse =
        (DatasourcesResponse) datasourceResponse.getEntity();
    assertThat(datasourcesResponse.getDatasources().size()).isEqualTo(0);
  }

  @Test
  public void testListDatasources200_singleset() {
    when(mockDatasourceService.getAll(any(), any()))
        .thenReturn(Collections.singletonList(Generator.genDatasource()));
    final Response datasourceResponse = datasourceResource.list(100, 0);
    assertThat(datasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());

    final DatasourcesResponse datasourcesResponse =
        (DatasourcesResponse) datasourceResponse.getEntity();
    assertThat(datasourcesResponse.getDatasources().size()).isEqualTo(1);
  }

  @Test
  public void testListDatasources200_multipleItems() {
    final List<Datasource> resultSet = new ArrayList<>();
    resultSet.add(Generator.genDatasource());
    resultSet.add(Generator.genDatasource());

    when(mockDatasourceService.getAll(any(), any())).thenReturn(resultSet);
    final Response datasourceResponse = datasourceResource.list(100, 0);
    assertThat(datasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());

    final DatasourcesResponse datasourcesResponse =
        (DatasourcesResponse) datasourceResponse.getEntity();
    assertThat(datasourcesResponse.getDatasources().size()).isEqualTo(2);
  }

  @Test
  public void testListDatasourceResponseCorrect() {
    final Datasource ds1 = Generator.genDatasource();
    final List<Datasource> resultSet = Collections.singletonList(ds1);

    when(mockDatasourceService.getAll(any(), any())).thenReturn(resultSet);

    final Response datasourceResponse = datasourceResource.list(100, 0);
    assertThat(datasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());

    final DatasourcesResponse datasourcesResponse =
        (DatasourcesResponse) datasourceResponse.getEntity();
    assertThat(datasourcesResponse.getDatasources().size()).isEqualTo(1);
    final DatasourceResponse returnedDatasource = datasourcesResponse.getDatasources().get(0);

    assertThat(returnedDatasource.getName()).isEqualTo(ds1.getName().getValue());
    assertThat(returnedDatasource.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test
  public void testGetDatasource() throws MarquezServiceException {
    final DatasourceName datasourceName = DatasourceName.fromString("mysqlcluster");
    final ConnectionUrl connectionUrl =
        ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists_");
    final DatasourceUrn datasourceUrn = DatasourceUrn.from(connectionUrl, datasourceName);

    final Datasource ds1 =
        new Datasource(datasourceName, Instant.now(), datasourceUrn, connectionUrl);
    when(mockDatasourceService.get(datasourceUrn)).thenReturn(Optional.of(ds1));

    final Response datasourceResourceResponse = datasourceResource.get(datasourceUrn);
    assertThat(datasourceResourceResponse.getStatus()).isEqualTo(OK.getStatusCode());

    final DatasourceResponse datasourceResponse =
        (DatasourceResponse) datasourceResourceResponse.getEntity();

    assertThat(datasourceResponse.getName()).isEqualTo(ds1.getName().getValue());
    assertThat(datasourceResponse.getUrn()).isEqualTo(datasourceUrn.getValue());
    assertThat(datasourceResponse.getCreatedAt()).isNotEmpty();
    assertThat(datasourceResponse.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test(expected = DatasourceUrnNotFoundException.class)
  public void testGetNoSuchDatasource() throws MarquezServiceException {
    when(mockDatasourceService.get(any(DatasourceUrn.class))).thenReturn(Optional.empty());
    datasourceResource.get(DatasourceUrn.from(CONNECTION_URL, TEST_DATASOURCE_NAME));
  }

  @Test(expected = NullPointerException.class)
  public void testGet_throwsException_onNullDatasourceUrn() throws MarquezServiceException {
    final DatasourceUrn nullUrn = null;
    datasourceResource.get(nullUrn);
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetInternalError() throws MarquezServiceException {
    final DatasourceName datasourceName = DatasourceName.fromString("mysqlcluster");
    final ConnectionUrl connectionUrl =
        ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists_");
    final DatasourceUrn datasourceUrn = DatasourceUrn.from(connectionUrl, datasourceName);

    final Datasource ds1 =
        new Datasource(datasourceName, Instant.now(), datasourceUrn, connectionUrl);
    when(mockDatasourceService.get(ds1.getUrn())).thenThrow(MarquezServiceException.class);
    datasourceResource.get(ds1.getUrn());
  }

  @Test
  public void testCreateDatasource() throws MarquezServiceException {
    final Datasource ds1 = Generator.genDatasource();

    final DatasourceRequest validRequest =
        new DatasourceRequest(ds1.getName().getValue(), ds1.getConnectionUrl().getRawValue());

    when(mockDatasourceService.create(ds1.getConnectionUrl(), ds1.getName())).thenReturn(ds1);

    // When we submit it
    final Response createDatasourceResponse = datasourceResource.create(validRequest);
    assertThat(createDatasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());
    final DatasourceResponse returnedDatasource =
        (DatasourceResponse) createDatasourceResponse.getEntity();

    assertThat(returnedDatasource.getName()).isEqualTo(ds1.getName().getValue());
    assertThat(returnedDatasource.getUrn()).isEqualTo(ds1.getUrn().getValue());
    assertThat(returnedDatasource.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test
  public void testCreateDatasource_invalidDatasource() throws MarquezServiceException {
    final String invalidDatasourceType = "xyz_postgres_999";

    final String invalidConnectionUrl =
        "jdbc:" + invalidDatasourceType + "://localhost:5431/novelists";
    final DatasourceRequest invalidDatasourceRequest = mock(DatasourceRequest.class);
    when(invalidDatasourceRequest.getConnectionUrl()).thenReturn(invalidConnectionUrl);
    when(invalidDatasourceRequest.getName()).thenReturn("mysql_cluster_2");

    // When we submit it
    final Response createDatasourceResponse = datasourceResource.create(invalidDatasourceRequest);
    assertThat(createDatasourceResponse.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
  }

  @Test(expected = MarquezServiceException.class)
  public void testInternalErrorHandling() throws MarquezServiceException {
    final Datasource ds1 = Generator.genDatasource();

    final DatasourceRequest validRequest =
        new DatasourceRequest(ds1.getName().getValue(), ds1.getConnectionUrl().getRawValue());

    when(mockDatasourceService.create(any(), any())).thenThrow(new MarquezServiceException());
    datasourceResource.create(validRequest);
  }
}
