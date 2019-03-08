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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Optional;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceType;
import marquez.common.models.DatasourceUrn;
import marquez.db.DatasourceDao;
import marquez.db.models.DatasourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Datasource;
import marquez.service.models.Generator;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatasourceServiceTest {

  private static DatasourceService datasourceService;

  private static final DatasourceDao datasourceDao = mock(DatasourceDao.class);

  @Before
  public void setUp() {
    datasourceService = new DatasourceService(datasourceDao);
  }

  @After
  public void tearDown() {
    reset(datasourceDao);
  }

  @Test
  public void testCreateDatasource() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class))).thenReturn(Optional.of(row));

    final Datasource response =
        datasourceService.create(
            ConnectionUrl.fromString(row.getConnectionUrl()),
            DatasourceName.fromString(row.getName()));
    assertThat(response.getConnectionUrl())
        .isEqualTo(ConnectionUrl.fromString(row.getConnectionUrl()));

    assertThat(response.getName()).isEqualTo(DatasourceName.fromString(row.getName()));
  }

  @Test
  public void testGetDatasource() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class))).thenReturn(Optional.of(row));

    final DatasourceType type =
        ConnectionUrl.fromString(row.getConnectionUrl()).getDatasourceType();
    final Optional<Datasource> response =
        datasourceService.get(DatasourceUrn.from(type.toString(), row.getName()));
    assertThat(response.isPresent()).isTrue();

    assertThat(response.get().getConnectionUrl())
        .isEqualTo(ConnectionUrl.fromString(row.getConnectionUrl()));
    assertThat(response.get().getName()).isEqualTo(DatasourceName.fromString(row.getName()));
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetDatasourceError() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    final DatasourceType type =
        ConnectionUrl.fromString(row.getConnectionUrl()).getDatasourceType();

    datasourceService.get(DatasourceUrn.from(type.toString(), row.getName()));
  }

  @Test
  public void testGetNoSuchDatasource() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class))).thenReturn(Optional.empty());
    final Optional<Datasource> response = datasourceService.get(row.getUuid());
    assertThat(response.isPresent()).isFalse();
  }
}
