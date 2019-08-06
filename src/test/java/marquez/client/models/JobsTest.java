package marquez.client.models;

import static marquez.client.models.ModelGenerator.newJobs;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobsTest {

  @Test
  public void testNamespaces() {
    final List<Job> jobsList = newJobs(3);
    final Jobs jobs = new Jobs(jobsList);

    assertThat(jobs.getJobs().size()).isEqualTo(3);
    assertThat(jobs.getJobs()).isEqualTo(jobsList);
  }
}
