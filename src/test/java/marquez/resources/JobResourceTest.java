package marquez.resources;

import static org.mockito.Mockito.mock;

import io.dropwizard.testing.junit.ResourceTestRule;
import marquez.db.dao.JobDAO;
import org.junit.ClassRule;

class JobResourceTest {
  private static final JobDAO JOB_DAO = mock(JobDAO.class);

  @ClassRule
  public static final ResourceTestRule JOB_RESOURCE =
      ResourceTestRule.builder().addResource(new JobResource(JOB_DAO)).build();
}
