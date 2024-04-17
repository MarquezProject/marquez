/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.db.DbTestUtils.createJobWithSymlinkTarget;
import static marquez.db.DbTestUtils.createJobWithoutSymlinkTarget;
import static marquez.db.DbTestUtils.newJobWith;
import static marquez.service.models.ServiceModelGenerator.newJobMetaWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import marquez.api.JdbiUtils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.InputDatasetVersion;
import marquez.common.models.NamespaceName;
import marquez.common.models.OutputDatasetVersion;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class RunDaoTest {

  private static RunDao runDao;
  private static Jdbi jdbi;
  private static JobVersionDao jobVersionDao;
  private static OpenLineageDao openLineageDao;

  static NamespaceRow namespaceRow;
  static JobRow jobRow;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    RunDaoTest.jdbi = jdbi;
    runDao = jdbi.onDemand(RunDao.class);
    jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    namespaceRow = DbTestUtils.newNamespace(jdbi);
    jobRow = DbTestUtils.newJob(jdbi, namespaceRow.getName(), newJobName().getValue());
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void getRun() {

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    final RunRow runRow = DbTestUtils.newRun(jdbi, jobRow);
    DbTestUtils.transitionRunWithOutputs(
        jdbi, runRow.getUuid(), RunState.COMPLETED, jobMeta.getOutputs());

    jobVersionDao.upsertJobVersionOnRunTransition(
        jobVersionDao.loadJobRowRunDetails(jobRow, runRow.getUuid()),
        RunState.COMPLETED,
        Instant.now(),
        true);

    Optional<Run> run = runDao.findRunByUuid(runRow.getUuid());
    assertThat(run)
        .isPresent()
        .get()
        .extracting(
            Run::getInputDatasetVersions, InstanceOfAssertFactories.list(InputDatasetVersion.class))
        .hasSize(jobMeta.getInputs().size())
        .map(InputDatasetVersion::getDatasetVersionId)
        .map(DatasetVersionId::getName)
        .containsAll(
            jobMeta.getInputs().stream().map(DatasetId::getName).collect(Collectors.toSet()));

    assertThat(run)
        .get()
        .extracting(
            Run::getOutputDatasetVersions,
            InstanceOfAssertFactories.list(OutputDatasetVersion.class))
        .hasSize(jobMeta.getOutputs().size())
        .map(OutputDatasetVersion::getDatasetVersionId)
        .map(DatasetVersionId::getName)
        .containsAll(
            jobMeta.getOutputs().stream().map(DatasetId::getName).collect(Collectors.toSet()));
  }

  @Test
  public void getFindAll() {

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    Set<RunRow> expectedRuns =
        createRunsForJob(jobRow, 5, jobMeta.getOutputs()).collect(Collectors.toSet());
    List<Run> runs = runDao.findAll(jobRow.getNamespaceName(), jobRow.getName(), 10, 0);
    assertThat(runs)
        .hasSize(expectedRuns.size())
        .map(Run::getId)
        .map(RunId::getValue)
        .containsAll(expectedRuns.stream().map(RunRow::getUuid).collect(Collectors.toSet()));
  }

  @Test
  public void getCountFor() {

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);
    
    Set<RunRow> expectedRuns =
        createRunsForJob(jobRow, 5, jobMeta.getOutputs()).collect(Collectors.toSet());
    Integer count = runDao.countFor(jobRow.getName());
    assertThat(count)
        .isEqualTo(expectedRuns.size());
  }

  @Test
  public void getFindAllForSymlinkedJob() {
    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    final JobRow symlinkJob =
        createJobWithSymlinkTarget(
            jdbi, namespaceRow, newJobName().getValue(), jobRow.getUuid(), "symlink job");

    Set<RunRow> expectedRuns =
        Stream.concat(
                createRunsForJob(symlinkJob, 3, jobMeta.getOutputs()),
                createRunsForJob(jobRow, 2, jobMeta.getOutputs()))
            .collect(Collectors.toSet());

    // all runs should be present
    List<Run> runs = runDao.findAll(jobRow.getNamespaceName(), jobRow.getName(), 10, 0);
    assertThat(runs)
        .hasSize(expectedRuns.size())
        .map(Run::getId)
        .map(RunId::getValue)
        .containsAll(expectedRuns.stream().map(RunRow::getUuid).collect(Collectors.toSet()));
  }

  @Test
  public void testFindByLatestJob() {
    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);
    Set<RunRow> runs =
        createRunsForJob(jobRow, 5, jobMeta.getOutputs()).collect(Collectors.toSet());

    TreeSet<RunRow> sortedRuns =
        new TreeSet<>(Comparator.comparing(RunRow::getUpdatedAt).reversed());
    sortedRuns.addAll(runs);
    Optional<Run> byLatestJob = runDao.findByLatestJob(jobRow.getNamespaceName(), jobRow.getName());
    assertThat(byLatestJob)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("id", new RunId(sortedRuns.first().getUuid()));

    JobRow newTargetJob =
        createJobWithoutSymlinkTarget(jdbi, namespaceRow, "newTargetJob", "a symlink target");

    // update the old job to point to the new targets
    createJobWithSymlinkTarget(
        jdbi,
        namespaceRow,
        jobRow.getName(),
        newTargetJob.getUuid(),
        jobMeta.getDescription().orElse(null));

    // get the latest run for the *newTargetJob*. It should be the same as the old job's latest run
    byLatestJob = runDao.findByLatestJob(newTargetJob.getNamespaceName(), newTargetJob.getName());
    assertThat(byLatestJob)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("id", new RunId(sortedRuns.first().getUuid()));
  }

  @NotNull
  private Stream<RunRow> createRunsForJob(
      JobRow jobRow, int count, ImmutableSet<DatasetId> outputs) {
    return IntStream.range(0, count)
        .mapToObj(
            i -> {
              final RunRow runRow = DbTestUtils.newRun(jdbi, jobRow);
              DbTestUtils.transitionRunWithOutputs(
                  jdbi, runRow.getUuid(), RunState.COMPLETED, outputs);

              jobVersionDao.upsertJobVersionOnRunTransition(
                  jobVersionDao.loadJobRowRunDetails(jobRow, runRow.getUuid()),
                  RunState.COMPLETED,
                  Instant.now(),
                  true);
              return runRow;
            });
  }

  @Test
  public void updateRowWithNullNominalTimeDoesNotUpdateNominalTime() {
    final RunDao runDao = jdbi.onDemand(RunDao.class);

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    RunRow row = DbTestUtils.newRun(jdbi, jobRow);

    RunRow updatedRow =
        runDao.upsert(
            row.getUuid(),
            null,
            row.getUuid().toString(),
            row.getUpdatedAt(),
            jobRow.getUuid(),
            null,
            row.getRunArgsUuid(),
            null,
            null,
            namespaceRow.getName(),
            jobRow.getName(),
            null);

    assertThat(row.getUuid()).isEqualTo(updatedRow.getUuid());
    assertThat(row.getNominalStartTime()).isNotNull();
    assertThat(row.getNominalEndTime()).isNotNull();
    assertThat(updatedRow.getNominalStartTime()).isEqualTo(row.getNominalStartTime());
    assertThat(updatedRow.getNominalEndTime()).isEqualTo(row.getNominalEndTime());
  }

  @Test
  public void updateRowWithExternalId() {
    final RunDao runDao = jdbi.onDemand(RunDao.class);

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    RunRow row = DbTestUtils.newRun(jdbi, jobRow);

    runDao.upsert(
        row.getUuid(),
        null,
        row.getUuid().toString(),
        row.getUpdatedAt(),
        jobRow.getUuid(),
        null,
        row.getRunArgsUuid(),
        null,
        null,
        namespaceRow.getName(),
        jobRow.getName(),
        null);

    runDao.upsert(
        row.getUuid(),
        null,
        "updated-external-id",
        row.getUpdatedAt(),
        jobRow.getUuid(),
        null,
        row.getRunArgsUuid(),
        null,
        null,
        namespaceRow.getName(),
        jobRow.getName(),
        null);

    Optional<ExtendedRunRow> runRowOpt = runDao.findRunByUuidAsExtendedRow(row.getUuid());
    assertThat(runRowOpt)
        .isPresent()
        .get()
        .extracting("externalId")
        .isEqualTo("updated-external-id");
  }
}
