/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.client;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetMeta;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;

@Slf4j
public class MarquezClient {
  @VisibleForTesting
  static final URL DEFAULT_BASE_URL = Utils.toUrl("http://localhost:8080/api/v1");

  @VisibleForTesting static final String DEFAULT_NAMESPACE_NAME = "default";

  @VisibleForTesting static final int DEFAULT_LIMIT = 100;
  @VisibleForTesting static final int DEFAULT_OFFSET = 0;

  @VisibleForTesting final MarquezHttp http;
  @Getter private final String namespaceName;

  public MarquezClient() {
    this(DEFAULT_BASE_URL, DEFAULT_NAMESPACE_NAME);
  }

  public MarquezClient(final String namespaceName) {
    this(DEFAULT_BASE_URL, namespaceName);
  }

  public MarquezClient(final String baseUrlString, final String namespaceName) {
    this(Utils.toUrl(baseUrlString), namespaceName);
  }

  public MarquezClient(@NonNull final URL baseUrl, final String namespaceName) {
    this(MarquezHttp.create(baseUrl, MarquezClient.Version.get()), namespaceName);
  }

  MarquezClient(@NonNull final MarquezHttp http, @NonNull final String namespaceName) {
    this.http = http;
    this.namespaceName = namespaceName;
  }

  public Namespace createNamespace(@NonNull String namespaceName, @NonNull NamespaceMeta meta) {
    final String bodyAsJson = http.put(http.url("/namespaces/%s", namespaceName), meta.toJson());
    return Namespace.fromJson(bodyAsJson);
  }

  public Namespace getNamespace() {
    return getNamespace(namespaceName);
  }

  public Namespace getNamespace(@NonNull String namespaceName) {
    final String bodyAsJson = http.get(http.url("/namespaces/%s", namespaceName));
    return Namespace.fromJson(bodyAsJson);
  }

