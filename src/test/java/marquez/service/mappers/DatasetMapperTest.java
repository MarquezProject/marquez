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
  private static final Description NON_EMPTY_DESCRIPTION = Description.of("test description");
  private static final DatasetUrn DATASET_URN = DatasetUrn.of("urn:a:b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();

  @Test
  public void testMapDataset() {
    final DatasetRow datasetRow = newDatasetRow(NON_EMPTY_DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(datasetRow);
    assertNotNull(dataset);
    assertEquals(DATASET_URN, dataset.getUrn());
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(Optional.of(NON_EMPTY_DESCRIPTION), dataset.getDescription());
  }

  @Test
  public void testMapDatasetNoDescription() {
    final DatasetRow datasetRow = newDatasetRow(NO_DESCRIPTION);
    final Dataset dataset = DatasetMapper.map(datasetRow);
    assertNotNull(dataset);
    assertEquals(DATASET_URN, dataset.getUrn());
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(Optional.of(NO_DESCRIPTION), dataset.getDescription());
  }

  @Test
  public void testMapDatasetList() {
    final List<DatasetRow> datasetiRows = Arrays.asList(newDatasetRow(NON_EMPTY_DESCRIPTION));
    final List<Dataset> datasetRows = DatasetMapper.map(datasetiRows);
    assertNotNull(datasetRows);
    assertEquals(1, datasetRows.size());
  }

  @Test
  public void testMapEmptyDatasetList() {
    final List<DatasetRow> emptyDatasetRows = Arrays.asList();
    final List<Dataset> datasets = DatasetMapper.map(emptyDatasetRows);
    assertNotNull(datasets);
    assertEquals(0, datasets.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapDatasetNullDatasetRow() {
    final DatasetRow nullDatasetRow = null;
    DatasetMapper.map(nullDatasetRow);
  }

  private DatasetRow newDatasetRow(Description description) {
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(UUID.randomUUID())
        .dataSourceUuid(UUID.randomUUID())
        .urn(DATASET_URN)
        .description(description)
        .currentVersion(UUID.randomUUID())
        .build();
  }
}
