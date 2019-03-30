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

import static marquez.service.models.ServiceModelGenerator.newDatasource;
import static marquez.service.models.ServiceModelGenerator.newDatasources;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import marquez.UnitTests;
import marquez.api.models.DatasourceResponse;
import marquez.api.models.DatasourcesResponse;
import marquez.service.models.Datasource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceResponseMapperTest {
  @Test
  public void testMap_datasource() {
    final Datasource datasource = newDatasource();
    final DatasourceResponse response = DatasourceResponseMapper.map(datasource);
    assertThat(response).isNotNull();
    assertThat(datasource.getName().getValue()).isEqualTo(response.getName());
    assertThat(datasource.getCreatedAt().toString()).isEqualTo(response.getCreatedAt());
    assertThat(datasource.getUrn().getValue()).isEqualTo(response.getUrn());
    assertThat(datasource.getConnectionUrl().getRawValue()).isEqualTo(response.getConnectionUrl());
  }

  @Test
  public void testMap_throwsException_onNullDatasource() {
    final Datasource nulldatasource = null;
    assertThatNullPointerException().isThrownBy(() -> DatasourceResponseMapper.map(nulldatasource));
  }

  @Test
  public void testMap_datasources() {
    final List<Datasource> datasources = newDatasources(4);
    final List<DatasourceResponse> responses = DatasourceResponseMapper.map(datasources);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_throwsException_onNullDatasources() {
    final List<Datasource> datasources = null;
    assertThatNullPointerException().isThrownBy(() -> DatasourceResponseMapper.map(datasources));
  }

  @Test
  public void testToDatasourcesResponse() {
    final List<Datasource> datasources = newDatasources(4);
    final DatasourcesResponse response =
        DatasourceResponseMapper.toDatasourcesResponse(datasources);
    assertThat(response).isNotNull();
    assertThat(response.getDatasources()).hasSize(4);
  }

  @Test
  public void testToDatasourcesResponse_emptyDatasources() {
    final List<Datasource> emptyDatasources = Collections.emptyList();
    final DatasourcesResponse response =
        DatasourceResponseMapper.toDatasourcesResponse(emptyDatasources);
    assertThat(response).isNotNull();
    assertThat(response.getDatasources()).isEmpty();
  }

  @Test
  public void testToDatasourcesResponse_throwsException_onNullDatasources() {
    final List<Datasource> datasources = null;
    assertThatNullPointerException()
        .isThrownBy(() -> DatasourceResponseMapper.toDatasourcesResponse(datasources));
  }
}
