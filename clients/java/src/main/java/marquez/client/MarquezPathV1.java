package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.client.models.RunState;

class MarquezPathV1 {

  @VisibleForTesting static final String BASE_PATH = "/api/v1";

  @VisibleForTesting
  static String path(String pathTemplate, @Nullable String... pathArgs) {
    return BASE_PATH + String.format(pathTemplate, (Object[]) pathArgs);
  }

  static String listNamespacesPath() {
    return path("/namespaces");
  }

  static String namespacePath(String namespaceName) {
    return path("/namespaces/%s", namespaceName);
  }

  static String sourcePath(String sourceName) {
    return path("/sources/%s", sourceName);
  }

  static String listSourcesPath() {
    return path("/sources");
  }

  static String datasetVersionPath(
      @NonNull final String namespaceName,
      @NonNull final String datasetName,
      @NonNull final String version) {
    return path("/namespaces/%s/datasets/%s/versions/%s", namespaceName, datasetName, version);
  }

  static String listDatasetVersionsPath(
      @NonNull final String namespaceName, @NonNull final String datasetName) {
    return path("/namespaces/%s/datasets/%s/versions", namespaceName, datasetName);
  }

  static String listDatasetsPath(@NonNull String namespaceName) {
    return path("/namespaces/%s/datasets", namespaceName);
  }

  static String datasetPath(String namespaceName, String datasetName) {
    return path("/namespaces/%s/datasets/%s", namespaceName, datasetName);
  }

  static String listJobsPath(@NonNull String namespaceName) {
    return path("/namespaces/%s/jobs", namespaceName);
  }

  static String jobPath(String namespaceName, String jobName) {
    return path("/namespaces/%s/jobs/%s", namespaceName, jobName);
  }

  static String createRunPath(String namespaceName, String jobName) {
    return path("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
  }

  static String runPath(@NonNull String runId) {
    return path("/jobs/runs/%s", runId);
  }

  static String listRunsPath(@NonNull String namespaceName, @NonNull String jobName) {
    return path("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
  }

  static String runTransitionPath(String runId, RunState runState) {
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
    return path("/jobs/runs/%s/%s", runId, transition);
  }

  static String datasetTagPath(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String tagName) {
    return path("/namespaces/%s/datasets/%s/tags/%s", namespaceName, datasetName, tagName);
  }

  static String fieldTagPath(
      String namespaceName, String datasetName, String fieldName, String tagName) {
    return path(
        "/namespaces/%s/datasets/%s/fields/%s/tags/%s",
        namespaceName, datasetName, fieldName, tagName);
  }

  static String listTagsPath() {
    return path("/tags");
  }

  static String createTagPath(String name) {
    return path("/tags/%s", name);
  }
}
