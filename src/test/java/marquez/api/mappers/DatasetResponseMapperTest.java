package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
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
    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, nonEmptyDescription);
    final DatasetResponse datasetResponse = DATASET_RESPONSE_MAPPER.map(dataset);
    assertNotNull(datasetResponse);
    assertEquals(DATASET_URN, datasetResponse.getUrn());
    assertEquals(CREATED_AT, datasetResponse.getUrn());
    assertEquals(nonEmptyDescription, datasetResponse.getUrn());
  }

  @Test
  public void testMapDatasetResponseNoDescription() {
    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, NO_DESCRIPTION);
    final DatasetResponse datasetResponse = DATASET_RESPONSE_MAPPER.map(dataset);
    assertNotNull(datasetResponse);
    assertEquals(DATASET_URN, datasetResponse.getUrn());
    assertEquals(CREATED_AT, datasetResponse.getUrn());
    assertEquals(NO_DESCRIPTION, datasetResponse.getUrn());
  }
}
