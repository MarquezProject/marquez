package marquez;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import marquez.core.Job;
import org.junit.Test;
import static io.dropwizard.testing.FixtureHelpers.fixture;

import java.sql.Timestamp;

public class TestJobSerialization {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    public Job createSimepleJob() {
        return new Job("first-best-job", new Timestamp(1532038468530L), new Timestamp(1532038468530L),
                1, 2, new Timestamp(1532036468530L), Boolean.TRUE,
                "Best job. First job.");

    }

    @Test
    public void testSerialization() throws Exception {
        final Job job = new Job("first-best-job",,,"1", "juliet.hougland",
                "", "True", "Best job. First job.");
        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/job.json"),
                        Job.class));
        assertThat(MAPPER.writeValueAsString(job)).isEqualTo(expected);
    }

    @Test
    public void testDeserialization() throws Exception {
        final Job expectedJob = createSimpleJob();
        final Job resultJob = MAPPER.readValue(fixture("fixtures/specified_transformation.json"),
                Job.class);
        assertThat(expectedJob).isEqualTo(resultJob);
    }
}
