/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.UUID;
import marquez.BaseIntegrationTest;
import marquez.client.MarquezHttpException;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobVersion;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import marquez.common.models.CommonModelGenerator;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class JobResourceIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM dataset_versions_field_mapping");
          handle.execute("DELETE FROM stream_versions");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
          handle.execute("DELETE FROM run_states");
          handle.execute("DELETE FROM runs");
          handle.execute("DELETE FROM run_args");
          handle.execute("DELETE FROM job_versions_io_mapping");
          handle.execute("DELETE FROM job_versions");
          handle.execute("DELETE FROM jobs");
          return null;
        });
  }

  @Test
  public void testApp_listJobs() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    List<Job> jobs = client.listJobs(NAMESPACE_NAME);
    assertThat(jobs).hasSizeGreaterThan(0);
  }

  @Test
  public void testApp_listRuns() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());
    List<Run> runs = client.listRuns(NAMESPACE_NAME, JOB_NAME);
    assertThat(runs).hasSizeGreaterThan(0);
  }

  @Test
  public void testApp_createDuplicateRun() {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
          String runId = UUID.randomUUID().toString();
          client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
          client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
        });
  }

  @Test
  public void testApp_notExistsJobForRun() {
    Assertions.assertThrows(
        Exception.class,
        () -> {
          client.createRun(NAMESPACE_NAME, "NotExists", RunMeta.builder().build());
        });
  }

  @Test
  public void testApp_createNonMatchingJobWithRun() {
    String runId = UUID.randomUUID().toString();
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    client.createJob(NAMESPACE_NAME, "DIFFERENT_JOB", JOB_META);

    client.createRun(NAMESPACE_NAME, "DIFFERENT_JOB", RunMeta.builder().id(runId).build());

    final JobMeta JOB_META_WITH_RUN =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(runId)
            .build();
    // associate wrong run
    Assertions.assertThrows(
        Exception.class, () -> client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META_WITH_RUN));
  }

  @Test
  public void testApp_createJobWithMissingRun() {
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    Assertions.assertThrows(
        Exception.class, () -> client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META));
  }

  @Test
  public void testApp_createNotExistingDataset() {
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(NAMESPACE_NAME, "does-not-exist")
            .outputs(NAMESPACE_NAME, "does-not-exist")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    Assertions.assertThrows(
        Exception.class, () -> client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META));
  }

  @Test
  public void testApp_notExistsJob() {
    Assertions.assertThrows(Exception.class, () -> client.getJob(NAMESPACE_NAME, "not-existing"));
  }

  @Test
  public void testApp_listJobVersions() {
    String runId = UUID.randomUUID().toString();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    client.markRunAs(runId, RunState.COMPLETED);

    List<JobVersion> jobVersions = client.listJobVersions(NAMESPACE_NAME, JOB_NAME, 5, 0);
    assertThat(jobVersions)
        .hasSize(1)
        .first()
        .extracting(JobVersion::getLatestRun)
        .extracting(Run::getId)
        .isEqualTo(runId);

    String newRunId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(newRunId).build());
    final JobMeta newVersionMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(CommonModelGenerator.newLocation())
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(newRunId)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, newVersionMeta);
    client.markRunAs(newRunId, RunState.COMPLETED);

    jobVersions = client.listJobVersions(NAMESPACE_NAME, JOB_NAME, 5, 0);
    assertThat(jobVersions)
        .hasSize(2)
        .first()
        .extracting(JobVersion::getLatestRun)
        .extracting(Run::getId)
        .isEqualTo(newRunId);
  }

  @Test
  public void testApp_listJobVersionsWithInvalidJobName() {
    try {
      client.listJobVersions(NAMESPACE_NAME, "a fake job name", 5, 0);
      fail("No exception thrown hitting invalid job version URL");
    } catch (MarquezHttpException e) {
      assertThat(e.getCode()).isEqualTo(404);
    }
  }

  @Test
  public void testApp_getJobVersion() {
    String runId = UUID.randomUUID().toString();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    client.markRunAs(runId, RunState.COMPLETED);

    List<JobVersion> jobVersions = client.listJobVersions(NAMESPACE_NAME, JOB_NAME, 5, 0);
    assertThat(jobVersions)
        .first()
        .extracting(JobVersion::getLatestRun)
        .extracting(Run::getId)
        .isEqualTo(runId);
    UUID version = jobVersions.get(0).getVersion();
    JobVersion jobVersion = client.getJobVersion(NAMESPACE_NAME, JOB_NAME, version.toString());
    assertThat(jobVersion)
        .isNotNull()
        .matches(jv -> jv.getVersion().equals(version))
        .matches(jv -> jv.getInputs().isEmpty())
        .matches(jv -> jv.getOutputs().isEmpty())
        .extracting(JobVersion::getLatestRun)
        .isNotNull()
        .extracting(Run::getId)
        .isEqualTo(runId);
  }

  @Test
  public void testApp_getJobVersionWithInputsAndOutputs() {
    createSource(STREAM_SOURCE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    client.createDataset(NAMESPACE_NAME, STREAM_NAME, STREAM_META);
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);

    String jobName = "newJob";
    final JobMeta newVersionMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of(STREAM_ID))
            .outputs(ImmutableSet.of(DB_TABLE_ID))
            .location(CommonModelGenerator.newLocation())
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, jobName, newVersionMeta);

    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, jobName, RunMeta.builder().id(runId).build());
    client.createDataset(
        NAMESPACE_NAME,
        DB_TABLE_NAME,
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(runId)
            .build());
    client.markRunAs(runId, RunState.COMPLETED);

    List<JobVersion> jobVersions = client.listJobVersions(NAMESPACE_NAME, jobName, 5, 0);
    assertThat(jobVersions)
        .first()
        .extracting(JobVersion::getLatestRun)
        .extracting(Run::getId)
        .isEqualTo(runId);
    JobVersion jobVersion =
        client.getJobVersion(
            NAMESPACE_NAME, jobName, jobVersions.get(0).getId().getVersion().toString());
    assertThat(jobVersion)
        .isNotNull()
        .matches(jv -> jv.getInputs().size() == 1, "has one input")
        .matches(jv -> jv.getOutputs().size() == 1, "has one output");
  }

  @Test
  public void getSource() {
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(SOURCE_TYPE)
            .connectionUrl(CONNECTION_URL)
            .description(SOURCE_DESCRIPTION)
            .build();
    Source createdSource = client.createSource("sourceName", sourceMeta);
    assertThat(createdSource.getCreatedAt()).isNotNull();
    assertThat(createdSource.getName()).isEqualTo("sourceName");
    assertThat(createdSource.getType()).isEqualTo(sourceMeta.getType());
    assertThat(createdSource.getUpdatedAt()).isNotNull();
    assertThat(createdSource.getDescription()).isEqualTo(sourceMeta.getDescription());
    assertThat(createdSource.getConnectionUrl()).isEqualTo(sourceMeta.getConnectionUrl());

    Source source = client.getSource("sourceName");
    assertThat(source.getCreatedAt()).isNotNull();
    assertThat(source.getName()).isEqualTo("sourceName");
    assertThat(source.getType()).isEqualTo(sourceMeta.getType());
    assertThat(source.getUpdatedAt()).isNotNull();
    assertThat(source.getDescription()).isEqualTo(sourceMeta.getDescription());
    assertThat(source.getConnectionUrl()).isEqualTo(sourceMeta.getConnectionUrl());
  }
}
