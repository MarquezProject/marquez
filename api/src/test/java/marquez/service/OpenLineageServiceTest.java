package marquez.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import io.dropwizard.util.Resources;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.Utils;
import marquez.db.DatasetVersionDao;
import marquez.db.OpenLineageDao;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;

@Category({DataAccessTests.class, IntegrationTests.class})
public class OpenLineageServiceTest {
  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();
  private RunService runService;
  private OpenLineageDao openLineageDao;
  private DatasetVersionDao datasetVersionDao;
  private ArgumentCaptor<JobInputUpdate> runInputListener;
  private ArgumentCaptor<JobOutputUpdate> runOutputListener;

  @Before
  public void setup() {
    final Jdbi jdbi = dbRule.getJdbi();
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    runService = mock(RunService.class);
    runInputListener = ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(runService).notify(runInputListener.capture());
    runOutputListener = ArgumentCaptor.forClass(JobOutputUpdate.class);
    doNothing().when(runService).notify(runOutputListener.capture());
  }

  @Test
  public void testRunListener() throws IOException, ExecutionException, InterruptedException {
    OpenLineageService lineageService =
        new OpenLineageService(openLineageDao, runService, datasetVersionDao);

    LineageEvent start = getLineageEventFromResource("open_lineage/listener/1.json");
    LineageEvent end = getLineageEventFromResource("open_lineage/listener/2.json");

    lineageService.createAsync(start).get();
    lineageService.createAsync(end).get();

    assertEquals(1, runInputListener.getAllValues().size());
    assertEquals(1, runOutputListener.getAllValues().size());

    assertEquals(3, runInputListener.getAllValues().get(0).getInputs().size());
    assertEquals(2, runOutputListener.getAllValues().get(0).getOutputs().size());
  }

  private LineageEvent getLineageEventFromResource(String location) throws IOException {
    return Utils.newObjectMapper().readValue(Resources.getResource(location), LineageEvent.class);
  }
}
