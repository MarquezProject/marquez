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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import marquez.api.exceptions.ResourceException;
import marquez.api.exceptions.ResourceExceptionMapper;
import marquez.api.models.DatasourceRequest;
import marquez.api.models.DatasourceResponse;
import marquez.api.models.DatasourcesResponse;
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

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(datasourceResource)
          .addProvider(ResourceExceptionMapper.class)
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

    assertThat(returnedDatasource.getName()).isEqualTo(ds1.getDatasourceName().getValue());
    assertThat(returnedDatasource.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test
  public void testCreateDatasource() throws MarquezServiceException, ResourceException {
    final Datasource ds1 = Generator.genDatasource();

    final DatasourceRequest validRequest =
        new DatasourceRequest(
            ds1.getConnectionUrl().getRawValue(), ds1.getDatasourceName().getValue());

    when(mockDatasourceService.create(ds1.getConnectionUrl(), ds1.getDatasourceName()))
        .thenReturn(ds1);

    // When we submit it
    final Response createDatasourceResponse = datasourceResource.create(validRequest);
    assertThat(createDatasourceResponse.getStatus()).isEqualTo(OK.getStatusCode());
    final DatasourceResponse returnedDatasource =
        (DatasourceResponse) createDatasourceResponse.getEntity();

    assertThat(returnedDatasource.getName()).isEqualTo(ds1.getDatasourceName().getValue());
    assertThat(returnedDatasource.getConnectionUrl())
        .isEqualTo(ds1.getConnectionUrl().getRawValue());
  }

  @Test(expected = ResourceException.class)
  public void testInternalErrorHandling() throws MarquezServiceException, ResourceException {
    final Datasource ds1 = Generator.genDatasource();

    final DatasourceRequest validRequest =
        new DatasourceRequest(
            ds1.getConnectionUrl().getRawValue(), ds1.getDatasourceName().getValue());

    when(mockDatasourceService.create(any(), any())).thenThrow(new MarquezServiceException());
    datasourceResource.create(validRequest);
  }
}
