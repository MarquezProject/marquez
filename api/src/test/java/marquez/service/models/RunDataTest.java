/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.InputDatasetVersion;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.OutputDatasetVersion;
import marquez.common.models.RunState;
import org.junit.jupiter.api.Test;

class RunDataTest {

  private static final UUID RUN_UUID = UUID.randomUUID();
  private static final UUID JOB_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final Instant STARTED_AT = Instant.now();
  private static final Instant ENDED_AT = Instant.now();
  private static final UUID INPUT_UUID = UUID.randomUUID();
  private static final UUID OUTPUT_UUID = UUID.randomUUID();
  private static final UUID CHILD_RUN_ID = UUID.randomUUID();
  private static final UUID PARENT_RUN_ID = UUID.randomUUID();

  @Test
  void testCreateRunDataWithAllFields() {
    JobVersionId jobVersionId =
        JobVersionId.builder()
            .namespace(NamespaceName.of("test-namespace"))
            .name(JobName.of("test-job"))
            .version(UUID.randomUUID())
            .build();

    ImmutableMap<String, Object> facets = ImmutableMap.of("testFacet", "testValue");

    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            STARTED_AT,
            ENDED_AT,
            RunState.COMPLETED,
            JOB_UUID,
            jobVersionId,
            ImmutableList.of(INPUT_UUID),
            ImmutableList.of(OUTPUT_UUID),
            2,
            null,
            null,
            null,
            null,
            ImmutableList.of(CHILD_RUN_ID),
            ImmutableList.of(PARENT_RUN_ID),
            facets);

    assertThat(runData.getUuid()).isEqualTo(RUN_UUID);
    assertThat(runData.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(runData.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(runData.getStartedAt()).isPresent().contains(STARTED_AT);
    assertThat(runData.getEndedAt()).isPresent().contains(ENDED_AT);
    assertThat(runData.getState()).isEqualTo(RunState.COMPLETED);
    assertThat(runData.getJobUuid()).isEqualTo(JOB_UUID);
    assertThat(runData.getJobVersionId()).isEqualTo(jobVersionId);
    assertThat(runData.getInputUuids()).containsExactly(INPUT_UUID);
    assertThat(runData.getOutputUuids()).containsExactly(OUTPUT_UUID);
    assertThat(runData.getDepth()).isEqualTo(2);
    assertThat(runData.getChildRunIds()).containsExactly(CHILD_RUN_ID);
    assertThat(runData.getParentRunIds()).containsExactly(PARENT_RUN_ID);
    assertThat(runData.getFacets()).isEqualTo(facets);
  }

  @Test
  void testGetStartedAtWhenNull() {
    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            null, // startedAt is null
            ENDED_AT,
            RunState.RUNNING,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(runData.getStartedAt()).isEmpty();
  }

  @Test
  void testGetStartedAtWhenPresent() {
    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            STARTED_AT,
            null,
            RunState.RUNNING,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Optional<Instant> startedAt = runData.getStartedAt();
    assertThat(startedAt).isPresent();
    assertThat(startedAt.get()).isEqualTo(STARTED_AT);
  }

  @Test
  void testGetEndedAtWhenNull() {
    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            STARTED_AT,
            null, // endedAt is null
            RunState.RUNNING,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(runData.getEndedAt()).isEmpty();
  }

  @Test
  void testGetEndedAtWhenPresent() {
    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            STARTED_AT,
            ENDED_AT,
            RunState.COMPLETED,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    Optional<Instant> endedAt = runData.getEndedAt();
    assertThat(endedAt).isPresent();
    assertThat(endedAt.get()).isEqualTo(ENDED_AT);
  }

  @Test
  void testGetInputUuidsReturnsImmutableSet() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    List<UUID> inputList = ImmutableList.of(uuid1, uuid2);

    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            null,
            null,
            RunState.NEW,
            JOB_UUID,
            null,
            inputList,
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(runData.getInputUuids()).containsExactlyInAnyOrder(uuid1, uuid2);
    assertThat(runData.getInputUuids()).isInstanceOf(ImmutableSet.class);
  }

  @Test
  void testGetOutputUuidsReturnsImmutableSet() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    List<UUID> outputList = ImmutableList.of(uuid1, uuid2);

    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            null,
            null,
            RunState.NEW,
            JOB_UUID,
            null,
            ImmutableList.of(),
            outputList,
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    assertThat(runData.getOutputUuids()).containsExactlyInAnyOrder(uuid1, uuid2);
    assertThat(runData.getOutputUuids()).isInstanceOf(ImmutableSet.class);
  }

  @Test
  void testWithMethodsCreateNewInstances() {
    RunData original =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            null,
            null,
            RunState.NEW,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    DatasetId datasetId = new DatasetId(NamespaceName.of("ns"), DatasetName.of("dataset"));
    ImmutableSet<DatasetId> inputs = ImmutableSet.of(datasetId);
    RunData modified = original.withInputs(inputs);

    assertThat(modified.getInputs()).isEqualTo(inputs);
    assertThat(original.getInputs()).isNull();
    assertThat(modified).isNotSameAs(original);
  }

  @Test
  void testRunDataWithAllRunStates() {
    for (RunState state : RunState.values()) {
      RunData runData =
          new RunData(
              RUN_UUID,
              CREATED_AT,
              UPDATED_AT,
              null,
              null,
              state,
              JOB_UUID,
              null,
              ImmutableList.of(),
              ImmutableList.of(),
              0,
              null,
              null,
              null,
              null,
              null,
              null,
              null);

      assertThat(runData.getState()).isEqualTo(state);
    }
  }

  @Test
  void testRunDataWithDatasetVersions() {
    DatasetVersionId inputDsVersionId =
        DatasetVersionId.builder()
            .name(DatasetName.of("input-dataset"))
            .namespace(NamespaceName.of("input-namespace"))
            .version(UUID.randomUUID())
            .build();
    InputDatasetVersion inputVersion = new InputDatasetVersion(inputDsVersionId, ImmutableMap.of());

    DatasetVersionId outputDsVersionId =
        DatasetVersionId.builder()
            .name(DatasetName.of("output-dataset"))
            .namespace(NamespaceName.of("output-namespace"))
            .version(UUID.randomUUID())
            .build();
    OutputDatasetVersion outputVersion =
        new OutputDatasetVersion(outputDsVersionId, ImmutableMap.of());

    RunData runData =
        new RunData(
            RUN_UUID,
            CREATED_AT,
            UPDATED_AT,
            null,
            null,
            RunState.COMPLETED,
            JOB_UUID,
            null,
            ImmutableList.of(),
            ImmutableList.of(),
            0,
            null,
            null,
            ImmutableList.of(inputVersion),
            ImmutableList.of(outputVersion),
            null,
            null,
            null);

    assertThat(runData.getInputDatasetVersions()).containsExactly(inputVersion);
    assertThat(runData.getOutputDatasetVersions()).containsExactly(outputVersion);
  }
}
