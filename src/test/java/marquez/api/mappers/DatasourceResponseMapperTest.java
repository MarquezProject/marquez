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

package marquez.api.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import marquez.api.models.DatasourceResponse;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.service.models.Datasource;
import org.junit.Test;

public class DatasourceResponseMapperTest {
  private static final DatasourceName NAME = DatasourceName.fromString("datasource_name");
  private static final Instant CREATED_AT = Instant.now();

  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists_");

  private static final DatasourceUrn DATASOURCE_URN = DatasourceUrn.from(CONNECTION_URL, NAME);
  private static final Datasource DATASOURCE =
      new Datasource(NAME, CREATED_AT, DATASOURCE_URN, CONNECTION_URL);

  @Test
  public void testMapDatasource() {

    final DatasourceResponse datasourceResponse = DatasourceResponseMapper.map(DATASOURCE);
    assertNotNull(datasourceResponse);
    assertEquals(NAME.getValue(), datasourceResponse.getName());
    assertEquals(CREATED_AT.toString(), datasourceResponse.getCreatedAt());
    assertEquals(DATASOURCE_URN.getValue(), datasourceResponse.getUrn());
    assertEquals(CONNECTION_URL.getRawValue(), datasourceResponse.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasource() {
    final Datasource nulldatasource = null;
    DatasourceResponseMapper.map(nulldatasource);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceResponseNameMissing() {
    final DatasourceName nullDatasourceName = null;

    final Datasource nullNameDatasource = mock(Datasource.class);
    when(nullNameDatasource.getConnectionUrl()).thenReturn(CONNECTION_URL);
    when(nullNameDatasource.getUrn()).thenReturn(DATASOURCE_URN);
    when(nullNameDatasource.getCreatedAt()).thenReturn(CREATED_AT);

    when(nullNameDatasource.getName()).thenReturn(nullDatasourceName);

    DatasourceResponseMapper.map(nullNameDatasource);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceResponseUrnMissing() {
    final DatasourceUrn nullDatasourceUrn = null;

    final Datasource nullNameDatasource = mock(Datasource.class);
    when(nullNameDatasource.getName()).thenReturn(NAME);
    when(nullNameDatasource.getConnectionUrl()).thenReturn(CONNECTION_URL);
    when(nullNameDatasource.getCreatedAt()).thenReturn(CREATED_AT);

    when(nullNameDatasource.getUrn()).thenReturn(nullDatasourceUrn);

    DatasourceResponseMapper.map(nullNameDatasource);
  }

  @Test
  public void testMapDatasourceList() {
    final List<Datasource> datasources = Arrays.asList(DATASOURCE);
    final List<DatasourceResponse> datasourceResponses = DatasourceResponseMapper.map(datasources);
    assertNotNull(datasourceResponses);
    assertEquals(1, datasourceResponses.size());
  }

  @Test
  public void testMapEmptyDatasourceList() {
    final List<Datasource> datasources = Arrays.asList();
    final List<DatasourceResponse> datasourceResponses = DatasourceResponseMapper.map(datasources);
    assertNotNull(datasourceResponses);
    assertEquals(0, datasourceResponses.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasourceList() {
    final List<Datasource> datasources = null;
    DatasourceResponseMapper.map(datasources);
  }
}
