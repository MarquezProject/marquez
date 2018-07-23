package marquez;

import static io.dropwizard.testing.FixtureHelpers.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import marquez.core.Job;
import org.junit.Assert;
import org.junit.Test;

public class TestJobSerialization {
  static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  static final Job JOB =
      new Job(
          "first-best-job",
          new Timestamp(1532038468530L),
          new Timestamp(1532038468530L),
          1,
          2,
          new Timestamp(1532036468530L),
          Boolean.TRUE,
          "Best job. First job.");

  @Test
  public void testSerialization() throws Exception {
    final String expected =
        MAPPER.writeValueAsString(MAPPER.readValue(fixture("fixtures/job.json"), Job.class));
    Assert.assertEquals(MAPPER.writeValueAsString(JOB), expected);
  }

  @Test
  public void testDeserialization() throws Exception {
    final Job resultJob =
        MAPPER.readValue(fixture("fixtures/job.json"), Job.class);
    Assert.assertEquals(JOB, resultJob);
  }
}
