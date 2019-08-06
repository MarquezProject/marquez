package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasetUrns;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newJobName;
import static marquez.client.models.ModelGenerator.newLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobTest {
  private static final String JOB_NAME = newJobName();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.from(CREATED_AT);
  private static final List<String> INPUT_DATASET_URNS = newDatasetUrns(3);
  private static final List<String> OUTPUT_DATASET_URNS = newDatasetUrns(4);
  private static final String LOCATION = newLocation();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testJob() {
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            DESCRIPTION);

    assertThat(job.getName()).isEqualTo(JOB_NAME);
    assertThat(job.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(job.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(job.getInputDatasetUrns()).isEqualTo(INPUT_DATASET_URNS);
    assertThat(job.getOutputDatasetUrns()).isEqualTo(OUTPUT_DATASET_URNS);
    assertThat(job.getLocation()).isEqualTo(LOCATION);
    assertThat(job.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testJob_noJobName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  null,
                  CREATED_AT,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_blankJobName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Job(
                  " ",
                  CREATED_AT,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noCreatedAt() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  null,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noUpdatedAt() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  CREATED_AT,
                  null,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noInputDatasetUrns() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  CREATED_AT,
                  UPDATED_AT,
                  null,
                  OUTPUT_DATASET_URNS,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noOutputDatasetUrns() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  CREATED_AT,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  null,
                  LOCATION,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noLocation() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  CREATED_AT,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  null,
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_blankLocation() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Job(
                  JOB_NAME,
                  CREATED_AT,
                  UPDATED_AT,
                  INPUT_DATASET_URNS,
                  OUTPUT_DATASET_URNS,
                  " ",
                  DESCRIPTION);
            });
  }

  @Test
  public void testJob_noDescription() {
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            null);

    assertThat(job.getName()).isEqualTo(JOB_NAME);
    assertThat(job.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(job.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(job.getInputDatasetUrns()).isEqualTo(INPUT_DATASET_URNS);
    assertThat(job.getOutputDatasetUrns()).isEqualTo(OUTPUT_DATASET_URNS);
    assertThat(job.getLocation()).isEqualTo(LOCATION);
    assertThat(job.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
