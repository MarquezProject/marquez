package marquez.db;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.OPEN_LINEAGE;
import static marquez.db.LineageTestUtils.newDatasetFacet;
import static marquez.db.LineageTestUtils.writeDownstreamLineage;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Functions;
import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import marquez.db.LineageTestUtils.DatasetConsumerJob;
import marquez.db.LineageTestUtils.JobLineage;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Run;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ObjectAssert;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class LineageDaoTest {
  private static LineageDao lineageDao;
  private static OpenLineageDao openLineageDao;
  private final OpenLineage.InputDataset inputDataset =
      OPEN_LINEAGE.newInputDataset(
          NAMESPACE,
          "commonDataset",
          newDatasetFacet(
              OPEN_LINEAGE.newSchemaDatasetFacetFields("firstname", "string", "the first name"),
              OPEN_LINEAGE.newSchemaDatasetFacetFields("lastname", "string", "the last name"),
              OPEN_LINEAGE.newSchemaDatasetFacetFields("birthdate", "date", "the date of birth")),
          null);
  private final OpenLineage.OutputDataset outputDataset =
      OPEN_LINEAGE.newOutputDataset(
          NAMESPACE,
          "commonDataset",
          newDatasetFacet(
              OPEN_LINEAGE.newSchemaDatasetFacetFields("firstname", "string", "the first name"),
              OPEN_LINEAGE.newSchemaDatasetFacetFields("lastname", "string", "the last name"),
              OPEN_LINEAGE.newSchemaDatasetFacetFields("birthdate", "date", "the date of birth")),
          null);

  private final OpenLineage.JobFacets jobFacet = OPEN_LINEAGE.newJobFacets(null, null, null);

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
            Arrays.asList(outputDataset));
    List<JobLineage> jobRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 20, Optional.of("outputData")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            jobFacet,
            outputDataset);

    // don't expect a failed job in the returned lineage
    UpdateLineageRow failedJobRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "readJobFailed",
            "FAILED",
            jobFacet,
            Arrays.asList(inputDataset),
            Arrays.asList());

    // don't expect a disjoint job in the returned lineage
    UpdateLineageRow disjointJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeRandomDataset",
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    NAMESPACE,
                    "randomDataset",
                    newDatasetFacet(
                        OPEN_LINEAGE.newSchemaDatasetFacetFields(
                            "firstname", "string", "the first name"),
                        OPEN_LINEAGE.newSchemaDatasetFacetFields(
                            "lastname", "string", "the last name")),
                    null)),
            Arrays.asList());
    // fetch the first "readJob" lineage.
    Set<JobData> connectedJobs =
        lineageDao.getLineage(new HashSet<>(Arrays.asList(jobRows.get(0).getId())), 2);

    // 20 readJobs + 1 downstreamJob for each (20) + 1 write job = 41
    assertThat(connectedJobs).size().isEqualTo(41);

    Set<UUID> jobIds = connectedJobs.stream().map(JobData::getUuid).collect(Collectors.toSet());
    // expect the job that wrote "commonDataset", which is readJob0's input
    assertThat(jobIds).contains(writeJob.getJob().getUuid());

    // expect all downstream jobs
    Set<UUID> readJobUUIDs =
        jobRows.stream()
            .flatMap(row -> Stream.concat(Stream.of(row), row.getDownstreamJobs().stream()))
            .map(JobLineage::getId)
            .collect(Collectors.toSet());
    assertThat(jobIds).containsAll(readJobUUIDs);

    // expect that the failed job that reads the same input dataset is not present
    assertThat(jobIds).doesNotContain(failedJobRow.getJob().getUuid());

    // expect that the disjoint job that reads a random dataset is not present
    assertThat(jobIds).doesNotContain(disjointJob.getJob().getUuid());

    Map<UUID, JobData> actualJobRows =
        connectedJobs.stream().collect(Collectors.toMap(JobData::getUuid, Functions.identity()));
    for (JobLineage expected : jobRows) {
      JobData job = actualJobRows.get(expected.getId());
      assertThat(job.getInputUuids())
          .containsAll(
              expected.getInput().map(ds -> ds.getDatasetRow().getUuid()).stream()::iterator);
      assertThat(job.getOutputUuids())
          .containsAll(
              expected.getOutput().map(ds -> ds.getDatasetRow().getUuid()).stream()::iterator);
    }
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
            Arrays.asList(outputDataset));
    Set<UUID> lineage =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2).stream()
            .map(JobData::getUuid)
            .collect(Collectors.toSet());
    assertThat(lineage).hasSize(1).contains(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetLineageWithJobThatHasNoDatasets() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());
    Set<UUID> lineage =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2).stream()
            .map(JobData::getUuid)
            .collect(Collectors.toSet());

    assertThat(lineage).hasSize(1).first().isEqualTo(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetLineageWithNewJobInRunningState() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "RUNNING",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    Set<JobData> lineage =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2);

    // assert the job does exist
    ObjectAssert<JobData> writeAssert = assertThat(lineage).hasSize(1).first();
    writeAssert.extracting(JobData::getUuid).isEqualTo(writeJob.getJob().getUuid());

    // job in running state doesn't yet have any datasets in its lineage
    writeAssert
        .extracting(JobData::getOutputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .isEmpty();
    writeAssert
        .extracting(JobData::getInputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .isEmpty();
  }

  /**
   * Validate a job that consumes a dataset, but shares no datasets with any other job returns only
   * the consumed dataset
   */
  @Test
  public void testGetLineageWithJobThatSharesNoDatasets() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(inputDataset),
            Arrays.asList());

    // write a new dataset with a different name
    OpenLineage.OutputDataset anotherDataset =
        OPEN_LINEAGE.newOutputDataset(
            NAMESPACE,
            "anUncommonDataset",
            newDatasetFacet(
                OPEN_LINEAGE.newSchemaDatasetFacetFields("firstname", "string", "the first name"),
                OPEN_LINEAGE.newSchemaDatasetFacetFields("lastname", "string", "the last name"),
                OPEN_LINEAGE.newSchemaDatasetFacetFields("birthdate", "date", "the date of birth")),
            null);
    // write a bunch of jobs that share nothing with the writeJob
    writeDownstreamLineage(
        openLineageDao,
        Arrays.asList(new DatasetConsumerJob("consumer", 5, Optional.empty())),
        jobFacet,
        anotherDataset);

    // Validate that finalConsumer job only has a single dataset
    Set<UUID> jobIds = Collections.singleton(writeJob.getJob().getUuid());
    Set<JobData> finalConsumer = lineageDao.getLineage(jobIds, 2);
    assertThat(finalConsumer).hasSize(1).flatMap(JobData::getUuid).hasSize(1).containsAll(jobIds);
  }

  /** A failed consumer job doesn't show up in the datasets out edges */
  @Test
  public void testGetLineageWithFailedConsumer() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "failedConsumer",
        "FAILED",
        jobFacet,
        Arrays.asList(inputDataset),
        Arrays.asList());
    Set<JobData> lineage =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2);

    assertThat(lineage)
        .hasSize(1)
        .extracting(JobData::getUuid)
        .contains(writeJob.getJob().getUuid());
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
            Arrays.asList(outputDataset));

    writeDownstreamLineage(
        openLineageDao,
        new LinkedList<>(
            Arrays.asList(
                new DatasetConsumerJob("readJob", 3, Optional.of("outputData")),
                new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
        jobFacet,
        outputDataset);

    OpenLineage.JobFacets newVersionFacet =
        OPEN_LINEAGE
            .newJobFacetsBuilder()
            .sourceCodeLocation(
                OPEN_LINEAGE
                    .newSourceCodeLocationJobFacetBuilder()
                    .url(URI.create("git://git@github:location"))
                    .build())
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
            outputDataset);

    Set<JobData> lineage =
        lineageDao.getLineage(
            new HashSet<>(
                Arrays.asList(
                    newRows.get(0).getId(), newRows.get(0).getDownstreamJobs().get(0).getId())),
            2);
    assertThat(lineage)
        .hasSize(7)
        .extracting(JobData::getUuid)
        .containsAll(
            newRows.stream()
                    .flatMap(r -> Stream.concat(Stream.of(r), r.getDownstreamJobs().stream()))
                    .map(JobLineage::getId)
                ::iterator);
    assertThat(lineage)
        .filteredOn(r -> r.getName().getValue().equals("readJob0<-commonDataset"))
        .hasSize(1)
        .first()
        .extracting(JobData::getOutputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .hasSize(1)
        .first()
        .isEqualTo(newRows.get(0).getOutput().get().getDatasetRow().getUuid());

    assertThat(lineage)
        .filteredOn(
            r ->
                r.getName()
                    .getValue()
                    .equals("downstreamJob0<-outputData2<-readJob0<-commonDataset"))
        .hasSize(1)
        .first()
        .extracting(JobData::getInputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .hasSize(1)
        .first()
        .isEqualTo(
            newRows.get(0).getDownstreamJobs().get(0).getInput().get().getDatasetRow().getUuid());
    assertThat(lineage)
        .filteredOn(
            r ->
                r.getName()
                    .getValue()
                    .equals("downstreamJob0<-outputData2<-readJob0<-commonDataset"))
        .hasSize(1)
        .first()
        .extracting(JobData::getOutputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .isEmpty();
  }

  /** A failed producer job doesn't show up in the lineage */
  @Test
  public void testGetLineageWithFailedProducer() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "failedProducer",
        "FAILED",
        jobFacet,
        Arrays.asList(),
        Arrays.asList(outputDataset));
    Set<JobData> inputDatasets =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2);
    assertThat(inputDatasets)
        .hasSize(1)
        .flatMap(JobData::getUuid)
        .hasSize(1)
        .contains(writeJob.getJob().getUuid());
  }

  /** A failed producer job doesn't show up in the lineage */
  @Test
  public void testGetLineageChangedJobVersion() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    LineageTestUtils.createLineageRow(
        openLineageDao, "writeJob", "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList());

    // the new job is still returned, even though it isn't connected
    Set<JobData> jobData =
        lineageDao.getLineage(Collections.singleton(writeJob.getJob().getUuid()), 2);
    assertThat(jobData)
        .hasSize(1)
        .first()
        .matches(jd -> jd.getUuid().equals(writeJob.getJob().getUuid()))
        .extracting(JobData::getOutputUuids, InstanceOfAssertFactories.iterable(UUID.class))
        .isEmpty();
  }

  @Test
  public void testGetJobFromInputOrOutput() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "consumerJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(inputDataset),
        Arrays.asList());
    Optional<UUID> jobNode =
        lineageDao.getJobFromInputOrOutput(inputDataset.getName(), inputDataset.getNamespace());
    assertThat(jobNode).isPresent().get().isEqualTo(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetJobFromInputOrOutputPrefersRecentOutputJob() {
    // add some consumer jobs prior to the write so we know that the sort isn't simply picking
    // the first job created
    for (int i = 0; i < 5; i++) {
      LineageTestUtils.createLineageRow(
          openLineageDao,
          "consumerJob" + i,
          "COMPLETE",
          jobFacet,
          Arrays.asList(inputDataset),
          Arrays.asList());
    }
    // older write job- should be ignored.
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "olderWriteJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(),
        Arrays.asList(outputDataset));

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "consumerJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(inputDataset),
        Arrays.asList());
    Optional<UUID> jobNode =
        lineageDao.getJobFromInputOrOutput(inputDataset.getName(), inputDataset.getNamespace());
    assertThat(jobNode).isPresent().get().isEqualTo(writeJob.getJob().getUuid());
  }

  @Test
  public void testGetDatasetData() {
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "writeJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(),
        Arrays.asList(outputDataset));
    List<JobLineage> newRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 3, Optional.of("outputData2")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            jobFacet,
            outputDataset);
    Set<DatasetData> datasetData =
        lineageDao.getDatasetData(
            newRows.stream()
                .map(j -> j.getOutput().get().getDatasetRow().getUuid())
                .collect(Collectors.toSet()));
    assertThat(datasetData)
        .hasSize(3)
        .extracting(ds -> ds.getName().getValue())
        .allMatch(str -> str.contains("outputData2"));
  }

  @Test
  public void testGetCurrentRuns() {

    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));
    List<JobLineage> newRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 3, Optional.of("outputData2")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            jobFacet,
            outputDataset);

    Set<UUID> expectedRunIds =
        Stream.concat(
                Stream.of(writeJob.getRun().getUuid()), newRows.stream().map(JobLineage::getRunId))
            .collect(Collectors.toSet());
    Set<UUID> jobids =
        Stream.concat(
                Stream.of(writeJob.getJob().getUuid()), newRows.stream().map(JobLineage::getId))
            .collect(Collectors.toSet());

    List<Run> currentRuns = lineageDao.getCurrentRuns(jobids);

    // assert the job does exist
    assertThat(currentRuns)
        .hasSize(expectedRunIds.size())
        .extracting(r -> r.getId().getValue())
        .containsAll(expectedRunIds);
  }

  @Test
  public void testGetCurrentRunsWithFailedJob() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "FAIL",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));

    Set<UUID> jobids = Collections.singleton(writeJob.getJob().getUuid());

    List<Run> currentRuns = lineageDao.getCurrentRuns(jobids);

    // assert the job does exist
    assertThat(currentRuns)
        .hasSize(1)
        .extracting(r -> r.getId().getValue())
        .contains(writeJob.getRun().getUuid());
  }

  @Test
  public void testGetCurrentRunsGetsLatestRun() {
    for (int i = 0; i < 5; i++) {
      LineageTestUtils.createLineageRow(
          openLineageDao,
          "writeJob",
          "COMPLETE",
          jobFacet,
          Arrays.asList(),
          Arrays.asList(outputDataset));
    }

    List<JobLineage> newRows =
        writeDownstreamLineage(
            openLineageDao,
            new LinkedList<>(
                Arrays.asList(
                    new DatasetConsumerJob("readJob", 3, Optional.of("outputData2")),
                    new DatasetConsumerJob("downstreamJob", 1, Optional.empty()))),
            jobFacet,
            outputDataset);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "writeJob",
            "FAIL",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(outputDataset));

    Set<UUID> expectedRunIds =
        Stream.concat(
                Stream.of(writeJob.getRun().getUuid()), newRows.stream().map(JobLineage::getRunId))
            .collect(Collectors.toSet());
    Set<UUID> jobids =
        Stream.concat(
                Stream.of(writeJob.getJob().getUuid()), newRows.stream().map(JobLineage::getId))
            .collect(Collectors.toSet());

    List<Run> currentRuns = lineageDao.getCurrentRuns(jobids);

    // assert the job does exist
    assertThat(currentRuns)
        .hasSize(expectedRunIds.size())
        .extracting(r -> r.getId().getValue())
        .containsAll(expectedRunIds);
  }
}
