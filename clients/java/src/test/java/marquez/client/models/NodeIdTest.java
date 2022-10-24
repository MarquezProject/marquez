/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NodeIdTest {

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
    NodeId nodeId = NodeId.of(new DatasetId(namespace, dataset));
    assertTrue(nodeId.isDatasetType());
    assertFalse(nodeId.isDatasetFieldType());
    assertEquals(namespace, nodeId.asDatasetId().getNamespace());
    assertEquals(dataset, nodeId.asDatasetId().getName());
  }

  @ParameterizedTest(name = "testDatasetField-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset$colA",
        "gs://bucket$/path/to/data$colA",
        "gs://bucket$/path/to/data$col_A"
      },
      delimiter = '$')
  public void testDatasetField(String namespace, String dataset, String field) {
    NodeId nodeId = NodeId.of(new DatasetFieldId(namespace, dataset, field));
    assertFalse(nodeId.isDatasetType());
    assertTrue(nodeId.isDatasetFieldType());
    assertEquals(namespace, nodeId.asDatasetFieldId().getNamespace());
    assertEquals(dataset, nodeId.asDatasetFieldId().getDataset());
    assertEquals(field, nodeId.asDatasetFieldId().getField());
  }

  @ParameterizedTest(name = "testJob-{index} {argumentsWithNames}")
  @CsvSource(
      value = {"my-namespace$my-job", "org://team$my-job"},
      delimiter = '$')
  public void testJob(String namespace, String job) {
    JobId jobId = new JobId(namespace, job);
    NodeId nodeId = NodeId.of(jobId);
    assertTrue(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertEquals(jobId, nodeId.asJobId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asJobId().getNamespace());
    assertEquals(job, nodeId.asJobId().getName());
  }
}
