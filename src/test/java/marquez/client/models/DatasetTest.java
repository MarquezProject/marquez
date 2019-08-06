package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasetName;
import static marquez.client.models.ModelGenerator.newDatasetUrn;
import static marquez.client.models.ModelGenerator.newDatasourceUrn;
import static marquez.client.models.ModelGenerator.newDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetTest {
  private static final String DATASET_NAME = newDatasetName();
  private static final Instant CREATED_AT = Instant.now();
  private static final String URN = newDatasetUrn();
  private static final String DATASOURCE_URN = newDatasourceUrn();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testDataset() {
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);

    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
    assertThat(dataset.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(dataset.getUrn()).isEqualTo(URN);
    assertThat(dataset.getDatasourceUrn()).isEqualTo(DATASOURCE_URN);
    assertThat(dataset.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testDataset_noDatasetName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Dataset(null, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_blankDatasetName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Dataset(" ", CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_noCreatedAt() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Dataset(DATASET_NAME, null, URN, DATASOURCE_URN, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_noUrn() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Dataset(DATASET_NAME, CREATED_AT, null, DATASOURCE_URN, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_blankUrn() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Dataset(DATASET_NAME, CREATED_AT, " ", DATASOURCE_URN, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_noDatasourceUrn() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Dataset(DATASET_NAME, CREATED_AT, URN, null, DESCRIPTION);
            });
  }

  @Test
  public void testDataset_blankDatasourceUrn() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Dataset(DATASET_NAME, CREATED_AT, URN, " ", DESCRIPTION);
            });
  }

  @Test
  public void testDataset_noDescription() {
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, null);

    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
    assertThat(dataset.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(dataset.getUrn()).isEqualTo(URN);
    assertThat(dataset.getDatasourceUrn()).isEqualTo(DATASOURCE_URN);
    assertThat(dataset.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
