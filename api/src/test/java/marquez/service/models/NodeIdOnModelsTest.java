/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.Version;
import org.junit.jupiter.api.Test;

/** Tests that the {@code getNodeId()} method returns the correct {@link NodeId} for API models. */
class NodeIdOnModelsTest {

  private static final NamespaceName NAMESPACE = NamespaceName.of("test-namespace");
  private static final Instant NOW = Instant.now();

  @Test
  void datasetNodeIdHasCorrectFormat() {
    DatasetName datasetName = DatasetName.of("test-dataset");
    DatasetId datasetId = new DatasetId(NAMESPACE, datasetName);

    DbTable dataset =
        new DbTable(
            datasetId,
            datasetName,
            DatasetName.of("public.test_dataset"),
            NOW,
            NOW,
            SourceName.of("test-source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            null,
            null,
            null,
            null,
            ImmutableMap.of(),
            false);

    NodeId nodeId = dataset.getNodeId();
    assertThat(nodeId).isNotNull();
    assertThat(nodeId.isDatasetType()).isTrue();
    assertThat(nodeId.getValue()).isEqualTo("dataset:test-namespace:test-dataset");
  }

  @Test
  void datasetVersionNodeIdIncludesVersion() {
    DatasetName datasetName = DatasetName.of("test-dataset");
    DatasetId datasetId = new DatasetId(NAMESPACE, datasetName);
    UUID versionUuid = UUID.randomUUID();

    DbTableVersion datasetVersion =
        new DbTableVersion(
            datasetId,
            datasetName,
            DatasetName.of("public.test_dataset"),
            NOW,
            new Version(versionUuid),
            SourceName.of("test-source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            null,
            null,
            null,
            ImmutableMap.of());

    NodeId nodeId = datasetVersion.getNodeId();
    assertThat(nodeId).isNotNull();
    assertThat(nodeId.hasVersion()).isTrue();
    assertThat(nodeId.getValue())
        .isEqualTo("dataset:test-namespace:test-dataset#" + versionUuid);
  }

  @Test
  void jobNodeIdHasCorrectFormat() {
    JobName jobName = JobName.of("test-job");
    JobId jobId = new JobId(NAMESPACE, jobName);

    Job job =
        new Job(
            jobId,
            JobType.BATCH,
            jobName,
            "test-job",
            null,
            null,
            NOW,
            NOW,
            ImmutableSet.of(),
            ImmutableSet.of(),
            null,
            null,
            null,
            null,
            ImmutableMap.of(),
            null,
            null,
            null);

    NodeId nodeId = job.getNodeId();
    assertThat(nodeId).isNotNull();
    assertThat(nodeId.isJobType()).isTrue();
    assertThat(nodeId.getValue()).isEqualTo("job:test-namespace:test-job");
  }
}
