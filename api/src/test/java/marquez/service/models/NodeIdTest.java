package marquez.service.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NodeIdTest {

  @ParameterizedTest(name = "testDatasetWithUrl-{index} {argumentsWithNames}")
  @CsvSource({"gs://bucket,/path/to/data", "postgresql://hostname:5432/database,my_table"})
  public void testDatasetWithUrl() {
    String namespace = "gs://bucket";
    String datasetName = "/path/to/data";
    DatasetName dsName = DatasetName.of(datasetName);
    DatasetId dsId = new DatasetId(NamespaceName.of(namespace), dsName);
    NodeId nodeId = NodeId.of(dsId);
    assertFalse(nodeId.isRunType());
    assertFalse(nodeId.isJobType());
    assertTrue(nodeId.isDatasetType());
    assertFalse(nodeId.hasVersion());
    assertEquals(dsId, nodeId.asDatasetId());
    assertEquals(nodeId, NodeId.of(nodeId.getValue()));
    DatasetId ds = nodeId.asDatasetId();
    assertEquals(namespace, ds.getNamespace().getValue());
    assertEquals(datasetName, ds.getName().getValue());
  }
}
