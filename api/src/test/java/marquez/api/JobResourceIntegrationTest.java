/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static marquez.api.models.ApiModelGenerator.newOlActiveRun;
import static marquez.api.models.ApiModelGenerator.newOlActiveRuns;
import static marquez.api.models.ApiModelGenerator.newOlActiveRunsFor;
import static marquez.client.models.RunState.COMPLETED;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.collect.ImmutableList;
import io.openlineage.client.OpenLineage;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import marquez.api.models.ActiveRun;
import marquez.client.MarquezClientException;
import marquez.client.models.DatasetId;
import marquez.client.models.Job;
import marquez.client.models.JobVersion;
import marquez.client.models.Run;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** The test suite for {@link JobResource}. */
@Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class JobResourceIntegrationTest extends BaseResourceIntegrationTest {
  @Test
  public void testApp_getJob() {
    // (1) Start, then end OL run.
    final ActiveRun olActiveRun = newOlActiveRun(OL, OL_CLIENT, NAMESPACE_NAME);
    olActiveRun.startRun();
    olActiveRun.endRun();

    // Ensure run state COMPLETED.
    final Run runCompleted = MARQUEZ_CLIENT.getRun(olActiveRun.getRunId().toString());
    assertThat(runCompleted.getState()).isEqualTo(COMPLETED);

    // Ensure the job associated with the OL run has been added and a version
    // for the job has been created.
    final Job job = MARQUEZ_CLIENT.getJob(NAMESPACE_NAME, olActiveRun.getJobName());
    assertThat(job.getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(job.getName()).isEqualTo(olActiveRun.getJobName());
    assertThat(job.getInputs()).hasSize(olActiveRun.getInputs().size());
    assertThat(job.getOutputs()).hasSize(olActiveRun.getOutputs().size());
    assertThat(job.getCurrentVersion()).isNotEmpty();

    // Ensure the inputs / outputs associated with the OL run have been added to the job.
    for (final OpenLineage.InputDataset input : olActiveRun.getInputs()) {
      failIfNotIn(job.getInputs(), input);
    }
    for (final OpenLineage.OutputDataset output : olActiveRun.getOutputs()) {
      failIfNotIn(job.getOutputs(), output);
    }

    // Ensure the run has been associated with the job as latest, and COMPLETED
    final Run latestRunForJob = job.getLatestRun().orElseThrow();
    assertThat(latestRunForJob.getId()).isEqualTo(olActiveRun.getRunId().toString());
    assertThat(latestRunForJob.getState()).isEqualTo(COMPLETED);
  }

  @Test
  public void testApp_listJobs() {
    // (1) Start, then end OL runs.
    final ImmutableList<ActiveRun> olActiveRuns = newOlActiveRuns(OL, OL_CLIENT, NAMESPACE_NAME);
    for (final ActiveRun olActiveRun : olActiveRuns) {
      olActiveRun.startRun();
      olActiveRun.endRun();
    }

    // Ensure all jobs associated with the OL runs have been added.
    final List<Job> jobs = MARQUEZ_CLIENT.listJobs(NAMESPACE_NAME);
    for (final ActiveRun olActiveRun : olActiveRuns) {
      failIfNotIn(jobs, olActiveRun.getJobName());
    }
  }

  @Test
  public void testApp_getJobVersion() {
    // (1) Start, then end OL run.
    final ActiveRun olActiveRun = newOlActiveRun(OL, OL_CLIENT, NAMESPACE_NAME);
    olActiveRun.startRun();
    olActiveRun.endRun();

    // (2) Get job, then current version associated with job.
    final Job job = MARQUEZ_CLIENT.getJob(NAMESPACE_NAME, olActiveRun.getJobName());
    final UUID currentVersionForJob = job.getCurrentVersion().orElseThrow();
    final JobVersion currentJobVersion =
        MARQUEZ_CLIENT.getJobVersion(
            NAMESPACE_NAME, olActiveRun.getJobName(), currentVersionForJob.toString());
    assertThat(currentJobVersion.getVersion()).isEqualTo(currentVersionForJob);
  }

  @Test
  public void testApp_listJobVersions() {
    // (1) Start, then end OL run.
    final ActiveRun olActiveRun = newOlActiveRun(OL, OL_CLIENT, NAMESPACE_NAME);
    olActiveRun.startRun();
    olActiveRun.endRun();

    // (2) Get job, then current version associated with job.
    final Job job = MARQUEZ_CLIENT.getJob(NAMESPACE_NAME, olActiveRun.getJobName());
    final UUID currentVersionForJob = job.getCurrentVersion().orElseThrow();
    final JobVersion currentJobVersion =
        MARQUEZ_CLIENT.getJobVersion(
            NAMESPACE_NAME, olActiveRun.getJobName(), currentVersionForJob.toString());

    // Ensure current version in version list for job.
    final List<JobVersion> jobVersions =
        MARQUEZ_CLIENT.listJobVersions(NAMESPACE_NAME, olActiveRun.getJobName());
    assertThat(jobVersions).contains(currentJobVersion);
  }

  @Test
  public void testApp_listRuns() {
    // (1) Use a randomly generated job name.
    final String jobName = newJobName().getValue();

    // (2) Start, then end OL runs for job.
    final ImmutableList<ActiveRun> olActiveRuns =
        newOlActiveRunsFor(OL, OL_CLIENT, NAMESPACE_NAME, jobName);
    for (final ActiveRun olActiveRun : olActiveRuns) {
      olActiveRun.startRun();
      olActiveRun.endRun();
    }

    // Ensure all runs associated with job have been created.
    final List<Run> runs = MARQUEZ_CLIENT.listRuns(NAMESPACE_NAME, jobName);
    for (final ActiveRun olActiveRun : olActiveRuns) {
      failIfNotIn(runs, olActiveRun.getRunId());
    }
  }

  @Test
  public void testApp_getJob_throwsOnUnknownNamespace() {
    // (1) Use a randomly generated namespace.
    final String unknownNamespace = newNamespaceName().getValue();

    // Ensure namespace not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.getJob(unknownNamespace, newJobName().getValue()))
        .withMessage(ERROR_NAMESPACE_NOT_FOUND, unknownNamespace);
  }

  @Test
  public void testApp_getJob_throwsOnUnknownJob() {
    // (1) Use a randomly generated job name.
    final String unknownJobName = newJobName().getValue();

    // Ensure job name not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.getJob(NAMESPACE_NAME, unknownJobName))
        .withMessage(ERROR_JOB_NOT_FOUND, unknownJobName);
  }

  @Test
  public void testApp_listJobs_throwsOnUnknownNamespace() {
    // (1) Use a randomly generated namespace.
    final String unknownNamespace = newNamespaceName().getValue();

    // Ensure namespace not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.listJobs(unknownNamespace))
        .withMessage(ERROR_NAMESPACE_NOT_FOUND, unknownNamespace);
  }

  @Test
  public void testApp_getJobVersion_throwsOnUnknownNamespace() {
    // (1) Use a randomly generated namespace.
    final String unknownNamespace = newNamespaceName().getValue();

    // Ensure namespace not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(
            () ->
                MARQUEZ_CLIENT.getJobVersion(
                    unknownNamespace, newJobName().getValue(), newVersion().getValue().toString()))
        .withMessage(ERROR_NAMESPACE_NOT_FOUND, unknownNamespace);
  }

  @Test
  public void testApp_getJobVersion_throwsOnUnknownJob() {
    // (1) Use a randomly generated job name and version.
    final String unknownJobName = newJobName().getValue();
    final String unknownVersion = newVersion().getValue().toString(); // Not used.

    // (2) Start, then end OL runs.
    final ActiveRun olActiveRun = newOlActiveRun(OL, OL_CLIENT, NAMESPACE_NAME);
    olActiveRun.startRun();
    olActiveRun.endRun();

    // Ensure job name not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(
            () -> MARQUEZ_CLIENT.getJobVersion(NAMESPACE_NAME, unknownJobName, unknownVersion))
        .withMessage(ERROR_JOB_NOT_FOUND, unknownJobName);
  }

  @Test
  public void testApp_getJobVersion_throwsOnUnknownJobVersion() {
    // (1) Use a randomly generated job version.
    final String unknownVersion = newVersion().getValue().toString();

    // (2) Start, then end OL runs.
    final ActiveRun olActiveRun = newOlActiveRun(OL, OL_CLIENT, NAMESPACE_NAME);
    olActiveRun.startRun();
    olActiveRun.endRun();

    // Ensure job version not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(
            () ->
                MARQUEZ_CLIENT.getJobVersion(
                    NAMESPACE_NAME, olActiveRun.getJobName(), unknownVersion))
        .withMessage(ERROR_JOB_VERSION_NOT_FOUND, unknownVersion);
  }

  @Test
  public void testApp_listJobVersions_throwsOnUnknownNamespace() {
    // (1) Use a randomly generated namespace.
    final String unknownNamespace = newNamespaceName().getValue();
    final String unknownJobName = newNamespaceName().getValue(); // Not used.

    // Ensure namespace not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.listJobVersions(unknownNamespace, unknownJobName))
        .withMessage(ERROR_NAMESPACE_NOT_FOUND, unknownNamespace);
  }

  @Test
  public void testApp_listJobVersions_throwsOnUnknownJobName() {
    // (1) Use a randomly generated job name.
    final String unknownJobName = newNamespaceName().getValue();

    // Ensure job name not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.listJobVersions(NAMESPACE_NAME, unknownJobName))
        .withMessage(ERROR_JOB_NOT_FOUND, unknownJobName);
  }

  @Test
  public void testApp_listRuns_throwsOnUnknownNamespace() {
    // (1) Use a randomly generated namespace.
    final String unknownNamespace = newNamespaceName().getValue();
    final String unknownJobName = newNamespaceName().getValue(); // Not used.

    // Ensure namespace not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.listRuns(unknownNamespace, unknownJobName))
        .withMessage(ERROR_NAMESPACE_NOT_FOUND, unknownNamespace);
  }

  @Test
  public void testApp_listRuns_throwsOnUnknownJobName() {
    // (1) Use a randomly generated job name.
    final String unknownJobName = newJobName().getValue();

    // Ensure job name not found.
    assertThatExceptionOfType(MarquezClientException.class)
        .isThrownBy(() -> MARQUEZ_CLIENT.listRuns(NAMESPACE_NAME, unknownJobName))
        .withMessage(ERROR_JOB_NOT_FOUND, unknownJobName);
  }

  /** Fails if {@code dataset} not found in {@code datasetIds}. */
  private void failIfNotIn(final Set<DatasetId> datasetIds, final OpenLineage.Dataset dataset) {
    if (datasetIds.stream()
        .noneMatch(
            datasetId ->
                datasetId.getNamespace().equals(dataset.getNamespace())
                    && datasetId.getName().equals(dataset.getName()))) {
      fail(ERROR_FAIL_IF_NOT_IN, dataset.getName(), datasetIds);
    }
  }

  /** Fails if {@code jobName} not found in {@code jobs}. */
  private void failIfNotIn(final List<Job> jobs, final String jobName) {
    if (jobs.stream().noneMatch(job -> job.getName().equals(jobName))) {
      fail(ERROR_FAIL_IF_NOT_IN, jobName, jobs);
    }
  }

  /** Fails if {@code runId} not found in {@code runs}. */
  private void failIfNotIn(final List<Run> runs, final UUID runId) {
    if (runs.stream().noneMatch(run -> run.getId().equals(runId.toString()))) {
      fail(ERROR_FAIL_IF_NOT_IN, runId, runs);
    }
  }
}
