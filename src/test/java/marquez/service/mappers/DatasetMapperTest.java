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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import marquez.UnitTests;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.service.models.Dataset;
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
    assertThat(dataset.getDescription().getValue()).isEqualTo(row.getDescription());
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
    assertThat(dataset.getDescription().getValue()).isNull();
  }

  public void testMap_throwsException_onNullDatasourceUrn() {
    final DatasourceUrn nullDatasourceUrn = null;
    assertThatNullPointerException()
        .isThrownBy(() -> DatasetMapper.map(nullDatasourceUrn, newDatasetRow()));
  }

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
    assertThat(dataset.getDescription().getValue()).isEqualTo(rowExtended.getDescription());
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
    assertThat(dataset.getDescription().getValue()).isNull();
  }

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

  public void testMap_throwsException_onNullRowsExtended() {
    final List<DatasetRowExtended> nullRowsExtended = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMapper.map(nullRowsExtended));
  }
}
