package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasetName;
import static marquez.client.models.ModelGenerator.newDatasourceUrn;
import static marquez.client.models.ModelGenerator.newDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMetaTest {
  private static final String DATASET_NAME = newDatasetName();
  private static final String DATASOURCE_URN = newDatasourceUrn();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testDatasetMeta() {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();

    assertThat(datasetMeta.getName()).isEqualTo(DATASET_NAME);
    assertThat(datasetMeta.getDatasourceUrn()).isEqualTo(DATASOURCE_URN);
    assertThat(datasetMeta.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testDatasetMeta_noDatasetName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              DatasetMeta.builder().datasourceUrn(DATASOURCE_URN).description(DESCRIPTION).build();
            });
  }

  @Test
  public void testDatasetMeta_blankDatasetName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              DatasetMeta.builder()
                  .name(" ")
                  .datasourceUrn(DATASOURCE_URN)
                  .description(DESCRIPTION)
                  .build();
            });
  }

  @Test
  public void testDatasetMeta_noDatasourceUrn() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              DatasetMeta.builder().name(DATASET_NAME).description(DESCRIPTION).build();
            });
  }

  @Test
  public void testDatasetMeta_blankDatasourceUrn() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              DatasetMeta.builder()
                  .name(DATASET_NAME)
                  .datasourceUrn(" ")
                  .description(DESCRIPTION)
                  .build();
            });
  }

  @Test
  public void testDatasetMeta_noDescription() {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder().name(DATASET_NAME).datasourceUrn(DATASOURCE_URN).build();

    assertThat(datasetMeta.getName()).isEqualTo(DATASET_NAME);
    assertThat(datasetMeta.getDatasourceUrn()).isEqualTo(DATASOURCE_URN);
    assertThat(datasetMeta.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
