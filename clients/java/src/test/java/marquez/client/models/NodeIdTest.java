/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.NodeId.VERSION_DELIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
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

  @ParameterizedTest(name = "testJobWithVersion-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-job#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "org://team$my-job#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      },
      delimiter = '$')
  public void testJobWithVersion(String namespace, String job) {
    JobId jobId = new JobId(namespace, job);
    NodeId nodeId = NodeId.of(jobId);
    assertTrue(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertTrue(nodeId.hasVersion());
    assertEquals(jobId, nodeId.asJobId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asJobId().getNamespace());
    assertEquals(job, nodeId.asJobId().getName());
    assertEquals(job.split(VERSION_DELIM)[1], nodeId.asJobVersionId().getVersion().toString());
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
    DatasetId dsId = new DatasetId(namespace, dataset);
    NodeId nodeId = NodeId.of(dsId);
    assertFalse(nodeId.isJobType());
    assertTrue(nodeId.isDatasetType());
    assertTrue(nodeId.isDatasetVersionType());
    assertTrue(nodeId.hasVersion());
    assertEquals(dsId, nodeId.asDatasetId());
    assertEquals(namespace, nodeId.asDatasetId().getNamespace());
    assertEquals(dataset, nodeId.asDatasetId().getName());
    assertEquals(
        dataset.split(VERSION_DELIM)[1], nodeId.asDatasetVersionId().getVersion().toString());
  }

  @ParameterizedTest(name = "testDatasetFieldWithVersion-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset$colA#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "gs://bucket$/path/to/data$colA#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "gs://bucket$/path/to/data$col_A#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      },
      delimiter = '$')
  public void testDatasetFieldWithVersion(
      String namespace, String dataset, String fieldWithVersion) {
    String version = fieldWithVersion.split(VERSION_DELIM)[1];
    String field = fieldWithVersion.split(VERSION_DELIM)[0];

    DatasetFieldVersionId dsfId =
        new DatasetFieldVersionId(namespace, dataset, field, UUID.fromString(version));
    NodeId nodeId = NodeId.of(dsfId);
    assertFalse(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertTrue(nodeId.hasVersion());
    assertTrue(nodeId.isDatasetFieldVersionType());

    assertEquals(dsfId, nodeId.asDatasetFieldVersionId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asDatasetFieldVersionId().getNamespace());
    assertEquals(dataset, nodeId.asDatasetFieldVersionId().getName());
    assertEquals(field, nodeId.asDatasetFieldVersionId().getField());
    assertEquals(version, nodeId.asDatasetFieldVersionId().getVersion().toString());
  }
}
