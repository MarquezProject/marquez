package marquez.client.models;

import static marquez.client.models.ModelGenerator.newJobRuns;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRunsTest {
  @Test
  public void testNamespaces() {
    final List<JobRun> jobRunsList = newJobRuns(3);
    final JobRuns jobRuns = new JobRuns(jobRunsList);

    assertThat(jobRuns.getRuns().size()).isEqualTo(3);
    assertThat(jobRuns.getRuns()).isEqualTo(jobRunsList);
  }
}
