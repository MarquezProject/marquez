package marquez.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.client.MarquezPathV1.createRunPath;
import static marquez.client.MarquezPathV1.datasetPath;
import static marquez.client.MarquezPathV1.jobPath;
import static marquez.client.MarquezPathV1.namespacePath;
import static marquez.client.MarquezPathV1.runTransitionPath;
import static marquez.client.MarquezPathV1.sourcePath;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import marquez.client.models.DatasetMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SourceMeta;

@Slf4j
class MarquezWriteOnlyClientImpl implements MarquezWriteOnlyClient {

  private final Backend backend;

  public MarquezWriteOnlyClientImpl(Backend backend) {
    this.backend = backend;
  }

  private static String pathWithQueryParams(String path, Map<String, Object> queryParams) {
    StringBuilder pathBuilder = new StringBuilder(path);
    if (queryParams != null && !queryParams.isEmpty()) {
      boolean first = true;
      for (Entry<String, Object> entry : queryParams.entrySet()) {
        if (first) {
          pathBuilder.append("?");
          first = false;
        } else {
          pathBuilder.append("&");
        }
        final String paramName = entry.getKey();
        final String paramValue = String.valueOf(entry.getValue());
        try {
          final String paramNameEncoded = URLEncoder.encode(paramName, UTF_8.name());
          final String paramValueEncoded = URLEncoder.encode(paramValue, UTF_8.name());
          pathBuilder.append(paramNameEncoded).append("=").append(paramValueEncoded);
        } catch (UnsupportedEncodingException e) {
          log.error("Failed to append param '{}' with value '{}'.", paramName, paramValue, e);
          throw new MarquezClientException(e);
        }
      }
    }
    return pathBuilder.toString();
  }

  @Override
  public void createNamespace(String namespaceName, NamespaceMeta namespaceMeta) {
    backend.put(namespacePath(namespaceName), namespaceMeta.toJson());
  }

  @Override
  public void createSource(String sourceName, SourceMeta sourceMeta) {
    backend.put(sourcePath(sourceName), sourceMeta.toJson());
  }

  @Override
  public void createDataset(String namespaceName, String datasetName, DatasetMeta datasetMeta) {
    backend.put(datasetPath(namespaceName, datasetName), datasetMeta.toJson());
  }

  @Override
  public void createJob(String namespaceName, String jobName, JobMeta jobMeta) {
    backend.put(jobPath(namespaceName, jobName), jobMeta.toJson());
  }

  @Override
  public void createRun(String namespaceName, String jobName, RunMeta runMeta) {
    backend.post(createRunPath(namespaceName, jobName), runMeta.toJson());
  }

  @Override
  public void markRunAs(String runId, RunState runState, Instant at) {
    Map<String, Object> queryParams =
        at == null ? null : ImmutableMap.of("at", ISO_INSTANT.format(at));
    backend.post(pathWithQueryParams(runTransitionPath(runId, runState), queryParams));
  }

  @Override
  public void close() throws IOException {
    backend.close();
  }
}
