/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.db.DbTestUtils.createJobWithSymlinkTarget;
import static marquez.db.DbTestUtils.createJobWithoutSymlinkTarget;
import static marquez.db.DbTestUtils.newJobWith;
import static marquez.service.models.ServiceModelGenerator.newJobMetaWith;
import static org.assertj.core.api.Assertions.assertThat;

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
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
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

  static NamespaceRow namespaceRow;
  static JobRow jobRow;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    RunDaoTest.jdbi = jdbi;
    runDao = jdbi.onDemand(RunDao.class);
    jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    namespaceRow = DbTestUtils.newNamespace(jdbi);
    jobRow = DbTestUtils.newJob(jdbi, namespaceRow.getName(), newJobName().getValue());
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM datasets_tag_mapping");
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
  public void getRun() {

    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        newJobWith(jdbi, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    final RunRow runRow = DbTestUtils.newRun(jdbi, jobRow);
    DbTestUtils.transitionRunWithOutputs(
        jdbi, runRow.getUuid(), RunState.COMPLETED, jobMeta.getOutputs());

    jobVersionDao.upsertJobVersionOnRunTransition(
        jobRow, runRow.getUuid(), RunState.COMPLETED, Instant.now());

    Optional<Run> run = runDao.findRunByUuid(runRow.getUuid());
    assertThat(run)
        .isPresent()
        .get()
        .extracting(Run::getInputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(jobMeta.getInputs().size())
        .map(DatasetVersionId::getName)
        .containsAll(
            jobMeta.getInputs().stream().map(DatasetId::getName).collect(Collectors.toSet()));

    assertThat(run)
        .get()
        .extracting(Run::getOutputVersions, InstanceOfAssertFactories.list(DatasetVersionId.class))
        .hasSize(jobMeta.getOutputs().size())
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
                  jobRow, runRow.getUuid(), RunState.COMPLETED, Instant.now());
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
            namespaceRow.getUuid(),
            namespaceRow.getName(),
            jobRow.getName(),
            null,
            null);

    assertThat(row.getUuid()).isEqualTo(updatedRow.getUuid());
    assertThat(row.getNominalStartTime()).isNotNull();
    assertThat(row.getNominalEndTime()).isNotNull();
    assertThat(updatedRow.getNominalStartTime()).isEqualTo(row.getNominalStartTime());
    assertThat(updatedRow.getNominalEndTime()).isEqualTo(row.getNominalEndTime());
  }
}
