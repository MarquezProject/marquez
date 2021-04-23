package marquez.db;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.newDatasetFacet;
import static marquez.db.LineageTestUtils.writeDownstreamLineage;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import marquez.common.models.DatasetName;
import marquez.db.LineageTestUtils.DatasetConsumerJob;
import marquez.db.LineageTestUtils.JobLineage;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.SchemaField;
import marquez.service.models.LineageEvent.SourceCodeLocationJobFacet;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class LineageDaoTest {

  private static LineageDao lineageDao;
  private static OpenLineageDao openLineageDao;
  private final Dataset dataset =
      new Dataset(
          NAMESPACE,
          "commonDataset",
          newDatasetFacet(
              new SchemaField("firstname", "string", "the first name"),
              new SchemaField("lastname", "string", "the last name"),
              new SchemaField("birthdate", "date", "the date of birth")));
  private final JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

  static Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    LineageDaoTest.jdbi = jdbi;
    lineageDao = jdbi.onDemand(LineageDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM dataset_versions_field_mapping");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
          handle.execute("DELETE FROM run_states");
          handle.execute("DELETE FROM runs");
          handle.execute("DELETE FROM run_args");
          handle.execute("DELETE FROM job_versions_io_mapping");
          handle.execute("DELETE FROM job_versions");
          handle.execute("DELETE FROM jobs");
          handle.execute("DELETE FROM dataset_fields_tag_mapping");
          handle.execute("DELETE FROM dataset_fields");
          handle.execute("DELETE FROM datasets");
          handle.execute("DELETE FROM sources");
          handle.execute("DELETE FROM namespaces");
          return null;
        });
  }

  @Test
  public void testGetLineage() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("outputData")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            jobFacet,
            dataset);

    // don't expect a failed job job in the returned lineage
    UpdateLineageRow failedJobRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "readJobFailed",
            "FAILED",
            jobFacet,
            Arrays.asList(dataset),
            Arrays.asList());

    // fetch the first "readJob" lineage.
    Set<UUID> connectedJobs =
        lineageDao.getLineage(new HashSet<>(Arrays.asList(jobRows.get(0).getId())));
    assertThat(connectedJobs).size().isEqualTo(22);

    // expect the job that wrote "commonDataset", which is readJob0's input
    assertThat(connectedJobs).contains(writeJob.getJob().getUuid());

    // expect all jobs that read the same input dataset as readJob0
    Set<UUID> readJobUUIDs =
        jobRows.stream().map(LineageTestUtils.JobLineage::getId).collect(Collectors.toSet());
    assertThat(connectedJobs).contains(readJobUUIDs.toArray(UUID[]::new));

    // expect that the failed job that reads the same input dataset is not present
    assertThat(connectedJobs).doesNotContain(failedJobRow.getJob().getUuid());

    // also expect all jobs that read the output of readJob0 (downstreamJob0)
    Optional<JobData> downstreamJob =
        connectedJobs.stream()
            .filter(id -> !readJobUUIDs.contains(id) && !id.equals(writeJob.getJob().getUuid()))
            .flatMap(id -> lineageDao.getJob(Collections.singletonList(id)).stream())
            .findAny();
    assertThat(downstreamJob)
        .isPresent()
        .map(jd -> jd.getName().getValue())
        .get()
        .isEqualTo("downstreamJob0<-outputData<-readJob0<-commonDataset");
  }

  @Test
  public void testGetLineageWithJobThatHasNoDownstreamConsumers() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    Set<UUID> lineage = lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(lineage).hasSize(1).contains(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetLineageWithJobThatHasNoDatasets() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());
    Set<UUID> lineage = lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(lineage).isEmpty();
  }

  /**
   * Test the datasets that are inputs to the specified job are returned along with their output
   * edges.
   */
  @Test
  public void testGetInputDatasetsFromJobIds() {

    LineageTestUtils.createLineageRow(
        openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList(dataset));

    // create a graph starting with the commonDataset, which spawns 20 consumers.
    // each of those spawns 3 consumers of the "intermediate" dataset
    // each of those spawns 1 consumer of the "finalOutput" dataset
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("intermediate")),
                    new DatasetConsumerJob("downstreamJob", 3, Optional.of("finalOutput")),
                    new DatasetConsumerJob("finalConsumer", 1, Optional.empty()))),
            jobFacet,
            dataset);
    // sanity check
    List<JobLineage> downstreamJobs = jobRows.get(0).getDownstreamJobs();
    assertThat(downstreamJobs).hasSize(3);

    // fetch the dataset lineage for the first "downstreamJob" job. It touches the "intermediate"
    // dataset, which is its input, and the "finalOutput", which is its output.
    // There are 3 "downstreamJob" instances which read the same input dataset and 1
    // "finalConsumer" job, which reads the output. The returned datasets should have
    // pointers to those consumers.
    JobLineage firstDownstream = downstreamJobs.get(0);
    List<DatasetData> inputDatasets =
        lineageDao.getInputDatasetsFromJobIds(Collections.singleton(firstDownstream.getId()));

    // two datasets- intermediate and finalOutput
    assertThat(inputDatasets)
        .hasSize(2)
        .map(DatasetData::getName)
        .map(DatasetName::getValue)
        .containsAll(
            Arrays.asList(
                "finalOutput<-downstreamJob0<-intermediate<-readJob0<-commonDataset",
                "intermediate<-readJob0<-commonDataset"));

    // first has 1 consumer- the "finalConsumer"
    List<JobLineage> finalConsumers = firstDownstream.getDownstreamJobs();
    assertThat(inputDatasets.get(0).getJobUuids())
        .hasSize(1)
        .containsAll(finalConsumers.stream().map(JobLineage::getId)::iterator);

    // second one has 3 consumers- the "downstreamJob"s
    assertThat(inputDatasets.get(1).getJobUuids())
        .hasSize(3)
        .containsAll(downstreamJobs.stream().map(JobLineage::getId)::iterator);
  }

  /**
   * Validate a job that consumes a dataset, but shares no datasets with any other job returns only
   * the consumed dataset
   */
  @Test
  public void testGetInputDatasetsWithJobThatSharesNoDatasets() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset),
            Arrays.asList());

    // write a new dataset with a different name
    Dataset anotherDataset =
        new Dataset(
            NAMESPACE,
            "anUncommonDataset",
            newDatasetFacet(
                new SchemaField("firstname", "string", "the first name"),
                new SchemaField("lastname", "string", "the last name"),
                new SchemaField("birthdate", "date", "the date of birth")));
    // write a bunch of jobs that share nothing with the writeJob
    writeDownstreamLineage(
        openLineageDao,
        Arrays.asList(new DatasetConsumerJob("consumer", 5, Optional.empty())),
        jobFacet,
        anotherDataset);

    // Validate that finalConsumer job only has a single dataset
    Set<UUID> jobIds = Collections.singleton(writeJob.getJob().getUuid());
    List<DatasetData> inputsForFinalConsumer = lineageDao.getInputDatasetsFromJobIds(jobIds);
    assertThat(inputsForFinalConsumer)
        .hasSize(1)
        .flatMap(DatasetData::getJobUuids)
        .hasSize(1)
        .containsAll(jobIds);
  }

  @Test
  public void testGetInputDatasetsWithJobThatHasNoDatasets() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());
    Set<UUID> jobIds = Collections.singleton(writeJob.getJob().getUuid());
    List<DatasetData> inputDatasets = lineageDao.getInputDatasetsFromJobIds(jobIds);
    assertThat(inputDatasets).isEmpty();
  }

  @Test
  public void testGetInputDatasetsWithJobThatHasOnlyOutputs() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    List<DatasetData> inputDatasets =
        lineageDao.getInputDatasetsFromJobIds(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(inputDatasets).hasSize(1).flatMap(DatasetData::getJobUuids).isEmpty();
  }

  /** A failed consumer job doesn't show up in the datasets out edges */
  @Test
  public void testGetInputDatasetsWithFailedConsumer() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "failedConsumer",
        "FAILED",
        jobFacet,
        Arrays.asList(dataset),
        Arrays.asList());
    List<DatasetData> inputDatasets =
        lineageDao.getInputDatasetsFromJobIds(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(inputDatasets).hasSize(1).flatMap(DatasetData::getJobUuids).isEmpty();
  }

  /**
   * Test that a job with multiple versions will only return the datasets touched by the latest
   * version.
   */
  @Test
  public void testGetInputDatasetsWithJobThatHasMultipleVersions() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));

    writeDownstreamLineage(
        openLineageDao,
        new LinkedList<>(
            Arrays.asList(
                new DatasetConsumerJob("readJob", 3, Optional.of("outputData")),
                new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
        jobFacet,
        dataset);

    JobFacet newVersionFacet =
        JobFacet.builder()
            .sourceCodeLocation(
                SourceCodeLocationJobFacet.builder().url("git@github:location").build())
            .additional(LineageTestUtils.EMPTY_MAP)
            .build();

    // readJobV2 produces outputData2 and not outputData
    List<JobLineage> newRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 3, Optional.of("outputData2")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            newVersionFacet,
            dataset);

    List<DatasetData> inputData =
        lineageDao.getInputDatasetsFromJobIds(Collections.singleton(newRows.get(0).getId()));
    assertThat(inputData)
        .hasSize(2)
        .map(ds -> ds.getName().getValue())
        .containsAll(Arrays.asList("outputData2<-readJob0<-commonDataset", "commonDataset"))
        .doesNotContain("outputData<-readJob0<-commonDataset");

    assertThat(inputData)
        .filteredOn(ds -> ds.getName().getValue().equals("outputData2<-readJob0<-commonDataset"))
        .isNotEmpty()
        .map(DatasetData::getJobUuids)
        .first()
        .asList()
        .hasSize(1)
        .contains(newRows.get(0).getDownstreamJobs().get(0).getId());
  }

  /** */
  @Test
  public void testGetOutputDatasetsFromJobIds() {

    LineageTestUtils.createLineageRow(
        openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList(dataset));

    // create a graph starting with the commonDataset, which spawns 20 consumers.
    // each of those spawns 3 consumers of the "intermediate" dataset
    // each of those spawns 1 consumer of the "finalOutput" dataset
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("intermediate")),
                    new DatasetConsumerJob("downstreamJob", 3, Optional.of("finalOutput")),
                    new DatasetConsumerJob("finalConsumer", 1, Optional.empty()))),
            jobFacet,
            dataset);
    // sanity check
    List<JobLineage> downstreamJobs = jobRows.get(0).getDownstreamJobs();
    assertThat(downstreamJobs).hasSize(3);

    // fetch the dataset lineage for the first "downstreamJob" job. It touches the "intermediate"
    // dataset, which is its input, and the "finalOutput", which is its output.
    JobLineage firstDownstream = downstreamJobs.get(0);
    List<DatasetData> outputDatasets =
        lineageDao.getOutputDatasetsFromJobIds(Collections.singleton(firstDownstream.getId()));

    // two datasets- intermediate and finalOutput
    assertThat(outputDatasets)
        .hasSize(2)
        .map(DatasetData::getName)
        .map(DatasetName::getValue)
        .containsAll(
            Arrays.asList(
                "finalOutput<-downstreamJob0<-intermediate<-readJob0<-commonDataset",
                "intermediate<-readJob0<-commonDataset"));

    // find the jobs that output those datasets
    assertThat(outputDatasets)
        .map(DatasetData::getJobUuids)
        .allMatch(l -> l.size() == 1)
        .flatMap(Function.identity())
        .containsAll(Arrays.asList(firstDownstream.getId(), jobRows.get(0).getId()));
  }

  /**
   * Validate a job that consumes a dataset, but shares no datasets with any other job returns only
   * the consumed dataset
   */
  @Test
  public void testGetOutputDatasetsWithJobThatSharesNoDatasets() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));

    // write a new dataset with a different name
    Dataset anotherDataset =
        new Dataset(
            NAMESPACE,
            "anUncommonDataset",
            newDatasetFacet(
                new SchemaField("firstname", "string", "the first name"),
                new SchemaField("lastname", "string", "the last name"),
                new SchemaField("birthdate", "date", "the date of birth")));
    // write a bunch of jobs that share nothing with the writeJob
    writeDownstreamLineage(
        openLineageDao,
        Arrays.asList(new DatasetConsumerJob("consumer", 5, Optional.empty())),
        jobFacet,
        anotherDataset);

    Set<UUID> jobIds = Collections.singleton(writeJob.getJob().getUuid());
    List<DatasetData> outputDatasets = lineageDao.getOutputDatasetsFromJobIds(jobIds);
    assertThat(outputDatasets)
        .hasSize(1)
        .flatMap(DatasetData::getJobUuids)
        .hasSize(1)
        .containsAll(jobIds);
  }

  @Test
  public void testGetOutputDatasetsWithJobThatHasNoDatasets() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());
    List<DatasetData> lineage =
        lineageDao.getOutputDatasetsFromJobIds(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(lineage).isEmpty();
  }

  @Test
  public void testGetOutputDatasetsWithJobThatHasOnlyInputs() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset),
            Arrays.asList());
    Set<UUID> jobIds = Collections.singleton(writeJob.getJob().getUuid());
    List<DatasetData> outputDatasets = lineageDao.getOutputDatasetsFromJobIds(jobIds);
    assertThat(outputDatasets).hasSize(1).flatMap(DatasetData::getJobUuids).isEmpty();
  }

  /** A failed producer job doesn't show up in the datasets in edges */
  @Test
  public void testGetOutputDatasetsWithFailedProducer() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "failedProducer",
        "FAILED",
        jobFacet,
        Arrays.asList(),
        Arrays.asList(dataset));
    List<DatasetData> inputDatasets =
        lineageDao.getOutputDatasetsFromJobIds(Collections.singleton(writeJob.getJob().getUuid()));
    assertThat(inputDatasets)
        .hasSize(1)
        .flatMap(DatasetData::getJobUuids)
        .hasSize(1)
        .contains(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetOuptutDatasetsWithJobThatHasMultipleVersions() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(dataset));

    writeDownstreamLineage(
        openLineageDao,
        new LinkedList<>(
            Arrays.asList(
                new DatasetConsumerJob("readJob", 3, Optional.of("outputData")),
                new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
        jobFacet,
        dataset);

    JobFacet newVersionFacet =
        JobFacet.builder()
            .sourceCodeLocation(
                SourceCodeLocationJobFacet.builder().url("git@github:location").build())
            .additional(LineageTestUtils.EMPTY_MAP)
            .build();

    // readJobV2 produces outputData2 and not outputData
    List<JobLineage> newRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 3, Optional.of("outputData2")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            newVersionFacet,
            dataset);

    List<DatasetData> inputData =
        lineageDao.getOutputDatasetsFromJobIds(Collections.singleton(newRows.get(0).getId()));
    assertThat(inputData)
        .hasSize(2)
        .map(ds -> ds.getName().getValue())
        .containsAll(Arrays.asList("outputData2<-readJob0<-commonDataset", "commonDataset"))
        .doesNotContain("outputData<-readJob0<-commonDataset");

    assertThat(inputData)
        .filteredOn(ds -> ds.getName().getValue().equals("outputData2<-readJob0<-commonDataset"))
        .isNotEmpty()
        .map(DatasetData::getJobUuids)
        .first()
        .asList()
        .hasSize(1)
        .contains(newRows.get(0).getId());
  }
}
