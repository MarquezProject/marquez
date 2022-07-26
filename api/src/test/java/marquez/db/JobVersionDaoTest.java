/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.Generator.newTimestamp;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newVersion;
import static marquez.db.JobVersionDao.BagOfJobVersionInfo;
import static marquez.db.models.DbModelGenerator.newRowUuid;
import static marquez.service.models.ServiceModelGenerator.newJobMetaWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.BaseIntegrationTest;
import marquez.api.models.JobVersion;
import marquez.common.models.DatasetId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;
import marquez.common.models.Version;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** The test suite for {@link JobVersionDao}. */
@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class JobVersionDaoTest extends BaseIntegrationTest {
  static Jdbi jdbiForTesting;
  static DatasetVersionDao datasetVersionDao;
  static JobDao jobDao;
  static RunDao runDao;
  static JobVersionDao jobVersionDao;

  static NamespaceRow namespaceRow;
  static JobRow jobRow;

  @BeforeAll
  public static void setUpOnce(final Jdbi jdbi) {
    jdbiForTesting = jdbi;
    datasetVersionDao = jdbiForTesting.onDemand(DatasetVersionDao.class);
    jobDao = jdbi.onDemand(JobDao.class);
    runDao = jdbi.onDemand(RunDao.class);
    jobVersionDao = jdbiForTesting.onDemand(JobVersionDao.class);

    // Each tests requires both a namespace and job row.
    namespaceRow = DbTestUtils.newNamespace(jdbiForTesting);
    jobRow = DbTestUtils.newJob(jdbiForTesting, namespaceRow.getName(), newJobName().getValue());
  }

  @Test
  public void testUpsertJobVersion() {
    // Use a randomly generated job version. We'll attempt to associate multiple job versions with
    // the same version; only the first attempt will insert the job version row successfully.
    final Version version = newVersion();

    // (1) Add a new job version; no conflict on version.
    final int rowsBefore = jobVersionDao.count();
    jobVersionDao.upsertJobVersion(
        newRowUuid(),
        newTimestamp(),
        jobRow.getUuid(),
        jobRow.getJobContextUuid().get(),
        newLocation().toString(),
        version.getValue(),
        jobRow.getName(),
        namespaceRow.getUuid(),
        namespaceRow.getName());

    final int rowsAfter = jobVersionDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);

    // (2) Add another job version; conflict on version, not inserted.
    final int rowsBeforeConflict = jobVersionDao.count();
    jobVersionDao.upsertJobVersion(
        newRowUuid(),
        newTimestamp(),
        jobRow.getUuid(),
        jobRow.getJobContextUuid().get(),
        newLocation().toString(),
        version.getValue(),
        jobRow.getName(),
        namespaceRow.getUuid(),
        namespaceRow.getName());

    final int rowsAfterConflict = jobVersionDao.count();
    assertThat(rowsAfterConflict).isEqualTo(rowsBeforeConflict);
    Optional<JobVersion> jobVersion =
        jobVersionDao.findJobVersion(
            jobRow.getNamespaceName(), jobRow.getName(), version.getValue());
    assertThat(jobVersion).isPresent();
  }

  @Test
  public void testUpdateLatestRunFor() {
    // (1) Add a new job version.
    final ExtendedJobVersionRow jobVersionRow =
        jobVersionDao.upsertJobVersion(
            newRowUuid(),
            newTimestamp(),
            jobRow.getUuid(),
            jobRow.getJobContextUuid().get(),
            newLocation().toString(),
            newVersion().getValue(),
            jobRow.getName(),
            namespaceRow.getUuid(),
            namespaceRow.getName());
    assertThat(jobVersionRow.getLatestRunUuid()).isNotPresent();

    // (2) Add a new run.
    final RunArgsRow runArgsRow = DbTestUtils.newRunArgs(jdbiForTesting);
    final RunRow runRow =
        DbTestUtils.newRun(
            jdbiForTesting,
            jobVersionRow.getJobUuid(),
            jobVersionRow.getUuid(),
            runArgsRow.getUuid(),
            namespaceRow.getUuid(),
            namespaceRow.getName(),
            jobVersionRow.getJobName(),
            jobVersionRow.getLocation().orElse(null),
            jobVersionRow.getJobContextUuid());

    // Ensure the latest run is not associated with the job version.
    final Optional<UUID> noLatestRunUuid = jobVersionDao.findLatestRunFor(jobVersionRow.getUuid());
    assertThat(noLatestRunUuid).isNotPresent();

    // (3) Link latest run with the job version.
    jobVersionDao.updateLatestRunFor(jobVersionRow.getUuid(), newTimestamp(), runRow.getUuid());

    // Ensure the latest run is associated with the job version.
    final Optional<UUID> latestRunUuid = jobVersionDao.findLatestRunFor(jobVersionRow.getUuid());
    assertThat(latestRunUuid).isPresent().contains(runRow.getUuid());
  }

  @Test
  public void testGetJobVersion() {
    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        DbTestUtils.newJobWith(
            jdbiForTesting, namespaceRow.getName(), newJobName().getValue(), jobMeta);
    Version version = newVersion();
    final ExtendedJobVersionRow jobVersionRow =
        jobVersionDao.upsertJobVersion(
            newRowUuid(),
            newTimestamp(),
            jobRow.getUuid(),
            jobRow.getJobContextUuid().get(),
            newLocation().toString(),
            version.getValue(),
            jobRow.getName(),
            namespaceRow.getUuid(),
            namespaceRow.getName());
    DatasetDao datasetDao = jdbiForTesting.onDemand(DatasetDao.class);
    for (DatasetId ds : jobMeta.getInputs()) {
      DatasetRow dataset =
          datasetDao
              .findDatasetAsRow(ds.getNamespace().getValue(), ds.getName().getValue())
              .orElseThrow(
                  () -> new IllegalStateException("Can't find test dataset " + ds.getName()));

      jobVersionDao.upsertInputDatasetFor(jobVersionRow.getUuid(), dataset.getUuid());
    }
    for (DatasetId ds : jobMeta.getOutputs()) {
      DatasetRow dataset =
          datasetDao
              .findDatasetAsRow(ds.getNamespace().getValue(), ds.getName().getValue())
              .orElseThrow(
                  () -> new IllegalStateException("Can't find test dataset " + ds.getName()));

      jobVersionDao.upsertOutputDatasetFor(jobVersionRow.getUuid(), dataset.getUuid());
    }
    Optional<JobVersion> jobVersion =
        jobVersionDao.findJobVersion(namespaceRow.getName(), jobRow.getName(), version.getValue());
    assertThat(jobVersion)
        .isPresent()
        .get()
        .extracting(JobVersion::getInputs, InstanceOfAssertFactories.list(DatasetId.class))
        .containsAll(jobMeta.getInputs());
    assertThat(jobVersion)
        .get()
        .extracting(JobVersion::getOutputs, InstanceOfAssertFactories.list(DatasetId.class))
        .containsAll(jobMeta.getOutputs());
    assertThat(jobVersion).get().extracting(JobVersion::getLatestRun).isNull();
  }

  @Test
  public void testGetJobVersions() {
    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    final JobRow jobRow =
        DbTestUtils.newJobWith(
            jdbiForTesting, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    final RunRow runRow = DbTestUtils.newRun(jdbiForTesting, jobRow);
    final Run runCompleted =
        DbTestUtils.transitionRunWithOutputs(
            jdbiForTesting, runRow.getUuid(), RunState.COMPLETED, jobMeta.getOutputs());

    jobVersionDao.upsertJobVersionOnRunTransition(
        jobRow, runRow.getUuid(), RunState.COMPLETED, Instant.now());

    List<JobVersion> jobVersions =
        jobVersionDao.findAllJobVersions(namespaceRow.getName(), jobRow.getName(), 10, 0);
    assertThat(jobVersions)
        .hasSize(1)
        .first()
        .extracting(JobVersion::getInputs, InstanceOfAssertFactories.list(DatasetId.class))
        .containsAll(jobMeta.getInputs());

    assertThat(jobVersions)
        .hasSize(1)
        .first()
        .extracting(JobVersion::getLatestRun)
        .isNotNull()
        .extracting(Run::getId)
        .isEqualTo(runCompleted.getId());
  }

  @Test
  public void testUpsertJobVersionOnRunTransition() {
    // Generate a new job meta object with an existing namespace; the namespace will also be
    // associated with the input and output datasets for the job.
    final JobMeta jobMeta = newJobMetaWith(NamespaceName.of(namespaceRow.getName()));
    // (1) Add a new job; the input and output datasets for the job will also be added.
    final JobRow jobRow =
        DbTestUtils.newJobWith(
            jdbiForTesting, namespaceRow.getName(), newJobName().getValue(), jobMeta);

    // (2) Add a new run; the input dataset versions will also be associated with the run.
    final RunRow runRow = DbTestUtils.newRun(jdbiForTesting, jobRow);

    // Ensure the input dataset versions have been associated with the run.
    final List<ExtendedDatasetVersionRow> inputDatasetVersions =
        datasetVersionDao.findInputDatasetVersionsFor(runRow.getUuid());
    assertThat(inputDatasetVersions).hasSize(jobMeta.getInputs().size());

    // Ensure a run with the state NEW has no output dataset versions.
    final List<ExtendedDatasetVersionRow> noOutputDatasetVersions =
        datasetVersionDao.findOutputDatasetVersionsFor(runRow.getUuid());
    assertThat(noOutputDatasetVersions).isEmpty();

    // (4) Transition the run from NEW to RUNNING.
    final Run runStarted =
        DbTestUtils.transitionRunTo(jdbiForTesting, runRow.getUuid(), RunState.RUNNING);
    assertThat(runStarted.getState()).isEqualTo(RunState.RUNNING);
    assertThat(runStarted.getStartedAt()).isNotNull();

    // (5) Transition the run from RUNNING to COMPLETED.
    final Run runCompleted =
        DbTestUtils.transitionRunWithOutputs(
            jdbiForTesting, runRow.getUuid(), RunState.COMPLETED, jobMeta.getOutputs());
    assertThat(runCompleted.getState()).isEqualTo(RunState.COMPLETED);
    assertThat(runCompleted.getEndedAt()).isNotNull();
    assertThat(runCompleted.getDurationMs()).isPresent();

    // Ensure the output dataset versions have been associated with the run.
    final List<ExtendedDatasetVersionRow> outputDatasetVersions =
        datasetVersionDao.findOutputDatasetVersionsFor(runRow.getUuid());
    assertThat(outputDatasetVersions).hasSize(jobMeta.getOutputs().size());

    // Ensure the latest run not associated with a job version.
    final Optional<ExtendedJobVersionRow> jobVersionRow =
        jobVersionDao.findJobVersionFor(runRow.getUuid());
    assertThat(jobVersionRow).isNotPresent();

    // (6) Add a new job version on the run state transition to COMPLETED.
    final BagOfJobVersionInfo bagOfJobVersionInfo =
        jobVersionDao.upsertJobVersionOnRunTransition(
            jobRow, runRow.getUuid(), RunState.COMPLETED, newTimestamp());

    // Ensure the job version is associated with the latest run.
    final RunRow latestRunRowForJobVersion = runDao.findRunByUuidAsRow(runRow.getUuid()).get();
    assertThat(latestRunRowForJobVersion.getJobVersionUuid())
        .isPresent()
        .contains(bagOfJobVersionInfo.getJobVersionRow().getUuid());

    // Ensure the latest run is associated with the job version.
    final Optional<UUID> latestRunUuid =
        jobVersionDao.findLatestRunFor(bagOfJobVersionInfo.getJobVersionRow().getUuid());
    assertThat(latestRunUuid).isPresent().contains(runRow.getUuid());

    // Ensure the latest version is associated with the job.
    final JobRow jobRowForLatestRun =
        jobDao.findJobByNameAsRow(jobRow.getNamespaceName(), jobRow.getName()).get();
    assertThat(jobRowForLatestRun.getCurrentVersionUuid())
        .isPresent()
        .contains(bagOfJobVersionInfo.getJobVersionRow().getUuid());

    // Ensure the input datasets have been linked to the job version.
    final List<UUID> jobVersionInputDatasetUuids =
        jobVersionDao.findInputDatasetsFor(bagOfJobVersionInfo.getJobVersionRow().getUuid());
    assertThat(jobVersionInputDatasetUuids).hasSize(bagOfJobVersionInfo.getInputs().size());
    for (final ExtendedDatasetVersionRow jobVersionInputDatasetUuid :
        bagOfJobVersionInfo.getInputs()) {
      assertThat(jobVersionInputDatasetUuids).contains(jobVersionInputDatasetUuid.getDatasetUuid());
    }

    // Ensure the output datasets have been linked to the job version.
    final List<UUID> jobVersionOutputDatasetUuids =
        jobVersionDao.findOutputDatasetsFor(bagOfJobVersionInfo.getJobVersionRow().getUuid());
    assertThat(jobVersionOutputDatasetUuids).hasSize(bagOfJobVersionInfo.getOutputs().size());
    for (final ExtendedDatasetVersionRow outputDatasetVersion : bagOfJobVersionInfo.getOutputs()) {
      assertThat(jobVersionOutputDatasetUuids).contains(outputDatasetVersion.getDatasetUuid());
    }
    Optional<JobVersion> jobVersion =
        jobVersionDao.findJobVersion(
            jobRow.getNamespaceName(),
            jobRow.getName(),
            bagOfJobVersionInfo.getJobVersionRow().getVersion());
    assertThat(jobVersion)
        .isPresent()
        .get()
        .extracting(JobVersion::getInputs, InstanceOfAssertFactories.list(UUID.class))
        .isNotEmpty();
  }
}