  public List<Namespace> listNamespaces() {
    return listNamespaces(DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Namespace> listNamespaces(Integer limit, Integer offset) {
    final String bodyAsJson = http.get(http.url("/namespaces", newQueryParamsWith(limit, offset)));
    return Namespaces.fromJson(bodyAsJson).getValue();
  }

  public Source createSource(@NonNull String sourceName, @NonNull SourceMeta meta) {
    final String bodyAsJson = http.put(http.url("/sources/%s", sourceName), meta.toJson());
    return Source.fromJson(bodyAsJson);
  }

  public Source getSource(@NonNull String sourceName) {
    final String bodyAsJson = http.get(http.url("/sources/%s", sourceName));
    return Source.fromJson(bodyAsJson);
  }

  public List<Source> listSources() {
    return listSources(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Source> listSources(Integer limit, Integer offset) {
    return listSources(namespaceName, limit, offset);
  }

  public List<Source> listSources(String namespaceName) {
    return listSources(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Source> listSources(@NonNull String namespaceName, Integer limit, Integer offset) {
    final String bodyAsJson = http.get(http.url("/sources", newQueryParamsWith(limit, offset)));
    return Sources.fromJson(bodyAsJson).getValue();
  }

  public Dataset createDataset(String datasetName, DatasetMeta meta) {
    return createDataset(namespaceName, datasetName, meta);
  }

  public Dataset createDataset(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull DatasetMeta meta) {
    final String bodyAsJson =
        http.put(http.url("/namespaces/%s/datasets/%s", namespaceName, datasetName), meta.toJson());
    return Dataset.fromJson(bodyAsJson);
  }

  public Dataset getDataset(String datasetName) {
    return getDataset(namespaceName, datasetName);
  }

  public Dataset getDataset(@NonNull String namespaceName, @NonNull String datasetName) {
    final String bodyAsJson =
        http.get(http.url("/namespaces/%s/datasets/%s", namespaceName, datasetName));
    return Dataset.fromJson(bodyAsJson);
  }

  public List<Dataset> listDatasets() {
    return listDatasets(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Dataset> listDatasets(Integer limit, Integer offset) {
    return listDatasets(namespaceName, limit, offset);
  }

  public List<Dataset> listDatasets(String namespaceName) {
    return listDatasets(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Dataset> listDatasets(@NonNull String namespaceName, Integer limit, Integer offset) {
    final String bodyAsJson =
        http.get(
            http.url("/namespaces/%s/datasets", newQueryParamsWith(limit, offset), namespaceName));
    return Datasets.fromJson(bodyAsJson).getValue();
  }

  public Dataset tagDataset(
      @NonNull String namespaceName, @NonNull String datasetName, @NonNull String tagName) {
    final String bodyAsJson =
        http.post(
            http.url("/namespaces/%s/datasets/%s/tags/%s", namespaceName, datasetName, tagName));
    return Dataset.fromJson(bodyAsJson);
  }

  public Dataset tagDatasetField(
      @NonNull String namespaceName,
      @NonNull String datasetName,
      @NonNull String fieldName,
      @NonNull String tagName) {
    final String bodyAsJson =
        http.post(
            http.url(
                "/namespaces/%s/datasets/%s/fields/%s/tags/%s",
                namespaceName, datasetName, fieldName, tagName));
    return Dataset.fromJson(bodyAsJson);
  }

  public Job createJob(String jobName, JobMeta meta) {
    return createJob(namespaceName, jobName, meta);
  }

  public Job createJob(
      @NonNull String namespaceName, @NonNull String jobName, @NonNull JobMeta meta) {
    final String bodyAsJson =
        http.put(http.url("/namespaces/%s/jobs/%s", namespaceName, jobName), meta.toJson());
    return Job.fromJson(bodyAsJson);
  }

  public Job getJob(String jobName) {
    return getJob(namespaceName, jobName);
  }

  public Job getJob(@NonNull String namespaceName, @NonNull String jobName) {
    final String bodyAsJson = http.get(http.url("/namespaces/%s/jobs/%s", namespaceName, jobName));
    return Job.fromJson(bodyAsJson);
  }

  public List<Job> listJobs() {
    return listJobs(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Job> listJobs(Integer limit, Integer offset) {
    return listJobs(namespaceName, limit, offset);
  }

  public List<Job> listJobs(String namespaceName) {
    return listJobs(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Job> listJobs(@NonNull String namespaceName, Integer limit, Integer offset) {
    final String bodyAsJson =
        http.get(http.url("/namespaces/%s/jobs", newQueryParamsWith(limit, offset), namespaceName));
    return Jobs.fromJson(bodyAsJson).getValue();
  }

  public Run createRun(String jobName, RunMeta meta) {
    return createRun(namespaceName, jobName, meta);
  }

  public Run createRun(
      @NonNull String namespaceName, @NonNull String jobName, @NonNull RunMeta meta) {
    final String bodyAsJson =
        http.post(http.url("/namespaces/%s/jobs/%s/runs", namespaceName, jobName), meta.toJson());
    return Run.fromJson(bodyAsJson);
  }

  public Run getRun(@NonNull String runId) {
    final String bodyAsJson = http.get(http.url("/jobs/runs/%s", runId));
    return Run.fromJson(bodyAsJson);
  }

  public List<Run> listRuns(String jobName) {
    return listRuns(namespaceName, jobName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Run> listRuns(String jobName, Integer limit, Integer offset) {
    return listRuns(namespaceName, jobName, limit, offset);
  }

  public List<Run> listRuns(String namespaceName, String jobName) {
    return listRuns(namespaceName, jobName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  public List<Run> listRuns(
      @NonNull String namespaceName, @NonNull String jobName, Integer limit, Integer offset) {
    final String bodyAsJson =
        http.get(
            http.url(
                "/namespaces/%s/jobs/%s/runs",
                newQueryParamsWith(limit, offset), namespaceName, jobName));
    return Runs.fromJson(bodyAsJson).getValue();
  }

  public Run markRunAsRunning(String runId) {
    return markRunWith("/jobs/runs/%s/start", runId);
  }

  public Run markRunAsCompleted(String runId) {
    return markRunWith("/jobs/runs/%s/complete", runId);
  }

  public Run markRunAsAborted(String runId) {
    return markRunWith("/jobs/runs/%s/abort", runId);
  }

  public Run markRunAsFailed(String runId) {
    return markRunWith("/jobs/runs/%s/fail", runId);
  }

  private Run markRunWith(String pathTemplate, @NonNull String runId) {
    final String bodyAsJson = http.post(http.url(pathTemplate, runId));
    return Run.fromJson(bodyAsJson);
  }

  private Map<String, Object> newQueryParamsWith(@NonNull Integer limit, @NonNull Integer offset) {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    return ImmutableMap.of("limit", limit, "offset", offset);
  }

  public static final class Builder {
    @VisibleForTesting static final String NAMESPACE_NAME_ENV_VAR = "MARQUEZ_NAMESPACE";

    @VisibleForTesting URL baseUrl;
    private String namespaceName;

    private Builder() {
      this.baseUrl = DEFAULT_BASE_URL;
      this.namespaceName = System.getProperty(NAMESPACE_NAME_ENV_VAR, DEFAULT_NAMESPACE_NAME);
    }

    public Builder baseUrl(@NonNull String baseUrl) {
      return baseUrl(Utils.toUrl(baseUrl));
    }

    public Builder baseUrl(@NonNull URL baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder namespaceName(@NonNull String namespaceName) {
      this.namespaceName = namespaceName;
      return this;
    }

    public MarquezClient build() {
      return new MarquezClient(
          MarquezHttp.create(baseUrl, MarquezClient.Version.get()), namespaceName);
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

  @Value
  static class Datasets {
    @Getter List<Dataset> value;

    @JsonCreator
    Datasets(@JsonProperty("datasets") final List<Dataset> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Datasets fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<Datasets>() {});
    }
  }

  @Value
  static class Jobs {
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
}
