/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static marquez.client.models.RunState.ABORTED;
import static marquez.client.models.RunState.COMPLETED;
import static marquez.client.models.RunState.FAILED;
import static marquez.client.models.RunState.RUNNING;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetMeta;
import marquez.client.models.DatasetVersion;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobVersion;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SearchFilter;
import marquez.client.models.SearchResults;
import marquez.client.models.SearchSort;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import marquez.client.models.Tag;

@Slf4j
public class MarquezClient {

  static final URL DEFAULT_BASE_URL = Utils.toUrl("http://localhost:8080");

  @VisibleForTesting static final int DEFAULT_LIMIT = 100;
  @VisibleForTesting static final int DEFAULT_OFFSET = 0;

  @VisibleForTesting final MarquezUrl url;
  @VisibleForTesting final MarquezHttp http;

  public MarquezClient() {
    this(DEFAULT_BASE_URL, null);
  }

  public MarquezClient(final String baseUrlString) {
    this(baseUrlString, null);
  }

  public MarquezClient(final String baseUrlString, @Nullable final String apiKey) {
    this(Utils.toUrl(baseUrlString), apiKey);
  }

  public MarquezClient(final URL baseUrl) {
    this(baseUrl, null);
  }

  public MarquezClient(final URL baseUrl, @Nullable final String apiKey) {
    this(MarquezUrl.create(baseUrl), MarquezHttp.create(MarquezClient.Version.get(), apiKey));
  }

  @VisibleForTesting
  MarquezClient(@NonNull final MarquezUrl url, @NonNull final MarquezHttp http) {
    this.url = url;
    this.http = http;
  }

  public Namespace createNamespace(
      @NonNull String namespaceName, @NonNull NamespaceMeta namespaceMeta) {
    final String bodyAsJson = http.put(url.toNamespaceUrl(namespaceName), namespaceMeta.toJson());
    return Namespace.fromJson(bodyAsJson);
  }

  public Namespace getNamespace(@NonNull String namespaceName) {
    final String bodyAsJson = http.get(url.toNamespaceUrl(namespaceName));
    return Namespace.fromJson(bodyAsJson);
  }

