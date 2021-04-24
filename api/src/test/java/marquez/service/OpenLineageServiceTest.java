package marquez.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import marquez.common.Utils;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.OpenLineageDao;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.models.Dataset;
import marquez.service.models.Job;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.DatasetFacet;
import marquez.service.models.LineageEvent.DatasourceDatasetFacet;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.Run;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class OpenLineageServiceTest {

  public static final String NAMESPACE = "theNamespace";
  public static final String JOB_NAME = "theJob";
  public static final ZoneId TIMEZONE = ZoneId.of("America/Los_Angeles");
  public static final String DATASET_NAME = "theDataset";
  private RunService runService;
  private OpenLineageDao openLineageDao;
  private DatasetDao datasetDao;
  private DatasetVersionDao datasetVersionDao;
  private ArgumentCaptor<JobInputUpdate> runInputListener;
  private ArgumentCaptor<JobOutputUpdate> runOutputListener;
  private OpenLineageService lineageService;

  public static String EVENT_REQUIRED_ONLY = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";

  public static List<Object[]> getData() throws IOException, URISyntaxException {
    String prefix = "../integrations/spark/integrations";
    List<URI> rdd =
        Files.list(Paths.get(prefix + "/sparkrdd")).map(Path::toUri).collect(Collectors.toList());
    List<URI> sql =
        Files.list(Paths.get(prefix + "/sparksql")).map(Path::toUri).collect(Collectors.toList());
    return Stream.of(
            new Object[] {
              Arrays.asList(Resources.getResource(EVENT_REQUIRED_ONLY).toURI()),
              new ExpectedResults(0, 0, 0, 0)
            },
            new Object[] {
              Arrays.asList(Resources.getResource(EVENT_SIMPLE).toURI()),
              new ExpectedResults(2, 1, 1, 1)
            },
            new Object[] {
              Arrays.asList(Resources.getResource(EVENT_FULL).toURI()),
              new ExpectedResults(1, 1, 1, 1)
            },
            new Object[] {
              Arrays.asList(Resources.getResource(EVENT_UNICODE).toURI()),
              new ExpectedResults(2, 1, 1, 1)
            },
            new Object[] {
              Arrays.asList(
                  Resources.getResource("open_lineage/listener/1.json").toURI(),
                  Resources.getResource("open_lineage/listener/2.json").toURI()),
              new ExpectedResults(3, 2, 2, 1)
            },
            new Object[] {rdd, new ExpectedResults(1, 0, 2, 2)},
            new Object[] {sql, new ExpectedResults(1, 0, 4, 4)},
            new Object[] {
              Arrays.asList(Resources.getResource(EVENT_LARGE).toURI()),
              new ExpectedResults(1, 1, 1, 1)
            })
        .collect(Collectors.toList());
  }

  public static class ExpectedResults {
    public int inputDatasetCount;
    public int outputDatasetCount;
    public int inputEventCount;
    public int outputEventCount;

    public ExpectedResults(
        int inputDatasetCount, int outputDatasetCount, int inputEventCount, int outputEventCount) {
      this.inputDatasetCount = inputDatasetCount;
      this.outputDatasetCount = outputDatasetCount;
      this.inputEventCount = inputEventCount;
      this.outputEventCount = outputEventCount;
    }
  }

  @BeforeEach
  public void setup(Jdbi jdbi) {
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    runService = mock(RunService.class);
    runInputListener = ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(runService).notify(runInputListener.capture());
    runOutputListener = ArgumentCaptor.forClass(JobOutputUpdate.class);
    doNothing().when(runService).notify(runOutputListener.capture());
    lineageService = new OpenLineageService(openLineageDao, runService);
    datasetDao = jdbi.onDemand(DatasetDao.class);
  }

  private List<LineageEvent> initEvents(List<URI> uris) {
    List<LineageEvent> events = new ArrayList<>();
    for (URI uri : uris) {
      try {
        LineageEvent event = getLineageEventFromResource(uri);
        lineageService.createAsync(event).get();
        events.add(event);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return events;
  }

  @ParameterizedTest
  @MethodSource("getData")
  public void testRunListenerInput(List<URI> uris, ExpectedResults expectedResults) {
    initEvents(uris);

    if (expectedResults.inputDatasetCount > 0) {
      Assertions.assertEquals(
          expectedResults.inputEventCount,
          runInputListener.getAllValues().size(),
          "RunInputListener events");
      Assertions.assertEquals(
          expectedResults.inputDatasetCount,
          runInputListener
              .getAllValues()
              .get(runInputListener.getAllValues().size() - 1)
              .getInputs()
              .size(),
          "Dataset input count");
    }
  }

  @ParameterizedTest
  @MethodSource("getData")
  public void testRunListenerOutput(List<URI> uris, ExpectedResults expectedResults) {
    initEvents(uris);

    if (expectedResults.outputDatasetCount > 0) {
      Assertions.assertEquals(
          expectedResults.outputEventCount,
          runOutputListener.getAllValues().size(),
          "RunOutputListener events");
      Assertions.assertEquals(
          expectedResults.outputDatasetCount,
          runOutputListener.getAllValues().get(0).getOutputs().size(),
          "Dataset output count");
    }
  }

  @ParameterizedTest
  @MethodSource({"getData"})
  public void serviceCalls(List<URI> uris, ExpectedResults expectedResults) {
    List<LineageEvent> events = initEvents(uris);

    JobService jobService = new JobService(openLineageDao, runService);
    LineageEvent event = events.get(events.size() - 1);
    Optional<Job> job =
        jobService.findWithRun(
            openLineageDao.formatNamespaceName(event.getJob().getNamespace()),
            event.getJob().getName());
    Assertions.assertTrue(job.isPresent(), "Job does not exist: " + event.getJob().getName());

    RunService runService = new RunService(openLineageDao, new ArrayList());
    Optional<Run> run = runService.findBy(openLineageDao.runToUuid(event.getRun().getRunId()));
    Assertions.assertTrue(run.isPresent(), "Should have run");

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

  @Test
  public void testDatasetVersionUpdatedOnRunCompletion()
      throws ExecutionException, InterruptedException {
    LineageEvent.Dataset dataset =
        LineageEvent.Dataset.builder()
            .name(DATASET_NAME)
            .namespace(NAMESPACE)
            .facets(
                DatasetFacet.builder()
                    .dataSource(
                        DatasourceDatasetFacet.builder()
                            .name("theDatasource")
                            .uri("http://thedatasource")
                            .build())
                    .build())
            .build();

    // First run creates the dataset without a currentVersionUuid
    UUID firstRunId = UUID.randomUUID();
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("RUNNING")
                .run(new LineageEvent.Run(firstRunId.toString(), RunFacet.builder().build()))
                .job(LineageEvent.Job.builder().name(JOB_NAME).namespace(NAMESPACE).build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(new ArrayList<>())
                .outputs(Collections.singletonList(dataset))
                .build())
        .get();
    Optional<Dataset> datasetRow = datasetDao.find(NAMESPACE, DATASET_NAME);
    assertThat(datasetRow).isPresent().flatMap(Dataset::getCurrentVersionUuid).isNotPresent();

    // On complete, the currentVersionUuid is updated
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("COMPLETE")
                .run(new LineageEvent.Run(firstRunId.toString(), RunFacet.builder().build()))
                .job(LineageEvent.Job.builder().name(JOB_NAME).namespace(NAMESPACE).build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(new ArrayList<>())
                .outputs(Collections.singletonList(dataset))
                .build())
        .get();
    datasetRow = datasetDao.find(NAMESPACE, DATASET_NAME);
    assertThat(datasetRow).isPresent().flatMap(Dataset::getCurrentVersionUuid).isPresent();

    List<ExtendedDatasetVersionRow> outputs = datasetVersionDao.findOutputsByRunId(firstRunId);
    assertThat(outputs).hasSize(1).map(DatasetVersionRow::getVersion).isNotNull();

    UUID dsVersion1Id = outputs.get(0).getVersion();

    // A consumer gets the currentVersionUuid as its input version
    UUID secondRunId = UUID.randomUUID();
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("COMPLETE")
                .run(new LineageEvent.Run(secondRunId.toString(), RunFacet.builder().build()))
                .job(LineageEvent.Job.builder().name("AnInputJob").namespace(NAMESPACE).build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(Collections.singletonList(dataset))
                .outputs(new ArrayList<>())
                .build())
        .get();
    List<ExtendedDatasetVersionRow> inputs = datasetVersionDao.findInputsByRunId(secondRunId);
    assertThat(inputs).hasSize(1).map(DatasetVersionRow::getVersion).contains(dsVersion1Id);

    // fail to write the dataset - the currentVersionUuid is not updated
    UUID failedRunId = UUID.randomUUID();
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("FAILED")
                .run(new LineageEvent.Run(failedRunId.toString(), RunFacet.builder().build()))
                .job(LineageEvent.Job.builder().name(JOB_NAME).namespace(NAMESPACE).build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(new ArrayList<>())
                .outputs(Collections.singletonList(dataset))
                .build())
        .get();

    Optional<Dataset> afterFailureDataset = datasetDao.find(NAMESPACE, DATASET_NAME);
    assertThat(afterFailureDataset)
        .isPresent()
        .flatMap(Dataset::getCurrentVersionUuid)
        .isPresent()
        .get()
        .isEqualTo(datasetRow.get().getCurrentVersionUuid().get());

    // A new consumer job run only sees the first dataset version
    UUID fourthRunId = UUID.randomUUID();
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("COMPLETE")
                .run(new LineageEvent.Run(fourthRunId.toString(), RunFacet.builder().build()))
                .job(LineageEvent.Job.builder().name("AnInputJob").namespace(NAMESPACE).build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(Collections.singletonList(dataset))
                .outputs(new ArrayList<>())
                .build())
        .get();

    // still version 1 is consumed since the second producer job run failed
    assertThat(datasetVersionDao.findInputsByRunId(secondRunId))
        .hasSize(1)
        .map(DatasetVersionRow::getVersion)
        .contains(dsVersion1Id);
  }

  private void checkExists(LineageEvent.Dataset ds) {
    DatasetService datasetService = new DatasetService(openLineageDao, runService);

    Optional<Dataset> dataset =
        datasetService.find(
            openLineageDao.formatNamespaceName(ds.getNamespace()),
            openLineageDao.formatDatasetName(ds.getName()));
    Assertions.assertTrue(dataset.isPresent(), "Dataset does not exist: " + ds);
  }

  private static LineageEvent getLineageEventFromResource(URI location) {
    try {
      return Utils.newObjectMapper().readValue(location.toURL(), LineageEvent.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
