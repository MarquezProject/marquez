package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class JobSerializationTest {

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final UUID TEST_JOB_UUID = UUID.randomUUID();
  private static final Job TEST_JOB =
      new Job(
          TEST_JOB_UUID,
          "first-best-job",
          "owner",
          Timestamp.from(Instant.ofEpochMilli(1532036468530L)),
          "test",
          "Best job. First job.");;

  @Test
  public void testGuidNotSerialized() throws Exception {
    final String serializedOutput = MAPPER.writeValueAsString(TEST_JOB);
    assertThat(serializedOutput).doesNotContain("guid");
    assertThat(serializedOutput).doesNotContain(TEST_JOB_UUID.toString());
  }

  @Test
  public void testGuidSet() {
    assertThat(TEST_JOB.getGuid().equals(TEST_JOB_UUID));
  }
}