  public List<Namespace> listNamespaces() {
    return listNamespaces(DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Namespace> listNamespaces(int limit, int offset) {
    final String bodyAsJson = http.get(url.toListNamespacesUrl(limit, offset));
    return Namespaces.fromJson(bodyAsJson).getValue();
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Source createSource(@NonNull String sourceName, @NonNull SourceMeta sourceMeta) {
    final String bodyAsJson = http.put(url.toSourceUrl(sourceName), sourceMeta.toJson());
    return Source.fromJson(bodyAsJson);
  }

  public Source getSource(@NonNull String sourceName) {
    final String bodyAsJson = http.get(url.toSourceUrl(sourceName));
    return Source.fromJson(bodyAsJson);
  }

  public List<Source> listSources() {
    return listSources(DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Source> listSources(int limit, int offset) {
    final String bodyAsJson = http.get(url.toListSourcesUrl(limit, offset));
    return Sources.fromJson(bodyAsJson).getValue();
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Dataset createDataset(
      @NonNull String namespaceName,
      @NonNull String datasetName,
      @NonNull DatasetMeta datasetMeta) {
    final String bodyAsJson =
        http.put(url.toDatasetUrl(namespaceName, datasetName), datasetMeta.toJson());
    return Dataset.fromJson(bodyAsJson);
  }

  public Dataset getDataset(@NonNull String namespaceName, @NonNull String datasetName) {
    final String bodyAsJson = http.get(url.toDatasetUrl(namespaceName, datasetName));
    return Dataset.fromJson(bodyAsJson);
  }

  public DatasetVersion getDatasetVersion(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String version) {
    final String bodyAsJson =
        http.get(url.toDatasetVersionUrl(namespaceName, datasetName, version));
    return DatasetVersion.fromJson(bodyAsJson);
  }

  public List<DatasetVersion> listDatasetVersions(
      @NonNull String namespaceName, @NonNull String datasetName) {
    return listDatasetVersions(namespaceName, datasetName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<DatasetVersion> listDatasetVersions(
      @NonNull String namespaceName, @NonNull String datasetName, int limit, int offset) {
    final String bodyAsJson =
        http.get(url.toListDatasetVersionsUrl(namespaceName, datasetName, limit, offset));
    return DatasetVersions.fromJson(bodyAsJson).getValue();
  }

  public List<Dataset> listDatasets(String namespaceName) {
    return listDatasets(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Dataset> listDatasets(@NonNull String namespaceName, int limit, int offset) {
    final String bodyAsJson = http.get(url.toListDatasetsUrl(namespaceName, limit, offset));
    return Datasets.fromJson(bodyAsJson).getValue();
  }

  public Dataset tagDatasetWith(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String tagName) {
    final String bodyAsJson = http.post(url.toDatasetTagUrl(namespaceName, datasetName, tagName));
    return Dataset.fromJson(bodyAsJson);
  }

  public Dataset tagFieldWith(
      @NonNull String namespaceName,
      @NonNull String datasetName,
      @NonNull String fieldName,
      @NonNull String tagName) {
    final String bodyAsJson =
        http.post(url.toFieldTagURL(namespaceName, datasetName, fieldName, tagName));
    return Dataset.fromJson(bodyAsJson);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Job createJob(
      @NonNull String namespaceName, @NonNull String jobName, @NonNull JobMeta jobMeta) {
    final String bodyAsJson = http.put(url.toJobUrl(namespaceName, jobName), jobMeta.toJson());
    return Job.fromJson(bodyAsJson);
  }

  public Job getJob(@NonNull String namespaceName, @NonNull String jobName) {
    final String bodyAsJson = http.get(url.toJobUrl(namespaceName, jobName));
    return Job.fromJson(bodyAsJson);
  }

  public JobVersion getJobVersion(
      @NonNull String namespaceName, @NonNull String jobName, String version) {
    final String bodyAsJson = http.get(url.toJobVersionUrl(namespaceName, jobName, version));
    return JobVersion.fromJson(bodyAsJson);
  }

  public List<Job> listJobs(String namespaceName) {
    return listJobs(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Job> listJobs(@NonNull String namespaceName, int limit, int offset) {
    final String bodyAsJson = http.get(url.toListJobsUrl(namespaceName, limit, offset));
    return Jobs.fromJson(bodyAsJson).getValue();
  }

  public List<JobVersion> listJobVersions(
      @NonNull String namespaceName, String jobName, int limit, int offset) {
    final String bodyAsJson =
        http.get(url.toListJobVersionsUrl(namespaceName, jobName, limit, offset));
    return JobVersions.fromJson(bodyAsJson).getValue();
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run createRun(String namespaceName, String jobName, RunMeta runMeta) {
    return createRun(namespaceName, jobName, runMeta, false);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  private Run createRun(
      @NonNull String namespaceName,
      @NonNull String jobName,
      @NonNull RunMeta runMeta,
      boolean markRunAsRunning) {
    final String bodyAsJson =
        http.post(url.toCreateRunUrl(namespaceName, jobName), runMeta.toJson());
    final Run run = Run.fromJson(bodyAsJson);
    return (markRunAsRunning) ? markRunAsRunning(run.getId()) : run;
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run createRunAndStart(String namespaceName, String jobName, RunMeta runMeta) {
    return createRun(namespaceName, jobName, runMeta, true);
  }

  public Run getRun(@NonNull String runId) {
    final String bodyAsJson = http.get(url.toRunUrl(runId));
    return Run.fromJson(bodyAsJson);
  }

  public List<Run> listRuns(String namespaceName, String jobName) {
    return listRuns(namespaceName, jobName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Run> listRuns(
      @NonNull String namespaceName, @NonNull String jobName, int limit, int offset) {
    final String bodyAsJson = http.get(url.toListRunsUrl(namespaceName, jobName, limit, offset));
    return Runs.fromJson(bodyAsJson).getValue();
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAs(String runId, RunState runState) {
    return markRunAs(runId, runState, null);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAs(String runId, @NonNull RunState runState, @Nullable Instant at) {
    final String bodyAsJson = http.post(url.toRunTransitionUrl(runId, runState, at));
    return Run.fromJson(bodyAsJson);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsRunning(String runId) {
    return markRunAsRunning(runId, null);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsRunning(String runId, @Nullable Instant at) {
    return markRunAs(runId, RUNNING, at);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsCompleted(String runId) {
    return markRunAsCompleted(runId, null);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsCompleted(String runId, @Nullable Instant at) {
    return markRunAs(runId, COMPLETED, at);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsAborted(String runId) {
    return markRunAsAborted(runId, null);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsAborted(String runId, @Nullable Instant at) {
    return markRunAs(runId, ABORTED, at);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsFailed(String runId) {
    return markRunAsFailed(runId, null);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run markRunAsFailed(String runId, @Nullable Instant at) {
    return markRunAs(runId, FAILED, at);
  }

  public Set<Tag> listTags() {
    return listTags(DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public Set<Tag> listTags(int limit, int offset) {
    final String bodyAsJson = http.get(url.toListTagsUrl(limit, offset));
    return Tags.fromJson(bodyAsJson).getValue();
  }

  public Tag createTag(String name, String description) {
    String tagAsJson =
        http.put(url.toCreateTagsUrl(name), new TagDescription(description).toJson());
    return Tag.fromJson(tagAsJson);
  }

  public Tag createTag(String tag) {
    return createTag(tag, null);
  }

  public SearchResults search(String query) {
    return search(query, null, null, DEFAULT_LIMIT);
  }

  public SearchResults search(@NonNull String query, @NonNull SearchFilter filter) {
    return search(query, filter, null, DEFAULT_LIMIT);
  }

  public SearchResults search(@NonNull String query, @NonNull SearchSort sort) {
    return search(query, null, sort, DEFAULT_LIMIT);
  }

  public SearchResults search(@NonNull String query, int limit) {
    return search(query, null, null, limit);
  }

  public SearchResults search(
      String query, @Nullable SearchFilter filter, @Nullable SearchSort sort, int limit) {
    final String bodyAsJson = http.get(url.toSearchUrl(query, filter, sort, limit));
    return SearchResults.fromJson(bodyAsJson);
  }

  public static final class Builder {
    @VisibleForTesting URL baseUrl;
    @VisibleForTesting @Nullable String apiKey;
    @VisibleForTesting @Nullable SSLContext sslContext;

    private Builder() {
      this.baseUrl = DEFAULT_BASE_URL;
    }

    public Builder baseUrl(@NonNull String baseUrlString) {
      return baseUrl(Utils.toUrl(baseUrlString));
    }

    public Builder baseUrl(@NonNull URL baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder apiKey(@Nullable String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder sslContext(@Nullable SSLContext sslContext) {
      this.sslContext = sslContext;
      return this;
    }

    public MarquezClient build() {
      return new MarquezClient(
          MarquezUrl.create(baseUrl),
          MarquezHttp.create(sslContext, MarquezClient.Version.get(), apiKey));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Value
  static class Version {
    private static final String CONFIG_PROPERTIES = "config.properties";

    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String VERSION_UNKNOWN = "unknown";

    @Getter String value;

    private Version(@NonNull final String value) {
      this.value = value;
    }

    static Version get() {
      final Properties properties = new Properties();
      try (final InputStream stream =
          MarquezClient.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
        properties.load(stream);
        return new Version(properties.getProperty(VERSION_PROPERTY_NAME, VERSION_UNKNOWN));
      } catch (IOException e) {
        log.warn("Failed to load properties file: {}", CONFIG_PROPERTIES, e);
      }
      return NO_VERSION;
    }

    public static Version NO_VERSION = new Version(VERSION_UNKNOWN);
  }

  @Value
  static class Namespaces {
    @Getter List<Namespace> value;

    @JsonCreator
    Namespaces(@JsonProperty("namespaces") final List<Namespace> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Namespaces fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Namespaces>() {});
    }
  }

  @Value
  static class Sources {
    @Getter List<Source> value;

    @JsonCreator
    Sources(@JsonProperty("sources") final List<Source> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Sources fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Sources>() {});
    }
  }

  @NoArgsConstructor
  static class ResultsPage {
    @JsonProperty @Setter @Getter int totalCount;
  }

  @Value
  @EqualsAndHashCode(callSuper = false)
  public static class Datasets extends ResultsPage {
    private final @Getter List<Dataset> value;

    @JsonCreator
    Datasets(@JsonProperty("datasets") final List<Dataset> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Datasets fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Datasets>() {});
    }
  }

  @Value
  static class DatasetVersions {
    @Getter List<DatasetVersion> value;

    @JsonCreator
    DatasetVersions(@JsonProperty("versions") final List<DatasetVersion> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static DatasetVersions fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<DatasetVersions>() {});
    }
  }

  @Value
  @EqualsAndHashCode(callSuper = false)
  static class Jobs extends ResultsPage {
    @Getter List<Job> value;

    @JsonCreator
    Jobs(@JsonProperty("jobs") final List<Job> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Jobs fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Jobs>() {});
    }
  }

  @Value
  static class JobVersions {
    @Getter List<JobVersion> value;

    @JsonCreator
    JobVersions(@JsonProperty("versions") final List<JobVersion> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static JobVersions fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<JobVersions>() {});
    }
  }

  @Value
  static class Runs {
    @Getter List<Run> value;

    @JsonCreator
    Runs(@JsonProperty("runs") final List<Run> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Runs fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Runs>() {});
    }
  }

  @Value
  static class Tags {
    @Getter Set<Tag> value;

    @JsonCreator
    Tags(@JsonProperty("tags") final Set<Tag> value) {
      this.value = ImmutableSet.copyOf(value);
    }

    static Tags fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Tags>() {});
    }
  }

  @Value
  static class TagDescription {
    @Getter
    @JsonProperty("description")
    String value;

    String toJson() {
      return Utils.toJson(this);
    }
  }
}
