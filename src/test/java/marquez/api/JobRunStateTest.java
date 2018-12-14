package marquez.api;

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

public class JobRunStateTest {

  private static final UUID JOB_RUN_STATE_UUID = UUID.randomUUID();
  private static final Timestamp TRANSITIONED_AT_TIME = Timestamp.from(Instant.now());
  private static final String STATE = "NEW";

  private static final UUID JOB_RUN_UUID = UUID.randomUUID();

  private static final JobRunState JOB_RUN_STATE =
      new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, STATE);

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @Test
  public void testGuidNotSerialized() throws Exception {
    final String serializedOutput = MAPPER.writeValueAsString(JOB_RUN_STATE);
    assertThat(serializedOutput).doesNotContain("guid");
    assertThat(serializedOutput).doesNotContain(JOB_RUN_STATE_UUID.toString());
  }

  @Test
  public void testJobRunStateEquality() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, STATE);
    AssertionsForClassTypes.assertThat(JOB_RUN_STATE.equals(JOB_RUN_STATE));
    AssertionsForClassTypes.assertThat(JOB_RUN_STATE.equals(jrs2));
    AssertionsForClassTypes.assertThat(jrs2.equals(JOB_RUN_STATE));
  }

  @Test
  public void testHashCodeEquality() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, STATE);
    assertEquals(JOB_RUN_STATE.hashCode(), jrs2.hashCode());
  }

  @Test
  public void testJobRunStateInequalityOnUUID() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, STATE);
    AssertionsForClassTypes.assertThat(!JOB_RUN_STATE.equals(jrs2));
    AssertionsForClassTypes.assertThat(JOB_RUN_STATE.equals(JOB_RUN_STATE));
  }

  @Test
  public void testJobRunStateInequalityOnNonIDField() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, "COMPLETED");
    AssertionsForClassTypes.assertThat(!JOB_RUN_STATE.equals(jrs2));
  }

  @Test
  public void testJobRunStateHashcodeInequality() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, "COMPLETED");
    assertNotEquals(JOB_RUN_STATE.hashCode(), jrs2.hashCode());
  }

  @Test
  public void testJobRunStateHashcodeInequalityOnNonIdField() {
    JobRunState jrs2 = new JobRunState(TRANSITIONED_AT_TIME, JOB_RUN_UUID, "COMPLETED");
    assertNotEquals(JOB_RUN_STATE.hashCode(), jrs2.hashCode());
  }
}
