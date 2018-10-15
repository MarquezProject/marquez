package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;

public class JobVersionTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static UUID guid = UUID.randomUUID();
  private static UUID jobGuid = UUID.randomUUID();
  private static UUID version = UUID.randomUUID();
  private static UUID latestJobRunGuid = UUID.randomUUID();
  private static String uri = "http://foo.bar";

  private static final JobVersion jobVersion = buildFixture();

  private static JobVersion buildFixture() {
    return new JobVersion(
        guid,
        jobGuid,
        uri,
        version,
        latestJobRunGuid,
        new Timestamp(new Date(0).getTime()),
        new Timestamp(new Date(0).getTime()));
  }

  private static JobVersion buildRandom() {
    return new JobVersion(
        UUID.randomUUID(),
        UUID.randomUUID(),
        String.format("http://%d.bar", new Random().nextInt()),
        UUID.randomUUID(),
        UUID.randomUUID(),
        new Timestamp(new Date(0).getTime()),
        new Timestamp(new Date(0).getTime()));
  }

  @Test
  public void testEquals() {
    assertThat(jobVersion).isEqualTo(buildFixture());
    assertThat(buildFixture()).isEqualTo(jobVersion);
    assertThat(jobVersion).isNotEqualTo(buildRandom());
  }

  @Test
  public void testHashCode() {
    assertThat(jobVersion.hashCode()).isEqualTo(buildFixture().hashCode());
    assertThat(jobVersion.hashCode()).isNotEqualTo(buildRandom().hashCode());
  }
}
