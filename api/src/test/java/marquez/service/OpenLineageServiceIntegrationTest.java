/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import marquez.common.Utils;
import marquez.common.models.FieldName;
import marquez.common.models.JobType;
import marquez.common.models.RunState;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetEvent;
import marquez.service.models.Job;
import marquez.service.models.JobEvent;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.DatasetFacets;
import marquez.service.models.LineageEvent.DatasourceDatasetFacet;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.JobTypeJobFacet;
import marquez.service.models.LineageEvent.LineageEventBuilder;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.SQLJobFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
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
public class OpenLineageServiceIntegrationTest {

  public static final String NAMESPACE = "theNamespace";
  public static final String JOB_NAME = "theJob";
  public static final ZoneId TIMEZONE = ZoneId.of("America/Los_Angeles");
  public static final String DATASET_NAME = "theDataset";
  public static final String INPUT_DATASET_NAME = "theInputDataset";
  private RunService runService;

  private JobService jobService;
  private OpenLineageDao openLineageDao;
  private Jdbi jdbi;
  private JobDao jobDao;
  private JobVersionDao jobVersionDao;
  private DatasetDao datasetDao;
  private DatasetVersionDao datasetVersionDao;
  private ArgumentCaptor<JobInputUpdate> runInputListener;
  private ArgumentCaptor<JobOutputUpdate> runOutputListener;
  private ArgumentCaptor<RunTransition> runTransitionListener;
  private OpenLineageService lineageService;

  public static String EVENT_REQUIRED_ONLY = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";

