package marquez.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.models.LineageEvent;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.BuiltinHighlighterType;
import org.opensearch.client.opensearch.core.search.HighlighterType;

@Slf4j
public class SearchService {

  private final OpenSearchClient openSearchClient;

  public SearchService(@NonNull final OpenSearchClient openSearchClient) {
    this.openSearchClient = openSearchClient;
  }

  public SearchResponse<ObjectNode> searchDatasets(String query) throws IOException {
    String[] fields = {
      "run_id",
      "name",
      "namespace",
      "facets.schema.fields.name",
      "facets.schema.fields.type",
      "facets.columnLineage.fields.*.inputFields.name",
      "facets.columnLineage.fields.*.inputFields.namespace",
      "facets.columnLineage.fields.*.inputFields.field",
      "facets.columnLineage.fields.*.transformationDescription",
      "facets.columnLineage.fields.*.transformationType"
    };
    return this.openSearchClient.search(
        s ->
            s.index("datasets")
                .query(
                    q ->
                        q.multiMatch(
                            m ->
                                m.query(query)
                                    .type(TextQueryType.PhrasePrefix)
                                    .fields(Arrays.stream(fields).toList())
                                    .operator(Operator.Or)))
                .highlight(
                    hl -> {
                      for (String field : fields) {
                        hl.fields(
                            field,
                            f ->
                                f.type(
                                    HighlighterType.of(
                                        fn -> fn.builtin(BuiltinHighlighterType.Plain))));
                      }
                      return hl;
                    }),
        ObjectNode.class);
  }

  public SearchResponse<ObjectNode> searchJobs(String query) throws IOException {
    String[] fields = {
      "facets.sql.query",
      "facets.sourceCode.sourceCode",
      "facets.sourceCode.language",
      "runFacets.processing_engine.name",
      "run_id",
      "name",
      "namespace",
      "type"
    };
    return this.openSearchClient.search(
        s -> {
          s.index("jobs")
              .query(
                  q ->
                      q.multiMatch(
                          m ->
                              m.query(query)
                                  .type(TextQueryType.PhrasePrefix)
                                  .fields(Arrays.stream(fields).toList())
                                  .operator(Operator.Or)));
          s.highlight(
              hl -> {
                for (String field : fields) {
                  hl.fields(
                      field,
                      f ->
                          f.type(
                              HighlighterType.of(fn -> fn.builtin(BuiltinHighlighterType.Plain))));
                }
                return hl;
              });
          return s;
        },
        ObjectNode.class);
  }

  public void indexEvent(@Valid @NotNull LineageEvent event) {
    UUID runUuid = runUuidFromEvent(event.getRun());
    log.info("Indexing event {}", event);

    if (event.getInputs() != null) {
      indexDatasets(event.getInputs(), runUuid, event);
    }
    if (event.getOutputs() != null) {
      indexDatasets(event.getOutputs(), runUuid, event);
    }
    indexJob(runUuid, event);
  }

  private UUID runUuidFromEvent(LineageEvent.Run run) {
    UUID runUuid;
    try {
      runUuid = UUID.fromString(run.getRunId());
    } catch (Exception e) {
      runUuid = UUID.nameUUIDFromBytes(run.getRunId().getBytes(StandardCharsets.UTF_8));
    }
    return runUuid;
  }

  private Map<String, Object> buildJobIndexRequest(UUID runUuid, LineageEvent event) {
    Map<String, Object> jsonMap = new HashMap<>();

    jsonMap.put("run_id", runUuid.toString());
    jsonMap.put("eventType", event.getEventType());
    jsonMap.put("name", event.getJob().getName());
    jsonMap.put("type", event.getJob().isStreamingJob() ? "STREAM" : "BATCH");
    jsonMap.put("namespace", event.getJob().getNamespace());
    jsonMap.put("facets", event.getJob().getFacets());
    jsonMap.put("runFacets", event.getRun().getFacets());
    return jsonMap;
  }

  private Map<String, Object> buildDatasetIndexRequest(
      UUID runUuid, LineageEvent.Dataset dataset, LineageEvent event) {
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("run_id", runUuid.toString());
    jsonMap.put("eventType", event.getEventType());
    jsonMap.put("name", dataset.getName());
    jsonMap.put("inputFacets", dataset.getInputFacets());
    jsonMap.put("outputFacets", dataset.getOutputFacets());
    jsonMap.put("namespace", dataset.getNamespace());
    jsonMap.put("facets", dataset.getFacets());
    return jsonMap;
  }

  private void indexJob(UUID runUuid, LineageEvent event) {
    index(
        IndexRequest.of(
            i ->
                i.index("jobs")
                    .id(
                        String.format(
                            "JOB:%s:%s", event.getJob().getNamespace(), event.getJob().getName()))
                    .document(buildJobIndexRequest(runUuid, event))));
  }

  private void indexDatasets(
      List<LineageEvent.Dataset> datasets, UUID runUuid, LineageEvent event) {
    datasets.stream()
        .map(dataset -> buildDatasetIndexRequest(runUuid, dataset, event))
        .forEach(
            jsonMap ->
                index(
                    IndexRequest.of(
                        i ->
                            i.index("datasets")
                                .id(
                                    String.format(
                                        "DATASET:%s:%s",
                                        jsonMap.get("namespace"), jsonMap.get("name")))
                                .document(jsonMap))));
  }

  private void index(IndexRequest<Map<String, Object>> request) {
    try {
      this.openSearchClient.index(request);
    } catch (IOException e) {
      log.info("Failed to index event OpenSearch not available.", e);
    }
  }
}
