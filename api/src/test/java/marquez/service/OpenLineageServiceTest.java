package marquez.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.DatasetVersionDao;
import marquez.db.OpenLineageDao;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.models.Dataset;
import marquez.service.models.Job;
import marquez.service.models.LineageEvent;
import marquez.service.models.Run;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

@RunWith(Parameterized.class)
@Category({DataAccessTests.class, IntegrationTests.class})
public class OpenLineageServiceTest {
  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();
  private RunService runService;
  private OpenLineageDao openLineageDao;
  private DatasetVersionDao datasetVersionDao;
  private ArgumentCaptor<JobInputUpdate> runInputListener;
  private ArgumentCaptor<JobOutputUpdate> runOutputListener;
  private OpenLineageService lineageService;
  private Jdbi jdbi;

  public static String EVENT_REQUIRED_ONLY = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";
  private List<LineageEvent> eventList;

  @Parameters(name = "{0}")
  public static List<Object[]> data() throws IOException, URISyntaxException {
    String prefix = "../integrations/spark/integrations";
    List<URI> rdd =
        Files.list(Paths.get(prefix + "/sparkrdd")).map(Path::toUri).collect(Collectors.toList());
    List<URI> sql =
        Files.list(Paths.get(prefix + "/sparksql")).map(Path::toUri).collect(Collectors.toList());
    return Arrays.asList(
        new Object[] {
          Arrays.asList(Resources.getResource(EVENT_REQUIRED_ONLY).toURI()),
          new ExpectedResults(0, 0, 0)
        },
        new Object[] {
          Arrays.asList(Resources.getResource(EVENT_SIMPLE).toURI()), new ExpectedResults(2, 1, 1)
        },
        new Object[] {
          Arrays.asList(Resources.getResource(EVENT_FULL).toURI()), new ExpectedResults(1, 1, 1)
        },
        new Object[] {
          Arrays.asList(Resources.getResource(EVENT_UNICODE).toURI()), new ExpectedResults(2, 1, 1)
        },
        new Object[] {
          Arrays.asList(
              Resources.getResource("open_lineage/listener/1.json").toURI(),
              Resources.getResource("open_lineage/listener/2.json").toURI()),
          new ExpectedResults(3, 2, 2)
        },
        new Object[] {rdd, new ExpectedResults(1, 0, 2)},
        new Object[] {sql, new ExpectedResults(1, 0, 4)},
        new Object[] {
          Arrays.asList(Resources.getResource(EVENT_LARGE).toURI()), new ExpectedResults(1, 1, 1)
        });
  }

  public static class ExpectedResults {
    public int inputDatasetCount;
    public int outputDatasetCount;
    public int uniqueEventCount;

    public ExpectedResults(int inputDatasetCount, int outputDatasetCount, int uniqueEventCount) {
      this.inputDatasetCount = inputDatasetCount;
      this.outputDatasetCount = outputDatasetCount;
      this.uniqueEventCount = uniqueEventCount;
    }
  }

  @Parameter(value = 0)
  public List<URI> events;

  @Parameter(value = 1)
  public ExpectedResults expected;

  @Before
  public void setup() throws ExecutionException, InterruptedException {
    jdbi = dbRule.getJdbi();
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    runService = mock(RunService.class);
    runInputListener = ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(runService).notify(runInputListener.capture());
    runOutputListener = ArgumentCaptor.forClass(JobOutputUpdate.class);
    doNothing().when(runService).notify(runOutputListener.capture());
    lineageService = new OpenLineageService(openLineageDao, runService, datasetVersionDao);

    List<LineageEvent> eventList = new ArrayList<>();
    for (URI event : events) {
      LineageEvent l = getLineageEventFromResource(event);
      lineageService.createAsync(l).get();
      eventList.add(l);
    }
    this.eventList = eventList;
  }

  @Test
  public void testRunListenerInput() {
    if (expected.inputDatasetCount > 0) {
      assertEquals(
          "RunInputListener events",
          expected.uniqueEventCount,
          runInputListener.getAllValues().size());
      assertEquals(
          "Dataset input count",
          expected.inputDatasetCount,
          runInputListener
              .getAllValues()
              .get(runInputListener.getAllValues().size() - 1)
              .getInputs()
              .size());
    }
  }

  @Test
  public void testRunListenerOutput() {
    if (expected.outputDatasetCount > 0) {
      assertEquals(
          "RunOutputListener events",
          expected.uniqueEventCount,
          runOutputListener.getAllValues().size());
      assertEquals(
          "Dataset output count",
          expected.outputDatasetCount,
          runOutputListener.getAllValues().get(0).getOutputs().size());
    }
  }

  @Test
  public void serviceCalls() {
    JobService jobService = new JobService(openLineageDao, runService);
    LineageEvent event = eventList.get(eventList.size() - 1);
    Optional<Job> job =
        jobService.get(
            NamespaceName.of(openLineageDao.formatNamespaceName(event.getJob().getNamespace())),
            JobName.of(event.getJob().getName()));
    assertTrue("Job does not exist: " + event.getJob().getName(), job.isPresent());

    RunService runService = new RunService(openLineageDao, new ArrayList());
    Optional<Run> run =
        runService.getRun(RunId.of(openLineageDao.runToUuid(event.getRun().getRunId())));
    assertTrue("Should have run", run.isPresent());

    if (event.getInputs() != null) {
      for (LineageEvent.Dataset ds : event.getInputs()) {
        checkExists(ds);
      }
    }
    if (event.getOutputs() != null) {
      for (LineageEvent.Dataset ds : event.getOutputs()) {
        checkExists(ds);
      }
    }
  }

  private void checkExists(LineageEvent.Dataset ds) {
    DatasetService datasetService = new DatasetService(openLineageDao, runService);

    Optional<Dataset> dataset =
        datasetService.get(
            NamespaceName.of(openLineageDao.formatNamespaceName(ds.getNamespace())),
            DatasetName.of(openLineageDao.formatDatasetName(ds.getName())));
    assertTrue("Dataset does not exist: " + ds, dataset.isPresent());
  }

  private LineageEvent getLineageEventFromResource(URI location) {
    try {
      return Utils.newObjectMapper().readValue(location.toURL(), LineageEvent.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
