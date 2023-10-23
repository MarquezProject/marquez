/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.db.JobDao;
import marquez.db.LineageDao;
import marquez.db.models.JobRow;
import marquez.service.DelegatingDaos.DelegatingLineageDao;
import marquez.service.models.DatasetData;
import marquez.service.models.Edge;
import marquez.service.models.Graph;
import marquez.service.models.JobData;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import marquez.service.models.NodeType;
import marquez.service.models.Run;

@Slf4j
public class LineageService extends DelegatingLineageDao {

  public record JobWithParent(JobId job, JobId parent) {
  }

  public record DatasetLineage(DatasetId dataset, Collection<JobWithParent> consumers, Collection<JobWithParent> producers) {
  }

  public record ChildLineage(JobId job, Collection<DatasetLineage> inputs, Collection<DatasetLineage> outputs) {
  }

  public record ParentLineage(JobId parent, Collection<ChildLineage> children) {
  }

  private final JobDao jobDao;

  public LineageService(LineageDao delegate, JobDao jobDao) {
    super(delegate);
    this.jobDao = jobDao;
  }

  /**
   * This method is specialized for returning one level of lineage from a parent job.
   * It finds all the children of the provided parent node
   * It then finds the input and output datasets those children write to.
   * It finally returns the other jobs consuming or producing those datasets (and their parent).
   * @param parentJobId the parent job
   * @return 1 level of lineage for all the children jobs of the given parent
   */
  public ParentLineage parentDirectLineage(JobId parentJobId) {
    log.debug("Attempting to get lineage for parent job '{}'", parentJobId);

    Collection<SimpleLineageEdge> directLineageFromParent =
        getDirectLineageFromParent(parentJobId.getNamespace().getValue(), parentJobId.getName().getValue());


    Map<JobId, Map<String, Map<DatasetId, Map<String, List<JobWithParent>>>>> grouped =
        directLineageFromParent.stream().collect(
        groupingBy(SimpleLineageEdge::job1,
            filtering(e -> e.direction() != null,
            groupingBy(SimpleLineageEdge::direction,
                filtering(e -> e.dataset() != null,
                groupingBy(SimpleLineageEdge::dataset,
                    filtering(e -> e.direction2() != null,
                    groupingBy(SimpleLineageEdge::direction2,
                        mapping(e -> new JobWithParent(e.job2(), e.job2parent()),
                        toList())))))))));

    List<ChildLineage> children = grouped.entrySet().stream().map(
        e -> new ChildLineage(
              e.getKey(),
              toDatasetLineages(e.getValue().get("INPUT")),
              toDatasetLineages(e.getValue().get("OUTPUT"))
            )
      ).collect(toList());
    return new ParentLineage(parentJobId, children);
  }

  private Collection<DatasetLineage> toDatasetLineages(Map<DatasetId, Map<String, List<JobWithParent>>> datasets) {
    return datasets == null ? null : datasets.entrySet().stream().map(
        e -> new DatasetLineage(
              e.getKey(),
              e.getValue().get("INPUT"),
              e.getValue().get("OUTPUT")
            )
      ).collect(toList());
  }

  // TODO make input parameters easily extendable if adding more options like 'withJobFacets'
  public Lineage lineage(NodeId nodeId, int depth, boolean withRunFacets) {
    log.debug("Attempting to get lineage for node '{}' with depth '{}'", nodeId.getValue(), depth);
    Optional<UUID> optionalUUID = getJobUuid(nodeId);
    if (optionalUUID.isEmpty()) {
      log.warn(
          "Failed to get job associated with node '{}', returning orphan graph...",
          nodeId.getValue());
      return toLineageWithOrphanDataset(nodeId.asDatasetId());
    }
    UUID job = optionalUUID.get();
    log.debug("Attempting to get lineage for job '{}'", job);
    Set<JobData> jobData = getLineage(Collections.singleton(job), depth);

    // Ensure job data is not empty, an empty set cannot be passed to LineageDao.getCurrentRuns() or
    // LineageDao.getCurrentRunsWithFacets().
    if (jobData.isEmpty()) {
      // Log warning, then return an orphan lineage graph; a graph should contain at most one
      // job->dataset relationship.
      log.warn(
          "Failed to get lineage for job '{}' associated with node '{}', returning orphan graph...",
          job,
          nodeId.getValue());
      return toLineageWithOrphanDataset(nodeId.asDatasetId());
    }

    List<Run> runs =
        withRunFacets
            ? getCurrentRunsWithFacets(
                jobData.stream().map(JobData::getUuid).collect(Collectors.toSet()))
            : getCurrentRuns(jobData.stream().map(JobData::getUuid).collect(Collectors.toSet()));

    for (JobData j : jobData) {
      if (j.getLatestRun().isEmpty()) {
        for (Run run : runs) {
          if (j.getName().getValue().equalsIgnoreCase(run.getJobName())
              && j.getNamespace().getValue().equalsIgnoreCase(run.getNamespaceName())) {
            j.setLatestRun(run);
            break;
          }
        }
      }
    }
    Set<UUID> datasetIds =
        jobData.stream()
            .flatMap(jd -> Stream.concat(jd.getInputUuids().stream(), jd.getOutputUuids().stream()))
            .collect(Collectors.toSet());
    Set<DatasetData> datasets = new HashSet<>();
    if (!datasetIds.isEmpty()) {
      datasets.addAll(this.getDatasetData(datasetIds));
    }
    if (nodeId.isDatasetType()
        && datasets.stream().noneMatch(n -> n.getId().equals(nodeId.asDatasetId()))) {
      log.warn(
          "Found jobs {} which no longer share lineage with dataset '{}' - discarding",
          jobData.stream().map(JobData::getId).toList(),
          nodeId.getValue());
      return toLineageWithOrphanDataset(nodeId.asDatasetId());
    }

    return toLineage(jobData, datasets);
  }

