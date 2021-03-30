package marquez.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetId;
import marquez.common.models.JobId;
import marquez.db.LineageDao;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.service.DelegatingDaos.DelegatingLineageDao;
import marquez.service.models.Edge;
import marquez.service.models.Graph;
import marquez.service.models.Lineage;
import marquez.service.models.Node;
import marquez.service.models.NodeId;
import marquez.service.models.NodeType;
import marquez.service.models.Run;

@Slf4j
public class LineageService extends DelegatingLineageDao {
  public LineageService(LineageDao delegate) {
    super(delegate);
  }

  public Lineage lineage(NodeId nodeId, int depth) throws ExecutionException, InterruptedException {
    Optional<UUID> optionalUUID = getJobUuid(nodeId);
    if (optionalUUID.isEmpty()) {
      throw new NodeIdNotFoundException("Could not find node");
    }
    UUID job = optionalUUID.get();

    Set<UUID> seen = new LinkedHashSet<>();
    Set<UUID> cur = new HashSet<>();
    cur.add(job);

    int i = 0;
    do {
      cur = getLineage(cur);
      cur = new HashSet<UUID>(cur); // allow it to be mutable
      cur.removeAll(seen); // remove any dupes from current
      seen.addAll(cur); // add to seen list
    } while (i++ < depth && !cur.isEmpty()); // working set is empty or depth is reached
    seen.add(job); // include the base job

    CompletableFuture<List<JobData>> jobFuture = CompletableFuture.supplyAsync(() -> getJob(seen));
    CompletableFuture<List<DatasetData>> inFuture =
        CompletableFuture.supplyAsync(() -> getInputDatasetsFromJobIds(seen));
    CompletableFuture<List<DatasetData>> outFuture =
        CompletableFuture.supplyAsync(() -> getOutputDatasetsFromJobIds(seen));
    CompletableFuture.allOf(jobFuture, inFuture, outFuture).get();

    List<Run> runs = getCurrentRuns(seen);
    List<JobData> jobData = jobFuture.get();
    // todo fix runtime
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

    return toLineage(seen, jobData, inFuture.get(), outFuture.get());
  }

  private Lineage toLineage(
      Set<UUID> seen, List<JobData> jobData, List<DatasetData> input, List<DatasetData> output) {
    Set<Node> nodes = new LinkedHashSet<>();
    // build mapping for later
    Map<UUID, Set<DatasetData>> jobToDsInput = new HashMap<>();
    Map<DatasetData, Set<UUID>> dsInputToJob = new HashMap<>();
    for (DatasetData data : input) {
      for (UUID jobUuid : data.getJobUuids()) {
        jobToDsInput.computeIfAbsent(jobUuid, e -> new HashSet<>()).add(data);
      }
      dsInputToJob.computeIfAbsent(data, e -> new HashSet<>()).addAll(data.getJobUuids());
    }

    Map<UUID, Set<DatasetData>> jobToDsOutput = new HashMap<>();
    Map<DatasetData, Set<UUID>> dsOutputToJob = new HashMap<>();
    for (DatasetData data : output) {
      for (UUID jobUuid : data.getJobUuids()) {
        jobToDsOutput.computeIfAbsent(jobUuid, e -> new HashSet<>()).add(data);
      }
      dsOutputToJob.computeIfAbsent(data, e -> new HashSet<>()).addAll(data.getJobUuids());
    }

    // build jobs
    Map<UUID, JobData> jobDataMap = Maps.uniqueIndex(jobData, JobData::getUuid);
    for (UUID jobUuid : seen) {
      JobData data = jobDataMap.get(jobUuid);
      if (data == null) {
        log.error("Could not find job node for {}", jobData);
        continue;
      }

      NodeId origin = NodeId.of(new JobId(data.getNamespace(), data.getName()));
      Node node =
          new Node(
              origin,
              NodeType.JOB,
              data,
              buildDatasetEdge(jobToDsInput.get(jobUuid), origin),
              buildDatasetEdge(origin, jobToDsOutput.get(jobUuid)));
      nodes.add(node);
    }

    Set<DatasetData> datasets = new LinkedHashSet<>();
    datasets.addAll(input);
    datasets.addAll(output);
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
      return getJobUuid(jobId.getName().getValue(), jobId.getNamespace().getValue());
    } else if (nodeId.isDatasetType()) {
      DatasetId datasetId = nodeId.asDatasetId();
      return getJobFromInputOrOutput(
          datasetId.getName().getValue(), datasetId.getNamespace().getValue());
    } else {
      throw new NodeIdNotFoundException("Node must be a dataset node or job node");
    }
  }
}
