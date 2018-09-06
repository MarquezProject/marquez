package marquez.api;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.Date;
import java.sql.Timestamp;

import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import marquez.api.JobVersion;

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

    @Test
    public void testEquals() {
        assertThat(jobVersion).isEqualTo(buildFixture());
        assertThat(buildFixture()).isEqualTo(jobVersion);
    }

    @Test
    public void testHashCode() {
        assertThat(jobVersion.hashCode()).isEqualTo(buildFixture().hashCode());
    }
}