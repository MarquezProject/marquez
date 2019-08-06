package marquez.client.models;

import static marquez.client.models.ModelGenerator.newRunArgs;
import static marquez.client.models.ModelGenerator.newRunId;
import static marquez.client.models.RunState.NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRunTest {
  private static final String RUN_ID = newRunId();
  private static final Instant NOMINAL_START_TIME = Instant.now();
  private static final Instant NOMINAL_END_TIME = Instant.from(NOMINAL_START_TIME);
  private static final String RUN_ARGS = newRunArgs();
  private static final RunState RUN_STATE = NEW;

  @Test
  public void testJobRun() {
    final JobRun jobRun =
        new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

    assertThat(jobRun.getRunId()).isEqualTo(RUN_ID);
    assertThat(jobRun.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRun.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRun.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
    assertThat(jobRun.getRunState()).isEqualTo(RUN_STATE);
  }

  @Test
  public void testJobRun_noRunId() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new JobRun(null, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);
            });
  }

  @Test
  public void testJobRun_blankRunId() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new JobRun(" ", NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);
            });
  }

  @Test
  public void testJobRun_noNominalStartTime() {
    final JobRun jobRun = new JobRun(RUN_ID, null, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

    assertThat(jobRun.getRunId()).isEqualTo(RUN_ID);
    assertThat(jobRun.getNominalStartTime()).isEqualTo(Optional.ofNullable(null));
    assertThat(jobRun.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRun.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
    assertThat(jobRun.getRunState()).isEqualTo(RUN_STATE);
  }

  @Test
  public void testJobRun_noNominalEndTime() {
    final JobRun jobRun = new JobRun(RUN_ID, NOMINAL_START_TIME, null, RUN_ARGS, RUN_STATE);

    assertThat(jobRun.getRunId()).isEqualTo(RUN_ID);
    assertThat(jobRun.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRun.getNominalEndTime()).isEqualTo(Optional.ofNullable(null));
    assertThat(jobRun.getRunArgs()).isEqualTo(Optional.ofNullable(RUN_ARGS));
    assertThat(jobRun.getRunState()).isEqualTo(RUN_STATE);
  }

  @Test
  public void testJobRun_noRunArgs() {
    final JobRun jobRun = new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, null, RUN_STATE);

    assertThat(jobRun.getRunId()).isEqualTo(RUN_ID);
    assertThat(jobRun.getNominalStartTime()).isEqualTo(Optional.ofNullable(NOMINAL_START_TIME));
    assertThat(jobRun.getNominalEndTime()).isEqualTo(Optional.ofNullable(NOMINAL_END_TIME));
    assertThat(jobRun.getRunArgs()).isEqualTo(Optional.ofNullable(null));
    assertThat(jobRun.getRunState()).isEqualTo(RUN_STATE);
  }

  @Test
  public void testJobRun_noRunState() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, null);
            });
  }
}
