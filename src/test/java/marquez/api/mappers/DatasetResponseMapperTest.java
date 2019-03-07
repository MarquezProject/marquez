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

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import marquez.UnitTests;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetResponseMapperTest {
  private static final DatasetName NAME = DatasetName.fromString("b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final DatasetUrn URN =
      DatasetUrn.fromString(String.format("urn:a:%s", NAME.getValue()));
  private static final Description DESCRIPTION = Description.fromString("test description");
  private static final Dataset DATASET =
      Dataset.builder().name(NAME).createdAt(CREATED_AT).urn(URN).description(DESCRIPTION).build();

  @Test
  public void testMap_dataset() {
    final Optional<String> expectedDescription = Optional.of(DESCRIPTION.getValue());

    final DatasetResponse response = DatasetResponseMapper.map(DATASET);
    assertNotNull(response);
    assertEquals(NAME.getValue(), response.getName());
    assertEquals(CREATED_AT.toString(), response.getCreatedAt());
    assertEquals(URN.getValue(), response.getUrn());
    assertEquals(expectedDescription, response.getDescription());
  }

  @Test
  public void testMap_datasetWithNoDescription() {
    final Optional<String> noDescription = Optional.of(NO_DESCRIPTION.getValue());

    final Dataset dataset = Dataset.builder().name(NAME).createdAt(CREATED_AT).urn(URN).build();
    final DatasetResponse response = DatasetResponseMapper.map(dataset);
    assertNotNull(response);
    assertEquals(NAME.getValue(), response.getName());
    assertEquals(URN.getValue(), response.getUrn());
    assertEquals(CREATED_AT.toString(), response.getCreatedAt());
    assertEquals(noDescription, response.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDataset() {
    final Dataset nullDataset = null;
    DatasetResponseMapper.map(nullDataset);
  }

  @Test
  public void testMap_datasets() {
    final List<Dataset> datasets = Arrays.asList(DATASET);
    final List<DatasetResponse> responses = DatasetResponseMapper.map(datasets);
    assertNotNull(responses);
    assertEquals(1, responses.size());
  }

  @Test
  public void testMap_emptyDatasets() {
    final List<Dataset> emptyDatasets = Arrays.asList();
    final List<DatasetResponse> responses = DatasetResponseMapper.map(emptyDatasets);
    assertNotNull(responses);
    assertTrue(responses.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDatasets() {
    final List<Dataset> nullDatasets = null;
    DatasetResponseMapper.map(nullDatasets);
  }

  @Test
  public void testToDatasetsResponse() {
    final List<Dataset> datasets = Arrays.asList(DATASET);
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(datasets);
    assertNotNull(response);
    assertEquals(1, response.getDatasets().size());
  }

  @Test
  public void testToDatasetsResponse_emptyDatasets() {
    final List<Dataset> emptyDatasets = Arrays.asList();
    final DatasetsResponse response = DatasetResponseMapper.toDatasetsResponse(emptyDatasets);
    assertNotNull(response);
    assertTrue(response.getDatasets().isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void testToDatasetsResponse_throwsException_onNullDatasets() {
    final List<Dataset> nullDatasets = null;
    DatasetResponseMapper.toDatasetsResponse(nullDatasets);
  }
}
