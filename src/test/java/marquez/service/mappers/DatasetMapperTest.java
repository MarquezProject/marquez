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
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMapperTest {
  private static final DatasetName NAME = DatasetName.fromString("b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final DatasetUrn URN =
      DatasetUrn.fromString(String.format("urn:dataset:a:%s", NAME.getValue()));
  private static final Description DESCRIPTION = Description.fromString("test description");
  private static final DatasetRow ROW =
      DatasetRow.builder()
          .uuid(UUID.randomUUID())
          .name(NAME.getValue())
          .createdAt(CREATED_AT)
          .updatedAt(UPDATED_AT)
          .namespaceUuid(UUID.randomUUID())
          .datasourceUuid(UUID.randomUUID())
          .urn(URN.getValue())
          .description(DESCRIPTION.getValue())
          .currentVersionUuid(UUID.randomUUID())
          .build();

  @Test
  public void testMap_row() {
    final Dataset dataset = DatasetMapper.map(ROW);
    assertNotNull(dataset);
    assertEquals(NAME, dataset.getName());
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(URN, dataset.getUrn());
    assertEquals(DESCRIPTION, dataset.getDescription());
  }

  @Test
  public void testMap_row_noDescription() {
    final DatasetRow rowWithNoDescription =
        DatasetRow.builder()
            .uuid(UUID.randomUUID())
            .name(NAME.getValue())
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .namespaceUuid(UUID.randomUUID())
            .datasourceUuid(UUID.randomUUID())
            .urn(URN.getValue())
            .currentVersionUuid(UUID.randomUUID())
            .build();

    final Dataset dataset = DatasetMapper.map(rowWithNoDescription);
    assertNotNull(dataset);
    assertEquals(NAME, dataset.getName());
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(URN, dataset.getUrn());
    assertEquals(NO_DESCRIPTION, dataset.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullRow() {
    final DatasetRow nullRow = null;
    DatasetMapper.map(nullRow);
  }

  @Test
  public void testMap_rows() {
    final List<DatasetRow> datasetRows = Arrays.asList(ROW);
    final List<Dataset> datasets = DatasetMapper.map(datasetRows);
    assertNotNull(datasets);
    assertEquals(1, datasets.size());
  }

  @Test
  public void testMap_emptyRows() {
    final List<DatasetRow> emptyRows = Arrays.asList();
    final List<Dataset> datasets = DatasetMapper.map(emptyRows);
    assertNotNull(datasets);
    assertTrue(datasets.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullRows() {
    final List<DatasetRow> nullRows = null;
    DatasetMapper.map(nullRows);
  }
}
