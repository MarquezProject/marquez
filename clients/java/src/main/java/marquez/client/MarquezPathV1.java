/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.client.models.RunState;
import org.apache.maven.shared.utils.StringUtils;

class MarquezPathV1 {
  @VisibleForTesting static final String BASE_PATH = "/api/v1";

  @VisibleForTesting
  static String path(String pathTemplate, @Nullable String... pathArgs) {
    /*
     Converts path template (prepended with BASE_PATH), where path parts are separated
     by slashes, and formats strings filling pathArgs in place of %s placeholder.
     This is required to properly handle double slashes in URLs using URIBuilder.
     Example:
     Path template "/namespaces/%s/datasets/%s/versions/%s"
     with args
     "s3://bucket", "dName", "vName"
     is converted to
     "/api/v1/namespaces/s3%3A%2F%2Fbucket/datasets/nName/versions/vName"
    */
    int argsLength = pathArgs == null ? 0 : pathArgs.length;
    if (StringUtils.countMatches(pathTemplate, "%s") != argsLength) {
      throw new MarquezClientException(
          String.format(
              "Amount of placeholders %s differ from amount of provided path arguments %s",
              pathTemplate.split("%s").length - 1, argsLength));
    }

    pathTemplate = BASE_PATH + pathTemplate;
    if (pathArgs == null) {
      return pathTemplate;
    }

    // Replace placeholders with path templates, removing empty strings
    Iterator<String> iterator = Arrays.stream(pathArgs).iterator();
    return Stream.of(pathTemplate.split("/"))
        .filter(it -> it != null && !it.isEmpty())
        .map(it -> it.equals("%s") ? iterator.next() : it)
        .map(MarquezPathV1::encode)
        .collect(Collectors.joining("/", "/", ""));
  }

  static String encode(String input) {
    try {
      return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new MarquezClientException(e);
    }
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

  static String listJobVersionsPath(@NonNull String namespaceName, String jobName) {
    return path("/namespaces/%s/jobs/%s/versions", namespaceName, jobName);
  }

  static String jobVersionPath(String namespaceName, String jobName, String version) {
    return path("/namespaces/%s/jobs/%s/versions/%s", namespaceName, jobName, version);
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

  static String searchPath() {
    return path("/search");
  }
}
