/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import com.google.common.collect.ImmutableSortedSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetFieldId;
import marquez.common.models.DatasetFieldVersionId;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobVersionId;
import marquez.db.ColumnLineageDao;
import marquez.db.DatasetFieldDao;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;
import marquez.service.models.ColumnLineage;
import marquez.service.models.ColumnLineageInputField;
import marquez.service.models.Dataset;
import marquez.service.models.Edge;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class ColumnLineageService extends DelegatingDaos.DelegatingColumnLineageDao {
  private final DatasetFieldDao datasetFieldDao;

  public ColumnLineageService(ColumnLineageDao dao, DatasetFieldDao datasetFieldDao) {
    super(dao);
    this.datasetFieldDao = datasetFieldDao;
  }

  public Lineage lineage(NodeId nodeId, int depth, boolean withDownstream) {
    ColumnNodes columnNodes = getColumnNodes(nodeId);
    if (columnNodes.nodeIds.isEmpty()) {
      throw new NodeIdNotFoundException("Could not find node");
    }

    return toLineage(
        getLineage(depth, columnNodes.nodeIds, withDownstream, columnNodes.createdAtUntil),
        nodeId.hasVersion());
  }

  private Lineage toLineage(Set<ColumnLineageNodeData> lineageNodeData, boolean includeVersion) {
    Map<NodeId, Node.Builder> graphNodes = new HashMap<>();
    Map<NodeId, List<NodeId>> inEdges = new HashMap<>();
    Map<NodeId, List<NodeId>> outEdges = new HashMap<>();

    // create nodes
    lineageNodeData.stream()
        .forEach(
            columnLineageNodeData -> {
              NodeId nodeId = toNodeId(columnLineageNodeData, includeVersion);
              graphNodes.put(nodeId, Node.datasetField().data(columnLineageNodeData).id(nodeId));
              columnLineageNodeData.getInputFields().stream()
                  .forEach(
                      inputNode -> {
                        NodeId inputNodeId = toNodeId(inputNode, includeVersion);
                        graphNodes.putIfAbsent(
                            inputNodeId,
                            Node.datasetField()
                                .id(inputNodeId)
                                .data(new ColumnLineageNodeData(inputNode)));
                        Optional.ofNullable(outEdges.get(inputNodeId))
                            .ifPresentOrElse(
                                nodeEdges -> nodeEdges.add(nodeId),
                                () -> outEdges.put(inputNodeId, new LinkedList<>(List.of(nodeId))));
                        Optional.ofNullable(inEdges.get(nodeId))
                            .ifPresentOrElse(
                                nodeEdges -> nodeEdges.add(inputNodeId),
                                () -> inEdges.put(nodeId, new LinkedList<>(List.of(inputNodeId))));
                      });
            });

    // add edges between the nodes
    inEdges.forEach(
        (nodeId, nodes) -> {
          graphNodes
              .get(nodeId)
              .inEdges(
                  nodes.stream()
                      .map(toNodeId -> new Edge(nodeId, toNodeId))
                      .collect(Collectors.toSet()));
        });
    outEdges.forEach(
        (nodeId, nodes) -> {
          graphNodes
              .get(nodeId)
              .outEdges(
                  nodes.stream()
                      .map(toNodeId -> new Edge(nodeId, toNodeId))
                      .collect(Collectors.toSet()));
        });

    // build nodes and return as lineage
    return new Lineage(
        ImmutableSortedSet.copyOf(
            graphNodes.values().stream().map(Node.Builder::build).collect(Collectors.toSet())));
  }

  private static NodeId toNodeId(ColumnLineageNodeData node, boolean includeVersion) {
    if (!includeVersion) {
      return NodeId.of(DatasetFieldId.of(node.getNamespace(), node.getDataset(), node.getField()));
    } else {
      return NodeId.of(
          DatasetFieldVersionId.of(
              node.getNamespace(), node.getDataset(), node.getField(), node.getDatasetVersion()));
    }
  }

  private static NodeId toNodeId(InputFieldNodeData node, boolean includeVersion) {
    if (!includeVersion) {
      return NodeId.of(DatasetFieldId.of(node.getNamespace(), node.getDataset(), node.getField()));
    } else {
      return NodeId.of(
          DatasetFieldVersionId.of(
              node.getNamespace(), node.getDataset(), node.getField(), node.getDatasetVersion()));
    }
  }

  private ColumnNodes getColumnNodes(NodeId nodeId) {
    if (nodeId.isDatasetFieldVersionType()) {
      return getColumnNodes(nodeId.asDatasetFieldVersionId());
    } else if (nodeId.isDatasetVersionType()) {
      return getColumnNodes(nodeId.asDatasetVersionId());
    } else if (nodeId.isJobVersionType()) {
      return getColumnNodes(nodeId.asJobVersionId());
    } else if (nodeId.isDatasetType()) {
      return getColumnNodes(nodeId.asDatasetId());
    } else if (nodeId.isDatasetFieldType()) {
      return getColumnNodes(nodeId.asDatasetFieldId());
    } else if (nodeId.isJobType()) {
      return getColumnNodes(nodeId.asJobId());
    }
    throw new UnsupportedOperationException("Unsupported NodeId: " + nodeId);
  }

  private ColumnNodes getColumnNodes(DatasetVersionId datasetVersionId) {
    List<Pair<UUID, Instant>> fieldsWithInstant =
        datasetFieldDao.findDatasetVersionFieldsUuids(datasetVersionId.getVersion());
    return new ColumnNodes(
        fieldsWithInstant.stream().map(pair -> pair.getValue()).findAny().orElse(Instant.now()),
        fieldsWithInstant.stream().map(pair -> pair.getKey()).collect(Collectors.toList()));
  }

  private ColumnNodes getColumnNodes(DatasetFieldVersionId datasetFieldVersionId) {
    List<Pair<UUID, Instant>> fieldsWithInstant =
        datasetFieldDao.findDatasetVersionFieldsUuids(
            datasetFieldVersionId.getFieldName().getValue(), datasetFieldVersionId.getVersion());
    return new ColumnNodes(
        fieldsWithInstant.stream().map(pair -> pair.getValue()).findAny().orElse(Instant.now()),
        fieldsWithInstant.stream().map(pair -> pair.getKey()).collect(Collectors.toList()));
  }

  private ColumnNodes getColumnNodes(JobVersionId jobVersionId) {
    List<Pair<UUID, Instant>> fieldsWithInstant =
        datasetFieldDao.findFieldsUuidsByJobVersion(jobVersionId.getVersion());
    return new ColumnNodes(
        fieldsWithInstant.stream().map(pair -> pair.getValue()).findAny().orElse(Instant.now()),
        fieldsWithInstant.stream().map(pair -> pair.getKey()).collect(Collectors.toList()));
  }

  private ColumnNodes getColumnNodes(DatasetId datasetId) {
    return new ColumnNodes(
        Instant.now(),
        datasetFieldDao.findDatasetFieldsUuids(
            datasetId.getNamespace().getValue(), datasetId.getName().getValue()));
  }

  private ColumnNodes getColumnNodes(DatasetFieldId datasetFieldId) {
    ColumnNodes columnNodes = new ColumnNodes(Instant.now(), new ArrayList<>());
    datasetFieldDao
        .findUuid(
            datasetFieldId.getDatasetId().getNamespace().getValue(),
            datasetFieldId.getDatasetId().getName().getValue(),
            datasetFieldId.getFieldName().getValue())
        .ifPresent(uuid -> columnNodes.nodeIds.add(uuid));
    return columnNodes;
  }

  private ColumnNodes getColumnNodes(JobId jobId) {
    return new ColumnNodes(
        Instant.now(),
        datasetFieldDao.findFieldsUuidsByJob(
            jobId.getNamespace().getValue(), jobId.getName().getValue()));
  }

  public void enrichWithColumnLineage(List<Dataset> datasets) {
    if (datasets.isEmpty()) {
      return;
    }

    Set<ColumnLineageNodeData> lineageRowsForDatasets =
        getLineageRowsForDatasets(
            datasets.stream()
                .map(d -> Pair.of(d.getNamespace().getValue(), d.getName().getValue()))
                .collect(Collectors.toList()));

    Map<Dataset, List<ColumnLineage>> datasetLineage = new HashMap<>();
    lineageRowsForDatasets.stream()
        .forEach(
            nodeData -> {
              Dataset dataset =
                  datasets.stream()
                      .filter(d -> d.getNamespace().getValue().equals(nodeData.getNamespace()))
                      .filter(d -> d.getName().getValue().equals(nodeData.getDataset()))
                      .findAny()
                      .get();

              if (!datasetLineage.containsKey(dataset)) {
                datasetLineage.put(dataset, new LinkedList<>());
              }
              datasetLineage
                  .get(dataset)
                  .add(
                      ColumnLineage.builder()
                          .name(nodeData.getField())
                          .inputFields(
                              nodeData.getInputFields().stream()
                                  .map(
                                      f ->
                                          new ColumnLineageInputField(
                                              f.getNamespace(),
                                              f.getDataset(),
                                              f.getField(),
                                              f.getTransformationDescription(),
                                              f.getTransformationType()))
                                  .collect(Collectors.toList()))
                          .build());
            });

    datasets.stream()
        .filter(dataset -> datasetLineage.containsKey(dataset))
        .forEach(dataset -> dataset.setColumnLineage(datasetLineage.get(dataset)));
  }

  private record ColumnNodes(Instant createdAtUntil, List<UUID> nodeIds) {}
}
