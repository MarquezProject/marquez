package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

public class JobRunTest {

  private static final UUID JOB_RUN_UUID = UUID.randomUUID();
  private static final Timestamp CREATED_AT_TIME = Timestamp.from(Instant.now());
  private static final UUID RUN_GUID = UUID.randomUUID();
  private static final List<String> RUN_ARGS = Collections.singletonList("--my-flag");
  private static final Timestamp STARTED_AT_TIME = Timestamp.from(Instant.now());
  private static final Timestamp ENDED_AT_TIME = Timestamp.from(Instant.now());
  private static final UUID JOB_VERSION_GUID = UUID.randomUUID();
  private static final UUID INPUT_DATA_SET_GUID = UUID.randomUUID();
  private static final UUID OUTPUT_DATA_SET_GUID = UUID.randomUUID();
  private static final Timestamp LATEST_HEARTBEAT = Timestamp.from(Instant.now());

  private static final JobRun JOB_RUN =
      new JobRun(
          JOB_RUN_UUID,
          CREATED_AT_TIME,
          RUN_GUID,
          RUN_ARGS,
          STARTED_AT_TIME,
          ENDED_AT_TIME,
          JOB_VERSION_GUID,
          INPUT_DATA_SET_GUID,
          OUTPUT_DATA_SET_GUID,
          LATEST_HEARTBEAT);

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
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_VERSION_GUID,
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
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
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_VERSION_GUID,
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
    assertEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunInequalityOnUUID() {
    JobRun jr2 =
        new JobRun(
            UUID.randomUUID(),
            CREATED_AT_TIME,
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_VERSION_GUID,
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(JOB_RUN));
  }

  @Test
  public void testJobRunInequalityOnNonIDField() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            UUID.randomUUID(),
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
  }

  @Test
  public void testJobRunHashcodeInequality() {
    JobRun jr2 =
        new JobRun(
            UUID.randomUUID(),
            CREATED_AT_TIME,
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            JOB_VERSION_GUID,
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunHashcodeInequalityOnNonIdField() {
    JobRun jr2 =
        new JobRun(
            JOB_RUN_UUID,
            CREATED_AT_TIME,
            RUN_GUID,
            RUN_ARGS,
            STARTED_AT_TIME,
            ENDED_AT_TIME,
            UUID.randomUUID(),
            INPUT_DATA_SET_GUID,
            OUTPUT_DATA_SET_GUID,
            LATEST_HEARTBEAT);
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }
}
