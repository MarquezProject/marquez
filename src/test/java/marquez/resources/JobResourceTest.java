package marquez.resources;

import static org.mockito.Mockito.mock;

import io.dropwizard.testing.junit.ResourceTestRule;
import marquez.core.services.JobService;
import org.junit.ClassRule;

class JobResourceTest {
  private static final JobService JOB_SERVICE = mock(JobService.class);

  @ClassRule
  public static final ResourceTestRule JOB_RESOURCE =
      ResourceTestRule.builder().addResource(new JobResource(JOB_SERVICE)).build();
}
