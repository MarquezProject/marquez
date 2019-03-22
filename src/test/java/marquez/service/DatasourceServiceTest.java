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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.DatasourceDao;
import marquez.db.models.DatasourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Datasource;
import marquez.service.models.Generator;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatasourceServiceTest {

  private static DatasourceService datasourceService;

  private static final DatasourceDao datasourceDao = mock(DatasourceDao.class);

  @BeforeClass
  public static void setUp() {
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

  @Test(expected = NullPointerException.class)
  public void testCreateDatasource_throwsException_onNullConnectionUrl()
      throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();

    datasourceService.create(null, DatasourceName.fromString(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testCreateDatasource_throwsException_onNullDatasourceName()
      throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();

    datasourceService.create(ConnectionUrl.fromString(row.getConnectionUrl()), null);
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreateDatasource_throwsException_onDaoException() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class))).thenReturn(Optional.empty());
    datasourceService.create(
        ConnectionUrl.fromString(row.getConnectionUrl()), DatasourceName.fromString(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceService_throwsException_onNullDatasourceDao() {
    new DatasourceService(null);
  }

  @Test
  public void testGetDatasourceByUrn() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class))).thenReturn(Optional.of(row));

    final Optional<Datasource> response =
        datasourceService.get(
            DatasourceUrn.from(
                ConnectionUrl.fromString(row.getConnectionUrl()),
                DatasourceName.fromString(row.getName())));
    assertThat(response.isPresent()).isTrue();

    assertThat(response.get().getConnectionUrl())
        .isEqualTo(ConnectionUrl.fromString(row.getConnectionUrl()));
    assertThat(response.get().getName()).isEqualTo(DatasourceName.fromString(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testGetDatasourceByUrn_throwsNpe_onNullInput() throws MarquezServiceException {
    final DatasourceUrn myNullDatasourceUrn = null;
    datasourceService.get(myNullDatasourceUrn);
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetDatasourceByUrn_throwMarquezServiceException_onDaoException()
      throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    datasourceService.get(
        DatasourceUrn.from(
            ConnectionUrl.fromString(row.getConnectionUrl()),
            DatasourceName.fromString(row.getName())));
  }

  @Test
  public void testGetAll_multipleResults() {
    final List<DatasourceRow> datasourceRowList = new ArrayList();
    datasourceRowList.add(Generator.genDatasourceRow());
    datasourceRowList.add(Generator.genDatasourceRow());

    when(datasourceDao.findAll(any(), any())).thenReturn(datasourceRowList);

    final List<Datasource> datasources = datasourceService.getAll(100, 0);

    assertThat(datasources.size()).isEqualTo(datasourceRowList.size());
    assertThat(datasources.get(0).getUrn()).isNotEqualTo(datasources.get(1).getUrn());
  }

  @Test
  public void testGetAll_singleResult() {
    final DatasourceRow generatedDatasourceRow = Generator.genDatasourceRow();
    final List<DatasourceRow> datasourceRowList = Collections.singletonList(generatedDatasourceRow);

    when(datasourceDao.findAll(any(), any())).thenReturn(datasourceRowList);

    final List<Datasource> datasources = datasourceService.getAll(100, 0);
    assertThat(datasources.size()).isEqualTo(datasourceRowList.size());

    final Datasource returnedDatasource = datasources.get(0);

    assertThat(returnedDatasource.getUrn().getValue()).isEqualTo(generatedDatasourceRow.getUrn());
    assertThat(returnedDatasource.getConnectionUrl().getRawValue())
        .isEqualTo(generatedDatasourceRow.getConnectionUrl());
    assertThat(returnedDatasource.getName().getValue()).isEqualTo(generatedDatasourceRow.getName());
  }

  @Test
  public void testGetAll_noResults() {
    final List<DatasourceRow> datasourceRowList = Collections.emptyList();

    when(datasourceDao.findAll(any(), any())).thenReturn(datasourceRowList);

    final List<Datasource> datasources = datasourceService.getAll(100, 0);
    assertThat(datasources.size()).isEqualTo(datasourceRowList.size());
  }
}