  private Lineage toLineageWithOrphanDataset(@NonNull DatasetId datasetId) {
    final DatasetData datasetData =
        getDatasetData(datasetId.getNamespace().getValue(), datasetId.getName().getValue());
    return new Lineage(
        ImmutableSortedSet.of(
            Node.dataset().data(datasetData).id(NodeId.of(datasetData.getId())).build()));
  }

  private Lineage toLineage(Set<JobData> jobData, Set<DatasetData> datasets) {
    Set<Node> nodes = new LinkedHashSet<>();
    // build mapping for later
    Map<UUID, DatasetData> datasetById =
        datasets.stream().collect(Collectors.toMap(DatasetData::getUuid, Functions.identity()));

    Map<DatasetData, Set<UUID>> dsInputToJob = new HashMap<>();
    Map<DatasetData, Set<UUID>> dsOutputToJob = new HashMap<>();
    // build jobs
    Map<UUID, JobData> jobDataMap = Maps.uniqueIndex(jobData, JobData::getUuid);
    for (JobData data : jobData) {
      if (data == null) {
        log.error("Could not find job node for {}", jobData);
        continue;
      }
      Set<DatasetData> inputs =
          data.getInputUuids().stream()
              .map(datasetById::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      Set<DatasetData> outputs =
          data.getOutputUuids().stream()
              .map(datasetById::get)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());
      data.setInputs(buildDatasetId(inputs));
      data.setOutputs(buildDatasetId(outputs));

      inputs.forEach(
          ds -> dsInputToJob.computeIfAbsent(ds, e -> new HashSet<>()).add(data.getUuid()));
      outputs.forEach(
          ds -> dsOutputToJob.computeIfAbsent(ds, e -> new HashSet<>()).add(data.getUuid()));

      NodeId origin = NodeId.of(new JobId(data.getNamespace(), data.getName()));
      Node node =
          new Node(
              origin,
              NodeType.JOB,
              data,
              buildDatasetEdge(inputs, origin),
              buildDatasetEdge(origin, outputs));
      nodes.add(node);
    }

    for (DatasetData dataset : datasets) {
      NodeId origin = NodeId.of(new DatasetId(dataset.getNamespace(), dataset.getName()));
      Node node =
          new Node(
              origin,
              NodeType.DATASET,
              dataset,
              buildJobEdge(dsOutputToJob.get(dataset), origin, jobDataMap),
              buildJobEdge(origin, dsInputToJob.get(dataset), jobDataMap));
      nodes.add(node);
    }

    return new Lineage(Lineage.withSortedNodes(Graph.directed().nodes(nodes).build()));
  }

  private ImmutableSet<DatasetId> buildDatasetId(Set<DatasetData> datasetData) {
    if (datasetData == null) {
      return ImmutableSet.of();
    }
    return datasetData.stream()
        .map(ds -> new DatasetId(ds.getNamespace(), ds.getName()))
        .collect(ImmutableSet.toImmutableSet());
  }

  private ImmutableSet<Edge> buildJobEdge(
      NodeId origin, Set<UUID> uuids, Map<UUID, JobData> jobDataMap) {
    if (uuids == null) {
      return ImmutableSet.of();
    }
    return uuids.stream()
        .map(jobDataMap::get)
        .filter(Objects::nonNull)
        .map(j -> new Edge(origin, buildEdge(j)))
        .collect(ImmutableSet.toImmutableSet());
  }

  private ImmutableSet<Edge> buildJobEdge(
      Set<UUID> uuids, NodeId origin, Map<UUID, JobData> jobDataMap) {
    if (uuids == null) {
      return ImmutableSet.of();
    }
    return uuids.stream()
        .map(jobDataMap::get)
        .filter(Objects::nonNull)
        .map(j -> new Edge(buildEdge(j), origin))
        .collect(ImmutableSet.toImmutableSet());
  }

  private ImmutableSet<Edge> buildDatasetEdge(NodeId nodeId, Set<DatasetData> datasetData) {
    if (datasetData == null) {
      return ImmutableSet.of();
    }
    return datasetData.stream()
        .map(ds -> new Edge(nodeId, buildEdge(ds)))
        .collect(ImmutableSet.toImmutableSet());
  }

  private ImmutableSet<Edge> buildDatasetEdge(Set<DatasetData> datasetData, NodeId nodeId) {
    if (datasetData == null) {
      return ImmutableSet.of();
    }
    return datasetData.stream()
        .map(ds -> new Edge(buildEdge(ds), nodeId))
        .collect(ImmutableSet.toImmutableSet());
  }

  private NodeId buildEdge(DatasetData ds) {
    return NodeId.of(new DatasetId(ds.getNamespace(), ds.getName()));
  }

  private NodeId buildEdge(JobData e) {
    return NodeId.of(new JobId(e.getNamespace(), e.getName()));
  }

  public Optional<UUID> getJobUuid(NodeId nodeId) {
    if (nodeId.isJobType()) {
      JobId jobId = nodeId.asJobId();
      return jobDao
          .findJobByNameAsRow(jobId.getNamespace().getValue(), jobId.getName().getValue())
          .map(JobRow::getUuid);
    } else if (nodeId.isDatasetType()) {
      DatasetId datasetId = nodeId.asDatasetId();
      return getJobFromInputOrOutput(
          datasetId.getName().getValue(), datasetId.getNamespace().getValue());
    } else {
      throw new NodeIdNotFoundException(
          String.format("Node '%s' must be of type dataset or job!", nodeId.getValue()));
    }
  }
}
