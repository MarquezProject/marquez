package marquez.client.models;

import static marquez.client.models.ModelGenerator.newRunArgs;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRunMetaTest {
  private static final Instant NOMINAL_START_TIME = Instant.now();
  private static final Instant NOMINAL_END_TIME = Instant.from(NOMINAL_START_TIME);
  private static final String RUN_ARGS = newRunArgs();

  @Test
  public void testJobRunMeta() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();

    assertThat(jobRunMeta.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRunMeta.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRunMeta.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
  }

  @Test
  public void testJobRunMeta_noNominalStartTime() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder().nominalEndTime(NOMINAL_END_TIME).runArgs(RUN_ARGS).build();

    assertThat(jobRunMeta.getNominalStartTime()).isEqualTo(Optional.ofNullable(null));
    assertThat(jobRunMeta.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRunMeta.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
  }

  @Test
  public void testJobRunMeta_noNominalEndTime() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder().nominalStartTime(NOMINAL_START_TIME).runArgs(RUN_ARGS).build();

    assertThat(jobRunMeta.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRunMeta.getNominalEndTime()).isEqualTo(Optional.ofNullable(null));
    assertThat(jobRunMeta.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
  }

  @Test
  public void testJobRunMeta_noRunArgs() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .build();

    assertThat(jobRunMeta.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRunMeta.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRunMeta.getRunArgs()).isEqualTo(Optional.ofNullable(null));
  }
}
