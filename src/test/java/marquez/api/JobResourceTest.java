package marquez.api;

import marquez.UnitTests;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class JobResourceTest {
  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceService namespaceService;
  @Mock private JobService jobService;
  private JobResource jobResource;

  @Before
  public void setUp() {
    jobResource = new JobResource(namespaceService, jobService);
  }
}
