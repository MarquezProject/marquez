package marquez.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import marquez.db.dao.JobDAO;
import org.junit.ClassRule;

import static org.mockito.Mockito.mock;

class JobResourceTest {
  private static final JobDAO JOB_DAO = mock(JobDAO.class);

  @ClassRule
  public static final ResourceTestRule JOB_RESOURCE =
      ResourceTestRule.builder().addResource(new JobResource(JOB_DAO)).build();
}
