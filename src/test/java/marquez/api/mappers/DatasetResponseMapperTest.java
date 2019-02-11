package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import marquez.api.models.DatasetResponse;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetResponseMapperTest {
  private static final Instant CREATED_AT = Instant.now();
  private static final DatasetUrn DATASET_URN = DatasetUrn.fromString("urn:a:b.c");
  private static final Description DESCRIPTION = Description.fromString("test description");
  private static final Dataset DATASET = new Dataset(DATASET_URN, CREATED_AT, DESCRIPTION);

  @Test
  public void testMapDataset() {
    final Optional<String> nonEmptyDescriptionString = Optional.of(DESCRIPTION.getValue());
    final DatasetResponse datasetResponse = DatasetResponseMapper.map(DATASET);
    assertNotNull(datasetResponse);
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(DATASET_URN.getValue(), datasetResponse.getUrn());
    assertEquals(nonEmptyDescriptionString, datasetResponse.getDescription());
  }

  @Test
  public void testMapDatasetNoDescription() {
    final Optional<String> noDescriptionString = Optional.of(NO_DESCRIPTION.getValue());
    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, NO_DESCRIPTION);
    final DatasetResponse datasetResponse = DatasetResponseMapper.map(dataset);
    assertNotNull(datasetResponse);
    assertEquals(DATASET_URN.getValue(), datasetResponse.getUrn());
    assertEquals(CREATED_AT.toString(), datasetResponse.getCreatedAt());
    assertEquals(noDescriptionString, datasetResponse.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDataset() {
    final Dataset nullDataset = null;
    DatasetResponseMapper.map(nullDataset);
  }

  @Test
  public void testMapDatasetList() {
    final List<Dataset> datasets = Arrays.asList(DATASET);
    final List<DatasetResponse> datasetResponses = DatasetResponseMapper.map(datasets);
    assertNotNull(datasetResponses);
    assertEquals(1, datasetResponses.size());
  }

  @Test
  public void testMapEmptyDatasetList() {
    final List<Dataset> datasets = Arrays.asList();
    final List<DatasetResponse> datasetResponses = DatasetResponseMapper.map(datasets);
    assertNotNull(datasetResponses);
    assertEquals(0, datasetResponses.size());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDatasetList() {
    final List<Dataset> datasets = null;
    DatasetResponseMapper.map(datasets);
  }
}
