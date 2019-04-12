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

import static marquez.service.models.ServiceModelGenerator.newDataset;
import static marquez.service.models.ServiceModelGenerator.newDatasets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import marquez.UnitTests;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetResponseMapperTest {
  @Test
  public void testMap_dataset() {
    final Dataset dataset = newDataset();
    final DatasetResponse response = DatasetResponseMapper.map(dataset);
    assertThat(response).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(response.getName());
    assertThat(dataset.getCreatedAt().toString()).isEqualTo(response.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(response.getUrn());
    assertThat(dataset.getDescription().getValue())
        .isEqualTo(response.getDescription().orElse(null));
  }

  @Test
  public void testMap_dataset_noDescription() {
    final Dataset dataset = newDataset(false);
    final DatasetResponse response = DatasetResponseMapper.map(dataset);
    assertThat(response).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(response.getName());
    assertThat(dataset.getCreatedAt().toString()).isEqualTo(response.getCreatedAt());
    assertThat(dataset.getUrn().getValue()).isEqualTo(response.getUrn());
    assertThat(Optional.ofNullable(dataset.getDescription())).isEqualTo(response.getDescription());
  }

  @Test
  public void testMap_throwsException_onNullDataset() {
    final Dataset nullDataset = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetResponseMapper.map(nullDataset));
  }

  @Test
  public void testMap_datasets() {
    final List<Dataset> datasets = newDatasets(4);
    final List<DatasetResponse> responses = DatasetResponseMapper.map(datasets);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_emptyDatasets() {
    final List<Dataset> emptyDatasets = Collections.emptyList();
    final List<DatasetResponse> emptyResponses = DatasetResponseMapper.map(emptyDatasets);
    assertThat(emptyResponses).isNotNull();
    assertThat(emptyResponses).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullDatasets() {
    final List<Dataset> nullDatasets = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetResponseMapper.map(nullDatasets));
  }

  @Test
  public void testToDatasetsResponse() {
    final List<Dataset> datasets = newDatasets(4);
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(datasets);
    assertThat(response).isNotNull();
    assertThat(response.getDatasets()).hasSize(4);
  }

  @Test
  public void testToDatasetsResponse_emptyDatasets() {
    final List<Dataset> emptyDatasets = Collections.emptyList();
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(emptyDatasets);
    assertThat(response).isNotNull();
    assertThat(response.getDatasets()).isEmpty();
  }

  @Test
  public void testToDatasetsResponse_throwsException_onNullDatasets() {
    final List<Dataset> nullDatasets = null;
    assertThatNullPointerException()
        .isThrownBy(() -> DatasetResponseMapper.toDatasetsResponse(nullDatasets));
  }
}
