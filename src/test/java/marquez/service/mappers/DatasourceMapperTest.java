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

import static marquez.db.models.DbModelGenerator.newDatasourceRow;
import static marquez.db.models.DbModelGenerator.newDatasourceRows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import marquez.UnitTests;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Datasource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceMapperTest {
  @Test
  public void testMap_row() {
    final DatasourceRow row = newDatasourceRow();
    final Datasource datasource = DatasourceMapper.map(row);
    assertThat(datasource).isNotNull();
    assertThat(row.getName()).isEqualTo(datasource.getName().getValue());
    assertThat(row.getCreatedAt()).isEqualTo(datasource.getCreatedAt().toString());
    assertThat(row.getUrn()).isEqualTo(datasource.getUrn().getValue());
    assertThat(row.getConnectionUrl()).isEqualTo(datasource.getConnectionUrl().getRawValue());
  }

  @Test
  public void testMap_throwsException_onNullRow() {
    final DatasourceRow nullRow = null;
    assertThatNullPointerException().isThrownBy(() -> DatasourceMapper.map(nullRow));
  }

  @Test
  public void testMap_rows() {
    final List<DatasourceRow> rows = newDatasourceRows(4);
    final List<Datasource> datasources = DatasourceMapper.map(rows);
    assertThat(datasources).isNotNull();
    assertThat(datasources).hasSize(4);
  }

  @Test
  public void testMap_emptyRows() {
    final List<DatasourceRow> emptyRows = Collections.emptyList();
    final List<Datasource> emptyDatasources = DatasourceMapper.map(emptyRows);
    assertThat(emptyDatasources).isNotNull();
    assertThat(emptyDatasources).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullRows() {
    final List<DatasourceRow> nullRows = null;
    assertThatNullPointerException().isThrownBy(() -> DatasourceMapper.map(nullRows));
  }
}
