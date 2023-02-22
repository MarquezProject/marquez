/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.service.models.NodeId.VERSION_DELIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import marquez.common.models.DatasetFieldId;
import marquez.common.models.DatasetFieldVersionId;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
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

  @ParameterizedTest(name = "testDatasetField-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset$colA",
        "gs://bucket$/path/to/data$colA",
        "gs://bucket$/path/to/data$col_A"
      },
      delimiter = '$')
  public void testDatasetField(String namespace, String dataset, String field) {
    NamespaceName namespaceName = NamespaceName.of(namespace);
    FieldName fieldName = FieldName.of(field);
    DatasetName datasetName = DatasetName.of(dataset);
    DatasetId dsId = new DatasetId(namespaceName, datasetName);
    DatasetFieldId dsfId = new DatasetFieldId(dsId, fieldName);
    NodeId nodeId = NodeId.of(dsfId);
    assertFalse(nodeId.isRunType());
    assertFalse(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertFalse(nodeId.hasVersion());
    assertTrue(nodeId.isDatasetFieldType());

    assertEquals(dsfId, nodeId.asDatasetFieldId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(namespace, nodeId.asDatasetFieldId().getDatasetId().getNamespace().getValue());
    assertEquals(dataset, nodeId.asDatasetFieldId().getDatasetId().getName().getValue());
    assertEquals(field, nodeId.asDatasetFieldId().getFieldName().getValue());
  }

  @ParameterizedTest(name = "testDatasetField-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset$colA$my-namespace$my-dataset$colB",
        "gs://bucket$/path/to/data$colA$gs://bucket$/path/to/data$colB",
        "gs://bucket$/path/to/data$col_A$gs://bucket$/path/to/data$col_B"
      },
      delimiter = '$')
  public void testSameTypeAs(
      String namespaceFirst,
      String datasetFirst,
      String fieldFirst,
      String namespaceSecond,
      String datasetSecond,
      String fieldSecond) {
    NamespaceName namespaceFirstName = NamespaceName.of(namespaceFirst);
    FieldName fielFirstdName = FieldName.of(fieldFirst);
    DatasetName datasetFirstName = DatasetName.of(datasetFirst);
    DatasetId dsFirstId = new DatasetId(namespaceFirstName, datasetFirstName);
    DatasetFieldId dsFirstfId = new DatasetFieldId(dsFirstId, fielFirstdName);
    NodeId nodeFirstId = NodeId.of(dsFirstfId);

    NamespaceName namespaceSecondName = NamespaceName.of(namespaceSecond);
    FieldName fielSeconddName = FieldName.of(fieldSecond);
    DatasetName datasetSecondName = DatasetName.of(datasetSecond);
    DatasetId dsSecondId = new DatasetId(namespaceSecondName, datasetSecondName);
    DatasetFieldId dsSecondfId = new DatasetFieldId(dsSecondId, fielSeconddName);
    NodeId nodeSecondId = NodeId.of(dsSecondfId);
    assertTrue(nodeFirstId.sameTypeAs(nodeSecondId));
  }

  @ParameterizedTest(name = "testDatasetField-{index} {argumentsWithNames}")
  @CsvSource(
      value = {
        "my-namespace$my-dataset$colA#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "gs://bucket$/path/to/data$colA#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
        "gs://bucket$/path/to/data$col_A#aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
      },
      delimiter = '$')
  public void testDatasetFieldVersion(String namespace, String dataset, String fieldWithVersion) {
    String version = fieldWithVersion.split(VERSION_DELIM)[1];
    String field = fieldWithVersion.split(VERSION_DELIM)[0];

    NamespaceName namespaceName = NamespaceName.of(namespace);
    FieldName fieldName = FieldName.of(field.split(VERSION_DELIM)[0]);
    DatasetName datasetName = DatasetName.of(dataset);
    DatasetId dsId = new DatasetId(namespaceName, datasetName);
    DatasetFieldVersionId dsfId =
        new DatasetFieldVersionId(dsId, fieldName, UUID.fromString(version));
    NodeId nodeId = NodeId.of(dsfId);
    assertFalse(nodeId.isRunType());
    assertFalse(nodeId.isJobType());
    assertFalse(nodeId.isDatasetType());
    assertTrue(nodeId.hasVersion());
    assertTrue(nodeId.isDatasetFieldVersionType());

    assertEquals(dsfId, nodeId.asDatasetFieldVersionId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    assertEquals(
        namespace, nodeId.asDatasetFieldVersionId().getDatasetId().getNamespace().getValue());
    assertEquals(dataset, nodeId.asDatasetFieldVersionId().getDatasetId().getName().getValue());
    assertEquals(field, nodeId.asDatasetFieldVersionId().getFieldName().getValue());
    assertEquals(version, nodeId.asDatasetFieldVersionId().getVersion().toString());
  }
}
