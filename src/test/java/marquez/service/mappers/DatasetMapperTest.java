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

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetMapperTest {
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final DatasetUrn DATASET_URN = DatasetUrn.fromString("urn:a:b.c");
  private static final Description DESCRIPTION = Description.fromString("test description");

  @Test
  public void testMapDatasetRow() {
    final DatasetRow datasetRow = newDatasetRow(DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(datasetRow);
    assertNotNull(dataset);
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(DATASET_URN, dataset.getUrn());
    assertEquals(Optional.of(DESCRIPTION), dataset.getDescription());
  }

  @Test
  public void testMapDatasetRowNoDescription() {
    final DatasetRow datasetRow = newDatasetRow(NO_DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(datasetRow);
    assertNotNull(dataset);
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(DATASET_URN, dataset.getUrn());
    assertEquals(Optional.of(NO_DESCRIPTION), dataset.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasetRow() {
    final DatasetRow nullDatasetRow = null;
    DatasetMapper.map(nullDatasetRow);
  }

  @Test
  public void testMapDatasetRowList() {
    final List<DatasetRow> datasetRows = Arrays.asList(newDatasetRow(DESCRIPTION));
    final List<Dataset> datasets = DatasetMapper.map(datasetRows);
    assertNotNull(datasets);
    assertEquals(1, datasets.size());
  }

  @Test
  public void testMapEmptyDatasetRowList() {
    final List<DatasetRow> emptyDatasetRows = Arrays.asList();
    final List<Dataset> datasets = DatasetMapper.map(emptyDatasetRows);
    assertNotNull(datasets);
    assertEquals(0, datasets.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasetRowList() {
    final List<DatasetRow> nullDatasetRows = null;
    DatasetMapper.map(nullDatasetRows);
  }

  private DatasetRow newDatasetRow(Description description) {
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(UUID.randomUUID())
        .dataSourceUuid(UUID.randomUUID())
        .urn(DATASET_URN.getValue())
        .description(description.getValue())
        .currentVersion(UUID.randomUUID())
        .build();
  }
}
