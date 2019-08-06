package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasetUrns;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobMetaTest {
  private static final List<String> INPUT_DATASET_URNS = newDatasetUrns(3);
  private static final List<String> OUTPUT_DATASET_URNS = newDatasetUrns(4);
  private static final String LOCATION = newLocation();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testJobMeta() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThat(jobMeta.getInputDatasetUrns()).isEqualTo(INPUT_DATASET_URNS);
    assertThat(jobMeta.getOutputDatasetUrns()).isEqualTo(OUTPUT_DATASET_URNS);
    assertThat(jobMeta.getLocation()).isEqualTo(LOCATION);
    assertThat(jobMeta.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testJobMeta_noInputDatasetUrns() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThat(jobMeta.getInputDatasetUrns()).isEqualTo(Collections.emptyList());
    assertThat(jobMeta.getOutputDatasetUrns()).isEqualTo(OUTPUT_DATASET_URNS);
    assertThat(jobMeta.getLocation()).isEqualTo(LOCATION);
    assertThat(jobMeta.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testJobMeta_noOutputDatasetUrns() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThat(jobMeta.getInputDatasetUrns()).isEqualTo(INPUT_DATASET_URNS);
    assertThat(jobMeta.getOutputDatasetUrns()).isEqualTo(Collections.emptyList());
    assertThat(jobMeta.getLocation()).isEqualTo(LOCATION);
    assertThat(jobMeta.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testJobMeta_noLocation() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                JobMeta.builder()
                    .inputDatasetUrns(INPUT_DATASET_URNS)
                    .outputDatasetUrns(OUTPUT_DATASET_URNS)
                    .description(DESCRIPTION)
                    .build());
  }

  @Test
  public void testJobMeta_blankLocation() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                JobMeta.builder()
                    .inputDatasetUrns(INPUT_DATASET_URNS)
                    .outputDatasetUrns(OUTPUT_DATASET_URNS)
                    .location(" ")
                    .description(DESCRIPTION)
                    .build());
  }

  @Test
  public void testJobMeta_noDescription() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .build();

    assertThat(jobMeta.getInputDatasetUrns()).isEqualTo(INPUT_DATASET_URNS);
    assertThat(jobMeta.getOutputDatasetUrns()).isEqualTo(OUTPUT_DATASET_URNS);
    assertThat(jobMeta.getLocation()).isEqualTo(LOCATION);
    assertThat(jobMeta.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
