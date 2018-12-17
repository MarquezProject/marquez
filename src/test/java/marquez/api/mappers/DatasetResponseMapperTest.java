package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Optional;
import marquez.api.models.DatasetResponse;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetResponseMapperTest {
  private static final DatasetResponseMapper DATASET_RESPONSE_MAPPER = new DatasetResponseMapper();
  private static final DatasetUrn DATASET_URN = DatasetUrn.of("urn:a:b.c");
  private static final Instant CREATED_AT = Instant.now();

  @Test
  public void testMapDatasetResponse() {
    final Description nonEmptyDescription = Description.of("test description");
    final Optional<String> nonEmptyDescriptionString = Optional.of(nonEmptyDescription.getValue());
    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, nonEmptyDescription);
    final DatasetResponse datasetResponse = DATASET_RESPONSE_MAPPER.map(dataset);
    assertNotNull(datasetResponse);
    assertEquals(DATASET_URN.getValue(), datasetResponse.getUrn());
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(nonEmptyDescriptionString, datasetResponse.getDescription());
  }

  @Test
  public void testMapDatasetResponseNoDescription() {
    final Optional<String> noDescriptionString = Optional.of(NO_DESCRIPTION.getValue());
    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, NO_DESCRIPTION);
    final DatasetResponse datasetResponse = DATASET_RESPONSE_MAPPER.map(dataset);
    assertNotNull(datasetResponse);
    assertEquals(DATASET_URN.getValue(), datasetResponse.getUrn());
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(noDescriptionString, datasetResponse.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapDatasetResponseNullDataset() {
    final Dataset nullDataset = null;
    DATASET_RESPONSE_MAPPER.map(nullDataset);
    ;
  }
}
