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

package marquez.service.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Datasource;
import org.junit.Test;

public class DatasourceMapperTest {
  private static final Instant CREATED_AT = Instant.now();

  private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5431/novelists";
  private static final String DATASOURCE_NAME = "my_database";
  private static final String DATASOURCE_URN =
      DatasourceUrn.from(
              ConnectionUrl.fromString(CONNECTION_URL).getDatasourceType(),
              DatasourceName.fromString(DATASOURCE_NAME))
          .toString();

  @Test
  public void testMapDatasourceRow() {
    final DatasourceRow datasourceRow =
        new DatasourceRow(
            UUID.randomUUID(), DATASOURCE_URN, DATASOURCE_NAME, CONNECTION_URL, CREATED_AT);
    final Datasource datasource = DatasourceMapper.map(datasourceRow);

    assertNotNull(datasourceRow);
    assertEquals(CONNECTION_URL, datasource.getConnectionUrl().getRawValue());
    assertEquals(DATASOURCE_NAME, datasource.getName().getValue());
    assertEquals(CREATED_AT, datasource.getCreatedAt());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasourceRow() {
    final DatasourceRow nullDatasourceRow = null;
    DatasourceMapper.map(nullDatasourceRow);
  }

  @Test
  public void testMapDatasourceRowList() {
    final List<DatasourceRow> datasourceRows =
        Arrays.asList(
            new DatasourceRow(
                UUID.randomUUID(), DATASOURCE_URN, DATASOURCE_NAME, CONNECTION_URL, CREATED_AT));
    final List<Datasource> datasources = DatasourceMapper.map(datasourceRows);

    assertNotNull(datasources);
    assertEquals(1, datasources.size());
  }

  @Test
  public void testMapEmptyDatasourceRowList() {
    final List<DatasourceRow> emptyDatasourceRows = Arrays.asList();
    final List<Datasource> datasources = DatasourceMapper.map(emptyDatasourceRows);
    assertNotNull(datasources);
    assertEquals(0, datasources.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasourceRowList() {
    final List<DatasourceRow> nullDatasourceRows = null;
    DatasourceMapper.map(nullDatasourceRows);
  }
}
