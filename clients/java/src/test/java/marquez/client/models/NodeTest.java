/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import marquez.client.Utils;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class NodeTest {
  private static final ObjectMapper MAPPER = Utils.newObjectMapper();

  @Test
  void testResolvesCorrectNodeDataTypeFromJsonLegacyDatasetField() throws Exception {
    final String input =
        """
            {
                "id": "datasetField:snowflake://matillion.eu-central-1:FROSTY_BORG.ETLD.Do11yJiraIssues:Assignee",
                "type": "DATASET_FIELD",
                "data": {
                    "type": "DATASET_FIELD",
                    "namespace": "snowflake://matillion.eu-central-1",
                    "dataset": "FROSTY_BORG.ETLD.Do11yJiraIssues",
                    "datasetVersion": "9caaa5b3-d101-4368-9bd1-b99669736a78",
                    "field": "Assignee",
                    "fieldType": "UNKNOWN",
                    "inputFields": []
                },
                "inEdges": [],
                "outEdges": []
            }
            """;

    final Node node = MAPPER.readValue(input, Node.class);

    assertTrue(node.getData() instanceof ColumnLineageNodeData);
  }

  @Test
  void testResolvesCorrectNodeDataTypeFromJsonDatasetField() throws Exception {
    final String input =
        """
        {
            "id": "datasetField:snowflake://matillion.eu-central-1:FROSTY_BORG.ETLD.Do11yJiraIssues:Assignee",
            "type": "DATASET_FIELD",
            "data": {
                "namespace": "snowflake://matillion.eu-central-1",
                "dataset": "FROSTY_BORG.ETLD.Do11yJiraIssues",
                "datasetVersion": "9caaa5b3-d101-4368-9bd1-b99669736a78",
                "field": "Assignee",
                "fieldType": "UNKNOWN",
                "inputFields": []
            },
            "inEdges": [],
            "outEdges": []
        }
        """;

    final Node node = MAPPER.readValue(input, Node.class);

    assertTrue(node.getData() instanceof ColumnLineageNodeData);
  }

  @Test
  void testResolvesCorrectNodeDataTypeFromJsonDataset() throws Exception {
    final String input =
        """
            {
                "id": "dataset:namespace:table",
                "type": "DATASET",
                "data": {
                    "id": {
                        "namespace": "namespace",
                        "name": "table"
                    },
                    "type": "DB_TABLE",
                    "name": "table",
                    "physicalName": "table",
                    "createdAt": "2023-08-09T11:17:30.091688Z",
                    "updatedAt": "2023-08-09T11:17:30.091688Z",
                    "namespace": "namespace",
                    "sourceName": "default",
                    "fields": [],
                    "tags": []
                },
                "inEdges": [],
                "outEdges": []
            }
            """;

    final Node node = MAPPER.readValue(input, Node.class);

    assertTrue(node.getData() instanceof DatasetNodeData);
    assertEquals(DatasetType.DB_TABLE, ((DatasetNodeData) node.getData()).getType());
  }

  @Test
  void testResolvesCorrectNodeDataTypeFromJsonJob() throws Exception {
    final String input =
        """
            {
               "id": "job:namespace:job",
               "type": "JOB",
               "data": {
                   "id": {
                       "namespace": "namespace",
                       "name": "job"
                   },
                   "type": "BATCH",
                   "name": "job",
                   "simpleName": "job",
                   "createdAt": "2023-08-09T11:17:30.091688Z",
                   "updatedAt": "2023-08-09T11:17:30.091688Z",
                   "namespace": "namespace",
                   "inputs": [],
                   "outputs": []
               },
               "inEdges": [],
               "outEdges": []
           }
            """;

    final Node node = MAPPER.readValue(input, Node.class);

    assertTrue(node.getData() instanceof JobNodeData);
    assertEquals(JobType.BATCH, ((JobNodeData) node.getData()).getType());
  }
}
