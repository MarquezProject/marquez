package marquez.service.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetMapperTest {
  private static final DatasetUrn DATASET_URN = DatasetUrn.of("urn:a:b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();

  @Test
  public void testMapDataset() {
    final Description nonEmptyDescription = Description.of("test description");
    final DatasetRow datasetRow = newDatasetRow(nonEmptyDescription);
    final Dataset dataset = DatasetMapper.map(datasetRow);
    assertNotNull(dataset);
    assertEquals(DATASET_URN, dataset.getUrn());
    assertEquals(CREATED_AT, dataset.getCreatedAt());
    assertEquals(Optional.of(nonEmptyDescription), dataset.getDescription());
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
