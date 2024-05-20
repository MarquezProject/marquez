/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.client.MarquezPathV1.columnLineagePath;
import static marquez.client.MarquezPathV1.createRunPath;
import static marquez.client.MarquezPathV1.createTagPath;
import static marquez.client.MarquezPathV1.datasetPath;
import static marquez.client.MarquezPathV1.datasetTagPath;
import static marquez.client.MarquezPathV1.datasetVersionPath;
import static marquez.client.MarquezPathV1.fieldTagPath;
import static marquez.client.MarquezPathV1.jobPath;
import static marquez.client.MarquezPathV1.jobTagPath;
import static marquez.client.MarquezPathV1.jobVersionPath;
import static marquez.client.MarquezPathV1.lineagePath;
import static marquez.client.MarquezPathV1.listDatasetVersionsPath;
import static marquez.client.MarquezPathV1.listDatasetsPath;
import static marquez.client.MarquezPathV1.listJobVersionsPath;
import static marquez.client.MarquezPathV1.listJobsPath;
import static marquez.client.MarquezPathV1.listNamespacesPath;
import static marquez.client.MarquezPathV1.listRunsPath;
import static marquez.client.MarquezPathV1.listSourcesPath;
import static marquez.client.MarquezPathV1.listTagsPath;
import static marquez.client.MarquezPathV1.namespacePath;
import static marquez.client.MarquezPathV1.runPath;
import static marquez.client.MarquezPathV1.runTransitionPath;
import static marquez.client.MarquezPathV1.searchPath;
import static marquez.client.MarquezPathV1.sourcePath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.client.models.NodeId;
import marquez.client.models.RunState;
import marquez.client.models.SearchFilter;
import marquez.client.models.SearchSort;
import org.apache.http.client.utils.URIBuilder;

class MarquezUrl {

  static MarquezUrl create(URL url) {
    return new MarquezUrl(url);
  }

  @VisibleForTesting final URL baseUrl;

  MarquezUrl(final URL baseUrl) {
    this.baseUrl = baseUrl;
  }

  @VisibleForTesting
  URL from(String path) {
    return from(path, ImmutableMap.of());
  }

  @VisibleForTesting
  URL from(String path, @Nullable Map<String, Object> queryParams) {
    try {
      final URIBuilder builder = new URIBuilder(URI.create(baseUrl.toURI() + path));
      if (queryParams != null) {
        queryParams.forEach((name, value) -> builder.addParameter(name, String.valueOf(value)));
      }
      return builder.build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException(
          "can not build url from parameters: " + path + " " + queryParams, e);
    }
  }

  private Map<String, Object> newQueryParamsWith(int limit, int offset) {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    return ImmutableMap.of("limit", limit, "offset", offset);
  }

  URL toListNamespacesUrl(int limit, int offset) {
    return from(listNamespacesPath(), newQueryParamsWith(limit, offset));
  }

  URL toNamespaceUrl(String namespaceName) {
    return from(namespacePath(namespaceName));
  }

  URL toEventUrl(MarquezClient.SortDirection sort, int limit) {
    return toEventUrl(sort, null, null, limit);
  }

  URL toEventUrl(
      @Nullable MarquezClient.SortDirection sort,
      @Nullable ZonedDateTime before,
      @Nullable ZonedDateTime after,
      int limit) {
    Map<String, Object> queryParams = new HashMap<>();

    if (sort != null) {
      queryParams.put("sortDirection", sort.getValue());
    }
    if (before != null) {
      queryParams.put("before", before.toOffsetDateTime().toString());
    }
    if (after != null) {
      queryParams.put("after", after.toOffsetDateTime().toString());
    }
    queryParams.put("limit", limit);
    return from(MarquezPathV1.lineageEventPath(), queryParams);
  }

  URL toSourceUrl(String sourceName) {
    return from(sourcePath(sourceName));
  }

  URL toDatasetUrl(String namespaceName, String datasetName) {
    return from(datasetPath(namespaceName, datasetName));
  }

