package marquez.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.google.common.collect.ImmutableMap;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import marquez.client.models.DatasetMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SourceMeta;

class MarquezWriteOnlyClientImpl implements MarquezWriteOnlyClient {

  private static final String BASE_PATH = "/api/v1";

  private final Backend backend;

  public MarquezWriteOnlyClientImpl(Backend backend) {
    this.backend = backend;
  }

  private String path(String pathTemplate, @Nullable String... pathArgs) {
    return path(String.format(pathTemplate, (Object[]) pathArgs), ImmutableMap.of());
  }

  private String path(
      String pathTemplate, Map<String, Object> queryParams, @Nullable String... pathArgs) {
    return path(String.format(pathTemplate, (Object[]) pathArgs), queryParams);
  }

  private String path(String path, Map<String, Object> queryParams) {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder.append(BASE_PATH).append(path);
    if (queryParams != null && !queryParams.isEmpty()) {
      boolean first = true;
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        if (first) {
          pathBuilder.append("?");
          first = false;
        } else {
          pathBuilder.append("&");
        }
        String paramName = URLEncoder.encode(entry.getKey(), UTF_8);
        String paramValue = URLEncoder.encode(String.valueOf(entry.getValue()), UTF_8);
        pathBuilder.append(paramName).append("=").append(paramValue);
      }
    }
    return pathBuilder.toString();
  }

  @Override
  public void createNamespace(String namespaceName, NamespaceMeta namespaceMeta) {
    backend.put(path("/namespaces/%s", namespaceName), namespaceMeta.toJson());
  }

  @Override
  public void createSource(String sourceName, SourceMeta sourceMeta) {
    backend.put(path("/sources/%s", sourceName), sourceMeta.toJson());
  }

  @Override
  public void createDataset(String namespaceName, String datasetName, DatasetMeta datasetMeta) {
    backend.put(
        path("/namespaces/%s/datasets/%s", namespaceName, datasetName), datasetMeta.toJson());
  }

  @Override
  public void createJob(String namespaceName, String jobName, JobMeta jobMeta) {
    backend.put(path("/namespaces/%s/jobs/%s", namespaceName, jobName), jobMeta.toJson());
  }

  @Override
  public void createRun(String namespaceName, String jobName, RunMeta runMeta) {
    backend.post(path("/namespaces/%s/jobs/%s/runs", namespaceName, jobName), runMeta.toJson());
  }

  @Override
  public void markRunAs(String runId, RunState runState, Instant at) {
    final String transition;
    switch (runState) {
      case RUNNING:
        transition = "start";
        break;
      case COMPLETED:
        transition = "complete";
        break;
      case ABORTED:
        transition = "abort";
        break;
      case FAILED:
        transition = "fail";
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Unexpected run state: %s", runState.name()));
    }
    backend.post(
        path(
            "/jobs/runs/%s/%s",
            at == null ? null : ImmutableMap.of("at", ISO_INSTANT.format(at)), runId, transition));
  }
}
