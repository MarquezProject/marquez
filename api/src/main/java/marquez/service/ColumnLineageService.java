/*
 * Copyright 2018-2022 contributors to the Marquez project
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
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.db.ColumnLineageDao;
import marquez.db.DatasetFieldDao;
import marquez.db.models.ColumnLineageNodeData;
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

  public Lineage lineage(NodeId nodeId, int depth, boolean withDownstream, Instant createdAtUntil) {
    List<UUID> columnNodeUuids = getColumnNodeUuids(nodeId);
    if (columnNodeUuids.isEmpty()) {
      throw new NodeIdNotFoundException("Could not find node");
    }
    return toLineage(getLineage(depth, columnNodeUuids, withDownstream, createdAtUntil));
  }

  private Lineage toLineage(Set<ColumnLineageNodeData> lineageNodeData) {
    Map<NodeId, Node.Builder> graphNodes = new HashMap<>();
    Map<NodeId, List<NodeId>> inEdges = new HashMap<>();
    Map<NodeId, List<NodeId>> outEdges = new HashMap<>();

    // create nodes
    lineageNodeData.stream()
        .forEach(
            columnLineageNodeData -> {
              NodeId nodeId =
                  NodeId.of(
                      DatasetFieldId.of(
                          columnLineageNodeData.getNamespace(),
                          columnLineageNodeData.getDataset(),
                          columnLineageNodeData.getField()));
              graphNodes.put(nodeId, Node.datasetField().data(columnLineageNodeData).id(nodeId));
              columnLineageNodeData.getInputFields().stream()
                  .map(
                      i ->
                          NodeId.of(
                              DatasetFieldId.of(i.getNamespace(), i.getDataset(), i.getField())))
                  .forEach(
                      inputNodeId -> {
                        graphNodes.putIfAbsent(inputNodeId, Node.datasetField().id(inputNodeId));
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

  List<UUID> getColumnNodeUuids(NodeId nodeId) {
    List<UUID> columnNodeUuids = new ArrayList<>();
    if (nodeId.isDatasetType()) {
      DatasetId datasetId = nodeId.asDatasetId();
      columnNodeUuids.addAll(
          datasetFieldDao.findDatasetFieldsUuids(
              datasetId.getNamespace().getValue(), datasetId.getName().getValue()));
    } else if (nodeId.isDatasetFieldType()) {
      DatasetFieldId datasetFieldId = nodeId.asDatasetFieldId();
      datasetFieldDao
          .findUuid(
              datasetFieldId.getDatasetId().getNamespace().getValue(),
              datasetFieldId.getDatasetId().getName().getValue(),
              datasetFieldId.getFieldName().getValue())
          .ifPresent(uuid -> columnNodeUuids.add(uuid));
    } else if (nodeId.isJobType()) {
      JobId jobId = nodeId.asJobId();
      columnNodeUuids.addAll(
          datasetFieldDao.findFieldsUuidsByJob(
              jobId.getNamespace().getValue(), jobId.getName().getValue()));
    }
    return columnNodeUuids;
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
                          .transformationDescription(nodeData.getTransformationDescription())
                          .transformationType(nodeData.getTransformationType())
                          .inputFields(
                              nodeData.getInputFields().stream()
                                  .map(
                                      f ->
                                          new ColumnLineageInputField(
                                              f.getNamespace(), f.getDataset(), f.getField()))
                                  .collect(Collectors.toList()))
                          .build());
            });

    datasets.stream()
        .filter(dataset -> datasetLineage.containsKey(dataset))
        .forEach(dataset -> dataset.setColumnLineage(datasetLineage.get(dataset)));
  }
}
