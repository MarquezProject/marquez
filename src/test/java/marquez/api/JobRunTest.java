package marquez.api;

import static marquez.api.JobRunState.State.toInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

public class JobRunTest {

  private static final UUID JOB_RUN_UUID = UUID.randomUUID();
  private static final Timestamp CREATED_AT_TIME = Timestamp.from(Instant.now());
  private static final Timestamp STARTED_AT_TIME = Timestamp.from(Instant.now());
  private static final Timestamp ENDED_AT_TIME = Timestamp.from(Instant.now());
  private static final UUID JOB_RUN_DEFINITION_UUID = UUID.randomUUID();
  private static final Integer CURRENT_STATE = toInt(JobRunState.State.NEW);

  private static final JobRun JOB_RUN =
      new JobRun(
          JOB_RUN_UUID,
          CREATED_AT_TIME,
          STARTED_AT_TIME,
          ENDED_AT_TIME,
          JOB_RUN_DEFINITION_UUID,
          CURRENT_STATE);

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @Test
  public void testGuidNotSerialized() throws Exception {
    final String serializedOutput = MAPPER.writeValueAsString(JOB_RUN);
    assertThat(serializedOutput).doesNotContain("guid");
    assertThat(serializedOutput).doesNotContain(JOB_RUN_UUID.toString());
  }

  @Test
  public void testGuidSet() {
    assertThat(JOB_RUN.getGuid().equals(JOB_RUN_UUID));
  }

  @Test
  public void testJobRunEquality() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            CURRENT_STATE);
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(JOB_RUN));
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(jr2));
    AssertionsForClassTypes.assertThat(jr2.equals(JOB_RUN));
  }

  @Test
  public void testHashCodeEquality() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            CURRENT_STATE);
    assertEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunInequalityOnUUID() {
    JobRun jr2 =
        new JobRun(
            UUID.randomUUID(),
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            CURRENT_STATE);
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(JOB_RUN));
  }

  @Test
  public void testJobRunInequalityOnNonIDField() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            toInt(JobRunState.State.FINISHED));
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
  }

  @Test
  public void testJobRunHashcodeInequality() {
    JobRun jr2 =
        new JobRun(
            UUID.randomUUID(),
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            CURRENT_STATE);
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunHashcodeInequalityOnNonIdField() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_RUN_DEFINITION_UUID,
            toInt(JobRunState.State.FINISHED));
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }
}
