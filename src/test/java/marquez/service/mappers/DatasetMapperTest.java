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

import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static marquez.db.models.DbModelGenerator.newDatasetRow;
import static marquez.db.models.DbModelGenerator.newDatasetRowExtended;
import static marquez.db.models.DbModelGenerator.newDatasetRowExtendedWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowsExtended;
import static marquez.service.models.ServiceModelGenerator.newDatasetMeta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import marquez.UnitTests;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.NamespaceName;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.DatasourceRow;
import marquez.db.models.DbModelGenerator;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMapperTest {
  @Test
  public void testMap_row() {
    final DatasourceUrn datasourceUrn = newDatasourceUrn();
    final DatasetRow row = newDatasetRow();
    final Dataset dataset = DatasetMapper.map(datasourceUrn, row);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(row.getName());
    assertThat(dataset.getCreatedAt()).isEqualTo(row.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(row.getUrn());
    assertThat(dataset.getDatasourceUrn()).isEqualTo(datasourceUrn);
    assertThat(dataset.getDescription().get().getValue()).isEqualTo(row.getDescription());
  }

  @Test
  public void testMap_row_noDescription() {
    final DatasourceUrn datasourceUrn = newDatasourceUrn();
    final DatasetRow row = newDatasetRowWith(NO_DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(datasourceUrn, row);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(row.getName());
    assertThat(dataset.getCreatedAt()).isEqualTo(row.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(row.getUrn());
    assertThat(dataset.getDatasourceUrn()).isEqualTo(datasourceUrn);
    assertThat(dataset.getDescription().get().getValue()).isNull();
  }

  @Test
  public void testMap_throwsException_onNullDatasourceUrn() {
    final DatasourceUrn nullDatasourceUrn = null;
    assertThatNullPointerException()
        .isThrownBy(() -> DatasetMapper.map(nullDatasourceUrn, newDatasetRow()));
  }

  @Test
  public void testMap_throwsException_onNullRow() {
    final DatasetRow nullRow = null;
    assertThatNullPointerException()
        .isThrownBy(() -> DatasetMapper.map(newDatasourceUrn(), nullRow));
  }

  @Test
  public void testMap_rowExtended() {
    final DatasetRowExtended rowExtended = newDatasetRowExtended();
    final Dataset dataset = DatasetMapper.map(rowExtended);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(rowExtended.getName());
    assertThat(dataset.getCreatedAt()).isEqualTo(rowExtended.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(rowExtended.getUrn());
    assertThat(dataset.getDatasourceUrn().getValue()).isEqualTo(rowExtended.getDatasourceUrn());
    assertThat(dataset.getDescription().get().getValue()).isEqualTo(rowExtended.getDescription());
  }

  @Test
  public void testMap_rowExtended_noDescription() {
    final DatasetRowExtended rowExtended = newDatasetRowExtendedWith(NO_DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(rowExtended);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(rowExtended.getName());
    assertThat(dataset.getCreatedAt()).isEqualTo(rowExtended.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(rowExtended.getUrn());
    assertThat(dataset.getDatasourceUrn().getValue()).isEqualTo(rowExtended.getDatasourceUrn());
    assertThat(dataset.getDescription().get().getValue()).isNull();
  }

  @Test
  public void testMap_throwsException_onNullRowExtended() {
    final DatasetRowExtended nullRowExtended = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMapper.map(nullRowExtended));
  }

  @Test
  public void testMap_rowsExtended() {
    final List<DatasetRowExtended> datasetRowsExtended = newDatasetRowsExtended(4);
    final List<Dataset> datasets = DatasetMapper.map(datasetRowsExtended);
    assertThat(datasets).isNotNull();
    assertThat(datasets).hasSize(4);
  }

  @Test
  public void testMap_emptyRowsExtended() {
    final List<DatasetRowExtended> datasetRowsExtended = newDatasetRowsExtended(0);
    final List<Dataset> datasets = DatasetMapper.map(datasetRowsExtended);
    assertThat(datasets).isNotNull();
    assertThat(datasets).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullRowsExtended() {
    final List<DatasetRowExtended> nullRowsExtended = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMapper.map(nullRowsExtended));
  }

  @Test(expected = NullPointerException.class)
  public void testDataSetRowMapper_nullNameSpaceRow() {
    final NamespaceRow namespaceRow = null;
    final DatasourceRow dataSourceRow = DbModelGenerator.newDatasourceRow();
    final DatasetMeta meta = newDatasetMeta();
    DatasetRowMapper.map(namespaceRow, dataSourceRow, meta);
  }

  @Test(expected = NullPointerException.class)
  public void testDataSetRowMapper_nullDataSourceRow() {
    final NamespaceRow namespaceRow = DbModelGenerator.newNamespaceRowWith(NamespaceName.of("a"));
    final DatasourceRow dataSourceRow = null;
    final DatasetMeta meta = newDatasetMeta();
    DatasetRowMapper.map(namespaceRow, dataSourceRow, meta);
  }

  @Test(expected = NullPointerException.class)
  public void testDataSetRowMapper_nullDataset() {
    final NamespaceRow namespaceRow = DbModelGenerator.newNamespaceRowWith(NamespaceName.of("a"));
    final DatasourceRow dataSourceRow = DbModelGenerator.newDatasourceRow();
    final DatasetMeta nullMeta = null;
    DatasetRowMapper.map(namespaceRow, dataSourceRow, nullMeta);
  }

  @Test
  public void testDataSetRowMapper_normalTest_NoDescription() {
    final NamespaceRow namespaceRow = DbModelGenerator.newNamespaceRowWith(NamespaceName.of("a"));
    final DatasourceRow dataSourceRow = DbModelGenerator.newDatasourceRow();
    final DatasetMeta meta = newDatasetMeta(false);
    DatasetRow dr = DatasetRowMapper.map(namespaceRow, dataSourceRow, meta);
    DatasourceName datasourceName = DatasourceName.of(dataSourceRow.getName());
    DatasetUrn datasetUrn = DatasetUrn.of(datasourceName, meta.getName());
    assertEquals(dataSourceRow.getUuid(), dr.getDatasourceUuid());
    assertEquals(meta.getName().getValue(), dr.getName());
    assertEquals(namespaceRow.getUuid(), dr.getNamespaceUuid());
    assertEquals(datasetUrn.getValue(), dr.getUrn());
    assertNull(dr.getDescription());
    assertThat(dr.getUuid()).isNotNull();
  }

  @Test
  public void testDataSetRowMapper_normalTest_WithDescription() {
    final NamespaceRow namespaceRow = DbModelGenerator.newNamespaceRowWith(NamespaceName.of("a"));
    final DatasourceRow dataSourceRow = DbModelGenerator.newDatasourceRow();
    final DatasetMeta meta = newDatasetMeta();
    DatasetRow dr = DatasetRowMapper.map(namespaceRow, dataSourceRow, meta);
    DatasourceName datasourceName = DatasourceName.of(dataSourceRow.getName());
    DatasetUrn datasetUrn = DatasetUrn.of(datasourceName, meta.getName());
    assertEquals(dataSourceRow.getUuid(), dr.getDatasourceUuid());
    assertEquals(meta.getName().getValue(), dr.getName());
    assertEquals(namespaceRow.getUuid(), dr.getNamespaceUuid());
    assertEquals(datasetUrn.getValue(), dr.getUrn());
    assertEquals(meta.getDescription().get().getValue(), dr.getDescription());
    assertThat(dr.getUuid()).isNotNull();
  }
}
