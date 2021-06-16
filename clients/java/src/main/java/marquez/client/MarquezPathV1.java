package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.client.models.RunState;

class MarquezPathV1 {
  @VisibleForTesting static final String BASE_PATH = "/api/v1";

  @VisibleForTesting
  static List<String> path(String pathTemplate, @Nullable String... pathArgs) {
    /*
     Converts path template (prepended with BASE_PATH), where path parts are separated
     by slashes, and formats strings filling pathArgs in place of %s placeholder.
     This is required to properly handle double slashes in URLs using URIBuilder.
     Example:
     Path template "/namespaces/%s/datasets/%s/versions/%s"
     with args
     "nName", "dName", "vName"
     is converted to list
     ["api", "v1", "namespaces", "nName", "datasets", "dName", "versions", "vName"]
    */
    List<String> resultPath = new ArrayList<>();
    pathTemplate = BASE_PATH + pathTemplate;
    int argsIndex = 0;
    for (String part : pathTemplate.split("/")) {
      if (part == null || part.isEmpty()) {
        continue;
      }
      if (part.equals("%s")) {
        resultPath.add(pathArgs[argsIndex]);
        argsIndex++;
      } else {
        resultPath.add(part);
      }
    }
    return resultPath;
  }

  static List<String> listNamespacesPath() {
    return path("/namespaces");
  }

  static List<String> namespacePath(String namespaceName) {
    return path("/namespaces/%s", namespaceName);
  }

  static List<String> sourcePath(String sourceName) {
    return path("/sources/%s", sourceName);
  }

  static List<String> listSourcesPath() {
    return path("/sources");
  }

  static List<String> datasetVersionPath(
      @NonNull final String namespaceName,
      @NonNull final String datasetName,
      @NonNull final String version) {
    return path("/namespaces/%s/datasets/%s/versions/%s", namespaceName, datasetName, version);
  }

  static List<String> listDatasetVersionsPath(
      @NonNull final String namespaceName, @NonNull final String datasetName) {
    return path("/namespaces/%s/datasets/%s/versions", namespaceName, datasetName);
  }

  static List<String> listDatasetsPath(@NonNull String namespaceName) {
    return path("/namespaces/%s/datasets", namespaceName);
  }

  static List<String> datasetPath(String namespaceName, String datasetName) {
    return path("/namespaces/%s/datasets/%s", namespaceName, datasetName);
  }

  static List<String> listJobsPath(@NonNull String namespaceName) {
    return path("/namespaces/%s/jobs", namespaceName);
  }

  static List<String> jobPath(String namespaceName, String jobName) {
    return path("/namespaces/%s/jobs/%s", namespaceName, jobName);
  }

  static List<String> createRunPath(String namespaceName, String jobName) {
    return path("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
  }

  static List<String> runPath(@NonNull String runId) {
    return path("/jobs/runs/%s", runId);
  }

  static List<String> listRunsPath(@NonNull String namespaceName, @NonNull String jobName) {
    return path("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
  }

  static List<String> runTransitionPath(String runId, RunState runState) {
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

  static List<String> datasetTagPath(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String tagName) {
    return path("/namespaces/%s/datasets/%s/tags/%s", namespaceName, datasetName, tagName);
  }

  static List<String> fieldTagPath(
      String namespaceName, String datasetName, String fieldName, String tagName) {
    return path(
        "/namespaces/%s/datasets/%s/fields/%s/tags/%s",
        namespaceName, datasetName, fieldName, tagName);
  }

  static List<String> listTagsPath() {
    return path("/tags");
  }
}