  public static List<Object[]> getData() throws IOException, URISyntaxException {
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
  public void setup(Jdbi jdbi) throws SQLException {
    this.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    jobDao = jdbi.onDemand(JobDao.class);
    jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    runService = mock(RunService.class);
    jobService = new JobService(jobDao, runService);
    runInputListener = ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(runService).notify(runInputListener.capture());
    runOutputListener = ArgumentCaptor.forClass(JobOutputUpdate.class);
    doNothing().when(runService).notify(runOutputListener.capture());
    runTransitionListener = ArgumentCaptor.forClass(RunTransition.class);
    doNothing().when(runService).notify(runTransitionListener.capture());
    lineageService = new OpenLineageService(openLineageDao, runService);
    datasetDao = jdbi.onDemand(DatasetDao.class);

    NamespaceRow namespace =
        jdbi.onDemand(NamespaceDao.class)
            .upsertNamespaceRow(UUID.randomUUID(), Instant.now(), NAMESPACE, "me");
    JobRow job =
        jobDao.upsertJob(
            UUID.randomUUID(),
            JobType.BATCH,
            Instant.now(),
            namespace.getUuid(),
            NAMESPACE,
            "parentJob",
            "description",
            null,
            null,
            null);
    Map<String, String> runArgsMap = new HashMap<>();
    RunArgsRow argsRow =
        jdbi.onDemand(RunArgsDao.class)
            .upsertRunArgs(
                UUID.randomUUID(),
                Instant.now(),
                Utils.toJson(runArgsMap),
                Utils.checksumFor(runArgsMap));
    jdbi.onDemand(RunDao.class)
        .upsert(
            UUID.fromString("3f5e83fa-3480-44ff-99c5-ff943904e5e8"),
            null,
            "3f5e83fa-3480-44ff-99c5-ff943904e5e8",
            Instant.now(),
            job.getUuid(),
            null,
            argsRow.getUuid(),
            Instant.now(),
            Instant.now(),
            RunState.RUNNING,
            Instant.now(),
            NAMESPACE,
            job.getName(),
            null);
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
  @MethodSource("getData")
  public void testRunTransition(List<URI> uris, ExpectedResults expectedResults) {
    initEvents(uris);

    if (expectedResults.inputEventCount > 0) {
      Assertions.assertEquals(
          uris.size(),
          runTransitionListener.getAllValues().size(),
          "RunTransition happens once for each run");
    }
  }

  @ParameterizedTest
  @MethodSource({"getData"})
  public void serviceCalls(List<URI> uris, ExpectedResults expectedResults) {
    List<LineageEvent> events = initEvents(uris);

    JobService jobService = new JobService(openLineageDao, runService);
    LineageEvent event = events.get(events.size() - 1);
    Optional<Job> job =
        jobService.findWithDatasetsAndRun(
            openLineageDao.formatNamespaceName(event.getJob().getNamespace()),
            event.getJob().getName());
    Assertions.assertTrue(job.isPresent(), "Job does not exist: " + event.getJob().getName());

    RunService runService = new RunService(openLineageDao, new ArrayList());
    Optional<Run> run =
        runService.findRunByUuid(openLineageDao.runToUuid(event.getRun().getRunId()));
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
                DatasetFacets.builder()
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
    Optional<Dataset> datasetRow = datasetDao.findDatasetByName(NAMESPACE, DATASET_NAME);
    assertThat(datasetRow).isPresent().flatMap(Dataset::getCurrentVersion).isNotPresent();

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
    datasetRow = datasetDao.findDatasetByName(NAMESPACE, DATASET_NAME);
    assertThat(datasetRow).isPresent().flatMap(Dataset::getCurrentVersion).isPresent();

    List<ExtendedDatasetVersionRow> outputs =
        datasetVersionDao.findOutputDatasetVersionsFor(firstRunId);
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
    List<ExtendedDatasetVersionRow> inputs =
        datasetVersionDao.findInputDatasetVersionsFor(secondRunId);
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

    Optional<Dataset> afterFailureDataset = datasetDao.findDatasetByName(NAMESPACE, DATASET_NAME);
    assertThat(afterFailureDataset)
        .isPresent()
        .flatMap(Dataset::getCurrentVersion)
        .isPresent()
        .get()
        .isEqualTo(datasetRow.get().getCurrentVersion().get());

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
    assertThat(datasetVersionDao.findInputDatasetVersionsFor(secondRunId))
        .hasSize(1)
        .map(DatasetVersionRow::getVersion)
        .contains(dsVersion1Id);
  }

  @Test
  void testJobIsNotHiddenAfterSubsequentOLEvent() throws ExecutionException, InterruptedException {
    String name = "aNotHiddenJob";

    LineageEvent.LineageEventBuilder builder =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .job(LineageEvent.Job.builder().name(name).namespace(NAMESPACE).build())
            .eventTime(Instant.now().atZone(TIMEZONE))
            .inputs(Collections.emptyList())
            .outputs(Collections.emptyList());

    lineageService
        .createAsync(
            builder
                .run(new LineageEvent.Run(UUID.randomUUID().toString(), RunFacet.builder().build()))
                .build())
        .get();

    assertThat(jobService.findJobByName(NAMESPACE, name)).isNotEmpty();

    jobService.delete(NAMESPACE, name);

    assertThat(jobService.findJobByName(NAMESPACE, name)).isEmpty();

    lineageService
        .createAsync(
            builder
                .run(new LineageEvent.Run(UUID.randomUUID().toString(), RunFacet.builder().build()))
                .build())
        .get();

    assertThat(jobService.findJobByName(NAMESPACE, name)).isNotEmpty();
  }

  @Test
  void testDatasetEvent() throws ExecutionException, InterruptedException {
    LineageEvent.Dataset dataset =
        LineageEvent.Dataset.builder()
            .name(DATASET_NAME)
            .namespace(NAMESPACE)
            .facets(
                DatasetFacets.builder()
                    .schema(
                        new SchemaDatasetFacet(
                            PRODUCER_URL,
                            SCHEMA_URL,
                            Arrays.asList(new SchemaField("col", "STRING", "my name"))))
                    .dataSource(
                        DatasourceDatasetFacet.builder()
                            .name("theDatasource")
                            .uri("http://thedatasource")
                            .build())
                    .build())
            .build();

    lineageService
        .createAsync(
            DatasetEvent.builder()
                .eventTime(Instant.now().atZone(TIMEZONE))
                .dataset(dataset)
                .build())
        .get();

    Optional<Dataset> datasetRow = datasetDao.findDatasetByName(NAMESPACE, DATASET_NAME);
    assertThat(datasetRow).isPresent().map(Dataset::getCurrentVersion).isPresent();
    assertThat(datasetRow.get().getSourceName().getValue()).isEqualTo("theDatasource");
    assertThat(datasetRow.get().getFields())
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("name", FieldName.of("col"))
        .hasFieldOrPropertyWithValue("type", "STRING");
  }

  @Test
  void testJobEvent() throws ExecutionException, InterruptedException {
    String query = "select * from table";
    LineageEvent.Job job =
        LineageEvent.Job.builder()
            .name(JOB_NAME)
            .namespace(NAMESPACE)
            .facets(JobFacet.builder().sql(SQLJobFacet.builder().query(query).build()).build())
            .build();

    DatasourceDatasetFacet theDatasource =
        DatasourceDatasetFacet.builder().name("theDatasource").uri("http://thedatasource").build();
    LineageEvent.Dataset input =
        LineageEvent.Dataset.builder()
            .name(INPUT_DATASET_NAME)
            .namespace(NAMESPACE)
            .facets(DatasetFacets.builder().dataSource(theDatasource).build())
            .build();

    LineageEvent.Dataset output =
        LineageEvent.Dataset.builder()
            .name(DATASET_NAME)
            .namespace(NAMESPACE)
            .facets(DatasetFacets.builder().dataSource(theDatasource).build())
            .build();

    lineageService
        .createAsync(
            JobEvent.builder()
                .eventTime(Instant.now().atZone(TIMEZONE))
                .job(job)
                .inputs(Collections.singletonList(input))
                .outputs(Collections.singletonList(output))
                .build())
        .get();

    Optional<Job> jobByName = jobDao.findJobByName(NAMESPACE, JOB_NAME);
    assertThat(jobByName).isPresent().map(Job::getCurrentVersion).isPresent();
    assertThat(jobByName.get().getFacets().get("sql").toString()).contains(query);

    DatasetService datasetService = new DatasetService(openLineageDao, runService);
    assertThat(
            datasetService
                .findDatasetByName(
                    openLineageDao.formatNamespaceName(input.getNamespace()),
                    openLineageDao.formatDatasetName(input.getName()))
                .get()
                .getSourceName()
                .getValue())
        .isEqualTo("theDatasource");

    assertThat(
            datasetService
                .findDatasetByName(
                    openLineageDao.formatNamespaceName(output.getNamespace()),
                    openLineageDao.formatDatasetName(output.getName()))
                .get()
                .getSourceName()
                .getValue())
        .isEqualTo("theDatasource");
  }

  @Test
  void testStreamingJob() throws ExecutionException, InterruptedException {
    // (1) Create output
    LineageEvent.Dataset input =
        LineageEvent.Dataset.builder().name(DATASET_NAME).namespace(NAMESPACE).build();

    LineageEvent.Dataset output =
        LineageEvent.Dataset.builder()
            .name(DATASET_NAME)
            .namespace(NAMESPACE)
            .facets(
                DatasetFacets.builder()
                    .schema(
                        new SchemaDatasetFacet(
                            PRODUCER_URL,
                            SCHEMA_URL,
                            Arrays.asList(new SchemaField("col", "STRING", "my name"))))
                    .build())
            .build();

    // (2) Streaming job not followed by a COMPLETE event writing to a output
    UUID firstRunId = UUID.randomUUID();
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("RUNNING")
                .run(new LineageEvent.Run(firstRunId.toString(), RunFacet.builder().build()))
                .job(
                    LineageEvent.Job.builder()
                        .name("streaming_job_name")
                        .namespace(NAMESPACE)
                        .facets(
                            JobFacet.builder()
                                .jobType(
                                    JobTypeJobFacet.builder()
                                        .processingType("STREAMING")
                                        .integration("FLINK")
                                        .jobType("JOB")
                                        .build())
                                .build())
                        .build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(Collections.singletonList(input))
                .outputs(Collections.singletonList(output))
                .build())
        .get();
    Optional<Dataset> datasetRow = datasetDao.findDatasetByName(NAMESPACE, DATASET_NAME);

    // (3) Assert that output is present and has dataset_version written
    assertThat(datasetRow).isPresent().flatMap(Dataset::getCurrentVersion).isPresent();

    // (4) Assert that job is present and its current version is present
    Job job = jobDao.findJobByName(NAMESPACE, "streaming_job_name").get();
    assertThat(job.getInputs()).hasSize(1);
    assertThat(job.getCurrentVersion()).isPresent();
    assertThat(job.getType()).isEqualTo(JobType.STREAM);
    assertThat(job.getLabels()).containsExactly("JOB", "FLINK");

    UUID initialJobVersion = job.getCurrentVersion().get();
    Instant updatedAt =
        jdbi.withHandle(
            h ->
                h.createQuery("SELECT max(updated_at) FROM job_versions")
                    .mapTo(Instant.class)
                    .first());

    // (5) Send COMPLETE event streaming job
    lineageService
        .createAsync(
            LineageEvent.builder()
                .eventType("COMPLETE")
                .run(new LineageEvent.Run(firstRunId.toString(), RunFacet.builder().build()))
                .job(
                    LineageEvent.Job.builder()
                        .name("streaming_job_name")
                        .namespace(NAMESPACE)
                        .facets(
                            JobFacet.builder()
                                .jobType(
                                    JobTypeJobFacet.builder()
                                        .processingType("STREAMING")
                                        .integration("FLINK")
                                        .jobType("JOB")
                                        .build())
                                .build())
                        .build())
                .eventTime(Instant.now().atZone(TIMEZONE))
                .inputs(Collections.emptyList())
                .outputs(Collections.emptyList())
                .build())
        .get();

    // (6) Assert job version exists
    job = jobDao.findJobByName(NAMESPACE, "streaming_job_name").get();
    assertThat(job.getCurrentVersion()).isPresent();
    assertThat(job.getType()).isEqualTo(JobType.STREAM);
    assertThat(job.getLabels()).containsExactly("JOB", "FLINK");
    assertThat(job.getCurrentVersion().get()).isEqualTo(initialJobVersion);

    // (7) Make sure updated_at in job version did not change
    Instant lastUpdatedAt =
        jdbi.withHandle(
            h ->
                h.createQuery("SELECT max(updated_at) FROM job_versions")
                    .mapTo(Instant.class)
                    .first());
    assertThat(updatedAt).isEqualTo(lastUpdatedAt);
  }

  @Test
  void testStreamingJobCreateSingleJobAndDatasetVersion()
      throws ExecutionException, InterruptedException {
    LineageEvent.Dataset dataset =
        LineageEvent.Dataset.builder().name(DATASET_NAME).namespace(NAMESPACE).build();
    UUID firstRunId = UUID.randomUUID();
    LineageEventBuilder eventBuilder =
        LineageEvent.builder()
            .eventType("RUNNING")
            .run(new LineageEvent.Run(firstRunId.toString(), RunFacet.builder().build()))
            .job(
                LineageEvent.Job.builder()
                    .name("streaming_job_name")
                    .namespace(NAMESPACE)
                    .facets(
                        JobFacet.builder()
                            .jobType(JobTypeJobFacet.builder().processingType("STREAMING").build())
                            .build())
                    .build())
            .eventTime(Instant.now().atZone(TIMEZONE));
    LineageEvent lineageEvent = eventBuilder.outputs(Collections.singletonList(dataset)).build();

    // (1) Emit running event
    lineageService.createAsync(lineageEvent).get();

    UUID datasetVersionUuid =
        datasetDao.findDatasetAsRow(NAMESPACE, DATASET_NAME).get().getCurrentVersionUuid().get();
    int initialJobVersionsCount =
        jobVersionDao.findAllJobVersions(NAMESPACE, "streaming_job_name", 10, 0).size();

    // (2) Emit other running event
    lineageService.createAsync(lineageEvent).get();

    // (3) Emit running event with no input nor output datasets
    lineageService.createAsync(eventBuilder.build()).get();
    assertThat(jobVersionDao.findAllJobVersions(NAMESPACE, "streaming_job_name", 10, 0).size())
        .isEqualTo(initialJobVersionsCount);

    // (4) Emit event with other input dataset
    LineageEvent.Dataset otherdataset =
        LineageEvent.Dataset.builder().name("otherDataset").namespace(NAMESPACE).build();
    lineageService
        .createAsync(eventBuilder.inputs(Collections.singletonList(otherdataset)).build())
        .get();
    assertThat(jobVersionDao.findAllJobVersions(NAMESPACE, "streaming_job_name", 10, 0).size())
        .isEqualTo(initialJobVersionsCount + 1);

    // (5) Verify dataset's version has not changed
    assertThat(
            datasetDao
                .findDatasetAsRow(NAMESPACE, DATASET_NAME)
                .get()
                .getCurrentVersionUuid()
                .get())
        .isEqualTo(datasetVersionUuid);
  }

  private void checkExists(LineageEvent.Dataset ds) {
    DatasetService datasetService = new DatasetService(openLineageDao, runService);

    Optional<Dataset> dataset =
        datasetService.findDatasetByName(
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