  URL toListJobsUrl(@NonNull String namespaceName, int limit, int offset) {
    return from(listJobsPath(namespaceName), newQueryParamsWith(limit, offset));
  }

  URL toJobUrl(String namespaceName, String jobName) {
    return from(jobPath(namespaceName, jobName));
  }

  URL toListJobVersionsUrl(@NonNull String namespaceName, String jobName, int limit, int offset) {
    return from(listJobVersionsPath(namespaceName, jobName), newQueryParamsWith(limit, offset));
  }

  URL toJobVersionUrl(String namespaceName, String jobName, String version) {
    return from(jobVersionPath(namespaceName, jobName, version));
  }

  URL toCreateRunUrl(String namespaceName, String jobName) {
    return from(createRunPath(namespaceName, jobName));
  }

  URL toRunUrl(@NonNull String runId) {
    return from(runPath(runId));
  }

  URL toListRunsUrl(@NonNull String namespaceName, @NonNull String jobName, int limit, int offset) {
    return from(listRunsPath(namespaceName, jobName), newQueryParamsWith(limit, offset));
  }

  URL toRunTransitionUrl(String runId, RunState runState, Instant at) {
    return from(
        runTransitionPath(runId, runState),
        at == null ? ImmutableMap.of() : ImmutableMap.of("at", ISO_INSTANT.format(at)));
  }

  URL toListSourcesUrl(int limit, int offset) {
    return from(listSourcesPath(), newQueryParamsWith(limit, offset));
  }

  URL toListDatasetsUrl(@NonNull String namespaceName, int limit, int offset) {
    return from(listDatasetsPath(namespaceName), newQueryParamsWith(limit, offset));
  }

  URL toDatasetVersionUrl(String namespaceName, String datasetName, String version) {
    return from(datasetVersionPath(namespaceName, datasetName, version));
  }

  URL toListDatasetVersionsUrl(
      @NonNull String namespaceName, @NonNull String datasetName, int limit, int offset) {
    return from(
        listDatasetVersionsPath(namespaceName, datasetName), newQueryParamsWith(limit, offset));
  }

  URL toDatasetTagUrl(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String tagName) {
    return from(datasetTagPath(namespaceName, datasetName, tagName));
  }

  URL toJobTagUrl(@NonNull String namespaceName, @NonNull String jobName, @NonNull String tagName) {
    return from(jobTagPath(namespaceName, jobName, tagName));
  }

  URL toFieldTagURL(String namespaceName, String datasetName, String fieldName, String tagName) {
    return from(fieldTagPath(namespaceName, datasetName, fieldName, tagName));
  }

  URL toListTagsUrl(int limit, int offset) {
    return from(listTagsPath(), newQueryParamsWith(limit, offset));
  }

  URL toCreateTagsUrl(String name) {
    return from(createTagPath(name));
  }

  URL toSearchUrl(
      @NonNull String query, @Nullable SearchFilter filter, @Nullable SearchSort sort, int limit) {
    final ImmutableMap.Builder queryParams = new ImmutableMap.Builder();
    queryParams.put("q", query);
    if (filter != null) {
      queryParams.put("filter", filter);
    }
    if (filter != null) {
      queryParams.put("sort", sort);
    }
    queryParams.put("limit", limit);
    return from(searchPath(), queryParams.build());
  }

  URL toLineageUrl(NodeId nodeId, int depth) {
    final ImmutableMap.Builder queryParams = new ImmutableMap.Builder();
    queryParams.put("nodeId", nodeId.getValue());
    queryParams.put("depth", String.valueOf(depth));
    return from(lineagePath(), queryParams.build());
  }

  URL toColumnLineageUrl(NodeId nodeId, int depth, boolean withDownstream) {
    final ImmutableMap.Builder queryParams = new ImmutableMap.Builder();
    queryParams.put("nodeId", nodeId.getValue());
    queryParams.put("depth", String.valueOf(depth));
    queryParams.put("withDownstream", String.valueOf(withDownstream));
    return from(columnLineagePath(), queryParams.build());
  }
}
