/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.service.models.NodeId.VERSION_DELIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NodeIdTest {

  @ParameterizedTest(name = "testJob-{index} {argumentsWithNames}")
  @CsvSource(
      value = {"my-namespace$my-job", "org://team$my-job"},
      delimiter = '$')
  public void testJob(String namespace, String job) {
    NamespaceName nsName = NamespaceName.of(namespace);
    JobName jobName = JobName.of(job);
    JobId jobId = new JobId(nsName, jobName);
    NodeId nodeId = NodeId.of(jobId);
    assertFalse(nodeId.isRunType());
    assertTrue(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertFalse(nodeId.hasVersion());
    assertEquals(jobId, nodeId.asJobId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asJobId().getNamespace().getValue());
    assertEquals(job, nodeId.asJobId().getName().getValue());
  }

  @ParameterizedTest(name = "testJobWithVersion-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-job#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "org://team$my-job#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      },
      delimiter = '$')
  public void testJobWithVersion(String namespace, String job) {
    NamespaceName nsName = NamespaceName.of(namespace);
    JobName jobName = JobName.of(job);
    JobId jobId = new JobId(nsName, jobName);
    NodeId nodeId = NodeId.of(jobId);
    assertFalse(nodeId.isRunType());
    assertTrue(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertTrue(nodeId.hasVersion());
    assertEquals(jobId, nodeId.asJobId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asJobId().getNamespace().getValue());
    assertEquals(job, nodeId.asJobId().getName().getValue());
    assertEquals(job.split(VERSION_DELIM)[1], nodeId.asJobVersionId().getVersion().toString());
  }

  @ParameterizedTest(name = "testDataset-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset",
        "gs://bucket$/path/to/data",
        "postgresql://hostname:5432/database$my_table",
        "my-namespace$my_struct<a:bigint,b:bigint,c:string>"
      },
      delimiter = '$')
  public void testDataset(String namespace, String dataset) {
    NamespaceName namespaceName = NamespaceName.of(namespace);
    DatasetName datasetName = DatasetName.of(dataset);
    DatasetId dsId = new DatasetId(namespaceName, datasetName);
    NodeId nodeId = NodeId.of(dsId);
    assertFalse(nodeId.isRunType());
    assertFalse(nodeId.isJobType());
    assertTrue(nodeId.isDatasetType());
    assertFalse(nodeId.hasVersion());
    assertEquals(dsId, nodeId.asDatasetId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asDatasetId().getNamespace().getValue());
    assertEquals(dataset, nodeId.asDatasetId().getName().getValue());
  }

  @ParameterizedTest(name = "testDatasetWithVersion-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "gs://bucket$/path/to/data#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "postgresql://hostname:5432/database$my_table#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "my-namespace$my_struct<a:bigint,b:bigint,c:string>#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      },
      delimiter = '$')
  public void testDatasetWithVersion(String namespace, String dataset) {
    NamespaceName namespaceName = NamespaceName.of(namespace);
    DatasetName datasetName = DatasetName.of(dataset);
    DatasetId dsId = new DatasetId(namespaceName, datasetName);
    NodeId nodeId = NodeId.of(dsId);
    assertFalse(nodeId.isRunType());
    assertFalse(nodeId.isJobType());
    assertTrue(nodeId.isDatasetType());
    assertTrue(nodeId.isDatasetVersionType());
    assertTrue(nodeId.hasVersion());
    assertEquals(dsId, nodeId.asDatasetId());
    assertEquals(namespace, nodeId.asDatasetId().getNamespace().getValue());
    assertEquals(dataset, nodeId.asDatasetId().getName().getValue());
    assertEquals(
        dataset.split(VERSION_DELIM)[1], nodeId.asDatasetVersionId().getVersion().toString());
  }
}
