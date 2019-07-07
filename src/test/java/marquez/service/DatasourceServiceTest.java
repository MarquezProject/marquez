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
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public class DatasourceServiceTest {

  private DatasourceService datasourceService;

  @Rule public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock private DatasourceDao datasourceDao;

  @Before
  public void setUp() {
    datasourceService = new DatasourceService(datasourceDao);
  }

  @Test
  public void testCreateDatasource() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.insert(any(DatasourceRow.class))).thenReturn(Optional.of(row));

    final Datasource response =
        datasourceService.create(
            ConnectionUrl.of(row.getConnectionUrl()), DatasourceName.of(row.getName()));
    assertThat(response.getConnectionUrl()).isEqualTo(ConnectionUrl.of(row.getConnectionUrl()));

    assertThat(response.getName()).isEqualTo(DatasourceName.of(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testCreateDatasource_throwsException_onNullConnectionUrl()
      throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();

    datasourceService.create(null, DatasourceName.of(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testCreateDatasource_throwsException_onNullDatasourceName()
      throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();

    datasourceService.create(ConnectionUrl.of(row.getConnectionUrl()), null);
  }

  @Test()
  public void testCreateDatasourceDuplicateName() throws MarquezServiceException {
    final DatasourceRow existingRow = Generator.genDatasourceRow();
    final String newConnectionUrl = "jdbc:postgresql://localhost:9999/different_novelists_";

    when(datasourceDao.findBy(DatasourceName.of(existingRow.getName())))
        .thenReturn(Optional.of(existingRow));
    Datasource createdDatasource =
        datasourceService.create(
            ConnectionUrl.of(newConnectionUrl), DatasourceName.of(existingRow.getName()));
    assertThat(createdDatasource.getConnectionUrl().getRawValue())
        .isEqualTo(existingRow.getConnectionUrl());
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreateDatasource_throwsException_onDaoException() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.insert(any(DatasourceRow.class))).thenReturn(Optional.empty());
    datasourceService.create(
        ConnectionUrl.of(row.getConnectionUrl()), DatasourceName.of(row.getName()));
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceService_throwsException_onNullDatasourceDao() {
    new DatasourceService(null);
  }

  @Test
  public void testGetDatasourceByUrn() throws MarquezServiceException {
    final DatasourceRow row = Generator.genDatasourceRow();
    when(datasourceDao.findBy(any(DatasourceUrn.class))).thenReturn(Optional.of(row));

    final Optional<Datasource> response =
        datasourceService.get(
            DatasourceUrn.of(
                ConnectionUrl.of(row.getConnectionUrl()), DatasourceName.of(row.getName())));
    assertThat(response.isPresent()).isTrue();

    assertThat(response.get().getConnectionUrl())
        .isEqualTo(ConnectionUrl.of(row.getConnectionUrl()));
    assertThat(response.get().getName()).isEqualTo(DatasourceName.of(row.getName()));
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
    when(datasourceDao.findBy(any(DatasourceUrn.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    datasourceService.get(
        DatasourceUrn.of(
            ConnectionUrl.of(row.getConnectionUrl()), DatasourceName.of(row.getName())));
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
