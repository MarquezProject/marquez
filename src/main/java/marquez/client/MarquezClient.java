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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import marquez.client.models.Datasource;
import marquez.client.models.DatasourceMeta;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobRun;
import marquez.client.models.JobRunMeta;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.utils.JsonUtils;

@Slf4j
public class MarquezClient {
  @VisibleForTesting static final int DEFAULT_LIMIT = 100;
  @VisibleForTesting static final int DEFAULT_OFFSET = 0;

  @VisibleForTesting final MarquezHttp http;

  @Getter private final String namespaceName;

  MarquezClient(@NonNull final MarquezHttp http, @NonNull final String namespaceName) {
    this.http = http;
    this.namespaceName = namespaceName;
  }

  /**
   * Creates and returns a new {@link Namespace} object with the given {@code namespaceName}.
   *
   * @param namespaceName name for the new {@link Namespace} object
   * @param namespaceMeta metadata for the new {@link Namespace}
   * @return the new {@link Namespace} object
   */
  public Namespace createNamespace(
      @NonNull String namespaceName, @NonNull NamespaceMeta namespaceMeta) {
    final String bodyAsJson =
        http.put(http.url("/namespaces/%s", namespaceName), namespaceMeta.toJson());
    return Namespace.fromJson(bodyAsJson);
  }

  /**
   * Retrieves the {@link Namespace} object with the default {@code namespaceName}.
   *
   * @return the retrieved {@link Namespace} object
   */
  public Namespace getNamespace() {
    return getNamespace(namespaceName);
  }

  /**
   * Retrieves the {@link Namespace} object with the given {@code namespaceName}.
   *
   * @param namespaceName name of the {@link Namespace} object to retrieve
   * @return the retrieved {@link Namespace} object
   */
  public Namespace getNamespace(@NonNull String namespaceName) {
    final String bodyAsJson = http.get(http.url("/namespaces/%s", namespaceName));
    return Namespace.fromJson(bodyAsJson);
  }

  /**
   * Retrieves a list of all {@link Namespace} objects.
   *
   * @return a {@link Namespaces} object containing {@link Namespace} objects
   */
  public List<Namespace> listNamespaces() {
    return listNamespaces(DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link Namespace} objects with {@code limit} and {@code offset}.
   *
   * @param limit max number of {@link Namespace} objects in the returned list
   * @param offset number of {@link Namespace} objects to skip before adding {@link Namespace}
   *     objects to the returned list
   */
  public List<Namespace> listNamespaces(@NonNull Integer limit, @NonNull Integer offset) {
    final String bodyAsJson = http.get(http.url("/namespaces", newQueryParamsWith(limit, offset)));
    return Namespaces.fromJson(bodyAsJson).getValue();
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata.
   *
   * @param datasourceMeta metadata for the new {@link Datasource} object
   * @return the new {@link Datasource} object
   */
  public Datasource createDatasource(@NonNull DatasourceMeta datasourceMeta) {
    final String bodyAsJson = http.post(http.url("/datasources"), datasourceMeta.toJson());
    return Datasource.fromJson(bodyAsJson);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasourceUrn}.
   *
   * @param datasourceUrn urn for the {@link Datasource} object to retrieve
   * @return the retrieved {@link Datasource} object
   */
  public Datasource getDatasource(@NonNull String datasourceUrn) {
    final String bodyAsJson = http.get(http.url("/datasources/%s", datasourceUrn));
    return Datasource.fromJson(bodyAsJson);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects.
   *
   * @return {@link Datasources} object containing {@link Datasource} objects
   */
  public List<Datasource> listDatasources() {
    return listDatasources(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects.
   *
   * @return {@link Datasources} object containing {@link Datasource} objects
   */
  public List<Datasource> listDatasources(Integer limit, Integer offset) {
    return listDatasources(namespaceName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects.
   *
   * @return {@link Datasources} object containing {@link Datasource} objects
   */
  public List<Datasource> listDatasources(String namespaceName) {
    return listDatasources(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects with {@code limit} and {@code offset}.
   *
   * @param limit max number of {@link Datasource} objects in the returned list
   * @param offset number of {@link Datasource} objects to skip before adding {@link Datasource}
   *     objects to the returned list
   * @return {@link Datasources} object containing {@link Datasource} objects
   */
  public List<Datasource> listDatasources(
      @NonNull String namespaceName, @NonNull Integer limit, @NonNull Integer offset) {
    final String bodyAsJson = http.get(http.url("/datasources", newQueryParamsWith(limit, offset)));
    return Datasources.fromJson(bodyAsJson).getValue();
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata in the default
   * namespace.
   *
   * @param datasetMeta metadata for the new {@link Dataset} object
   * @return the new {@link Dataset} object
   */
  public Dataset createDataset(DatasetMeta datasetMeta) {
    return createDataset(namespaceName, datasetMeta);
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata in the given
   * namespace.
   *
   * @param namespaceName namespace in which to create the new {@link Dataset} object
   * @param datasetMeta metadata for the new {@link Dataset} object
   * @return the new {@link Dataset} object
   */
  public Dataset createDataset(@NonNull String namespaceName, @NonNull DatasetMeta datasetMeta) {
    final String bodyAsJson =
        http.post(http.url("/namespaces/%s/datasets", namespaceName), datasetMeta.toJson());
    return Dataset.fromJson(bodyAsJson);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasetUrn} in the default
   * namespace.
   *
   * @param datasetUrn urn for the {@link Dataset} object to retrieve
   * @return the retrieved {@link Dataset} object
   */
  public Dataset getDataset(String datasetUrn) {
    return getDataset(namespaceName, datasetUrn);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasetUrn} in the given
   * namespace.
   *
   * @param namespaceName namespace from which to retrieve the {@link Dataset} object
   * @param datasetUrn urn for the {@link Dataset} object to retrieve
   * @return the retrieved {@link Dataset} object
   */
  public Dataset getDataset(@NonNull String namespaceName, @NonNull String datasetUrn) {
    final String bodyAsJson =
        http.get(http.url("/namespaces/%s/datasets/%s", namespaceName, datasetUrn));
    return Dataset.fromJson(bodyAsJson);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the default namespace.
   *
   * @return {@link Datasets} object containing {@link Job} objects
   */
  public List<Dataset> listDatasets() {
    return listDatasets(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the default namespace with {@code limit} and
   * {@code offset}.
   *
   * @param limit max number of {@link Dataset} objects in the returned list
   * @param offset number of {@link Dataset} objects to skip before adding {@link Dataset} objects
   *     to the returned list
   * @return {@link Datasets} object containing {@link Job} objects
   */
  public List<Dataset> listDatasets(Integer limit, Integer offset) {
    return listDatasets(namespaceName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the default namespace.
   *
   * @return {@link Datasets} object containing {@link Job} objects
   */
  public List<Dataset> listDatasets(String namespaceName) {
    return listDatasets(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the given namespace with {@code limit} and
   * {@code offset}.
   *
   * @param namespaceName namespace from which to retrieve {@link Dataset} objects
   * @param limit max number of {@link Dataset} objects in the returned list
   * @param offset number of {@link Dataset} objects to skip before adding {@link Dataset} objects
   *     to the returned list
   * @return {@link Datasets} object containing {@link Job} objects
   */
  public List<Dataset> listDatasets(
      @NonNull String namespaceName, @NonNull Integer limit, @NonNull Integer offset) {
    final String bodyAsJson =
        http.get(
            http.url("/namespaces/%s/datasets", newQueryParamsWith(limit, offset), namespaceName));
    return Datasets.fromJson(bodyAsJson).getValue();
  }

  /**
   * Creates and returns a new {@link Job} object with the given metadata in the default namespace.
   *
   * @param jobName name for the new {@link Job} object
   * @param jobMeta metadata for the new {@link Job} object
   * @return the new {@link Job} object
   */
  public Job createJob(String jobName, JobMeta jobMeta) {
    return createJob(namespaceName, jobName, jobMeta);
  }

  /**
   * Creates and returns a new {@link Job} object with the given metadata in the given namespace.
   *
   * @param namespaceName namespace for the new {@link Job} object
   * @param jobName name for the new {@link Job} object
   * @param jobMeta metadata for the new {@link Job} object
   * @return the new {@link Job} object
   */
  public Job createJob(
      @NonNull String namespaceName, @NonNull String jobName, @NonNull JobMeta jobMeta) {
    final String bodyAsJson =
        http.put(http.url("/namespaces/%s/jobs/%s", namespaceName, jobName), jobMeta.toJson());
    return Job.fromJson(bodyAsJson);
  }

  /**
   * Retrieves the {@link Job} object with the given name in the default namespace.
   *
   * @param jobName name of the {@link Job} object to retrieve
   * @return the retrieved {@link Job} object
   */
  public Job getJob(String jobName) {
    return getJob(namespaceName, jobName);
  }

  /**
   * Retrieves the {@link Job} object with the given name in the given namespace.
   *
   * @param namespaceName namespace for the new {@link Job} object
   * @param jobName name of the {@link Job} object to retrieve
   * @return the retrieved {@link Job} object
   */
  public Job getJob(@NonNull String namespaceName, @NonNull String jobName) {
    final String bodyAsJson = http.get(http.url("/namespaces/%s/jobs/%s", namespaceName, jobName));
    return Job.fromJson(bodyAsJson);
  }

  /**
   * Retrieves a list of all {@link Job} objects in the default namespace.
   *
   * @return a {@link Jobs} object containing {@link Job} objects
   */
  public List<Job> listJobs() {
    return listJobs(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of {@link Job} objects in the default namespace with {@code limit} and {@code
   * offset}.
   *
   * @param limit max number of {@link Job} objects in the returned list
   * @param offset number of {@link Job} objects to skip before adding {@link Job} objects to the
   *     returned list
   * @return a {@link Jobs} object containing {@link Job} objects
   */
  public List<Job> listJobs(Integer limit, Integer offset) {
    return listJobs(namespaceName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link Job} objects in the default namespace.
   *
   * @param namespaceName namespace from which to retrieve {@link Job} objects
   * @return a {@link Jobs} object containing {@link Job} objects
   */
  public List<Job> listJobs(String namespaceName) {
    return listJobs(namespaceName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of {@link Job} objects in the given namespace with {@code limit} and {@code
   * offset}.
   *
   * @param namespaceName namespace from which to retrieve {@link Job} objects
   * @param limit max number of {@link Job} objects in the returned list
   * @param offset number of {@link Job} objects to skip before adding {@link Job} objects to the
   *     returned list
   * @return a {@link Jobs} object containing {@link Job} objects
   */
  public List<Job> listJobs(
      @NonNull String namespaceName, @NonNull Integer limit, @NonNull Integer offset) {
    final String bodyAsJson =
        http.get(http.url("/namespaces/%s/jobs", newQueryParamsWith(limit, offset), namespaceName));
    return Jobs.fromJson(bodyAsJson).getValue();
  }

  /**
   * Creates and returns a new {@link JobRun} object with the given metadata for the given job in
   * the default namespace.
   *
   * @param jobName job associated with the new {@link JobRun} object
   * @param jobRunMeta metadata for the new {@link JobRun} object
   * @return the new {@link JobRun} object
   */
  public JobRun createJobRun(String jobName, JobRunMeta jobRunMeta) {
    return createJobRun(namespaceName, jobName, jobRunMeta);
  }

  /**
   * Creates and returns a new {@link JobRun} object with the given metadata for the given job in
   * the given namespace.
   *
   * @param namespaceName namespace for the new {@link JobRun} object
   * @param jobName job associated with the new {@link JobRun} object
   * @param jobRunMeta metadata for the new {@link JobRun} object
   * @return the new {@link JobRun} object
   */
  public JobRun createJobRun(
      @NonNull String namespaceName, @NonNull String jobName, @NonNull JobRunMeta jobRunMeta) {
    final String bodyAsJson =
        http.post(
            http.url("/namespaces/%s/jobs/%s/runs", namespaceName, jobName), jobRunMeta.toJson());
    return JobRun.fromJson(bodyAsJson);
  }

  /**
   * Retrieves the {@link JobRun} object with the given {@code runId}.
   *
   * @param runId id for the {@link JobRun} object to retrieve
   * @return the retrieved {@link JobRun} object
   */
  public JobRun getJobRun(@NonNull String runId) {
    final String bodyAsJson = http.get(http.url("/jobs/runs/%s", runId));
    return JobRun.fromJson(bodyAsJson);
  }

  /**
   * Retrieves a list of all {@link JobRun} objects for the given {@link Job} in the default
   * namespace.
   *
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   */
  public List<JobRun> listJobRuns(String jobName) {
    return listJobRuns(namespaceName, jobName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of {@link JobRun} objects for the given {@link Job} in the default namespace
   * with {@code limit} and {@code offset}.
   *
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @param limit max number of {@link JobRun} objects in the returned list
   * @param offset number of {@link JobRun} objects to skip before adding {@link JobRun} objects to
   *     the returned list
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   */
  public List<JobRun> listJobRuns(String jobName, Integer limit, Integer offset) {
    return listJobRuns(namespaceName, jobName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link JobRun} objects for the given {@link Job} in the default
   * namespace.
   *
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   */
  public List<JobRun> listJobRuns(String namespaceName, String jobName) {
    return listJobRuns(namespaceName, jobName, DEFAULT_LIMIT, DEFAULT_OFFSET);
  }

  /**
   * Retrieves a list of all {@link JobRun} objects for the given {@link Job} in the given namespace
   * with {@code limit} and {@code offset}.
   *
   * @param namespaceName namespace from which to retrieve {@link JobRun} objects
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @param limit max number of {@link JobRun} objects in the returned list
   * @param offset number of {@link JobRun} objects to skip before adding {@link JobRun} objects to
   *     the returned list
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   */
  public List<JobRun> listJobRuns(
      @NonNull String namespaceName,
      @NonNull String jobName,
      @NonNull Integer limit,
      @NonNull Integer offset) {
    final String bodyAsJson =
        http.get(
            http.url(
                "/namespaces/%s/jobs/%s/runs",
                newQueryParamsWith(limit, offset), namespaceName, jobName));
    return JobRuns.fromJson(bodyAsJson).getValue();
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as running.
   *
   * @param runId id for the {@link JobRun} to mark
   */
  public void markJobRunAsRunning(@NonNull String runId) {
    http.put(http.url("/jobs/runs/%s/run", runId));
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as completed.
   *
   * @param runId id for the {@link JobRun} to mark
   */
  public void markJobRunAsCompleted(@NonNull String runId) {
    http.put(http.url("/jobs/runs/%s/complete", runId));
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as aborted.
   *
   * @param runId id for the {@link JobRun} to mark
   */
  public void markJobRunAsAborted(@NonNull String runId) {
    http.put(http.url("/jobs/runs/%s/abort", runId));
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as failed.
   *
   * @param runId id for the {@link JobRun} to mark
   */
  public void markJobRunAsFailed(@NonNull String runId) {
    http.put(http.url("/jobs/runs/%s/fail", runId));
  }

  private Map<String, Object> newQueryParamsWith(Integer limit, Integer offset) {
    return ImmutableMap.of("limit", limit, "offset", offset);
  }

  public static final class Builder {
    @VisibleForTesting static final URL DEFAULT_BASE_URL = toUrl("http://localhost:8080/api/v1");
    @VisibleForTesting static final String DEFAULT_NAMESPACE_NAME = "default";
    @VisibleForTesting static final String NAMESPACE_NAME_ENV_VAR = "MARQUEZ_NAMESPACE";

    @VisibleForTesting URL baseUrl;

    private String namespaceName;

    private Builder() {
      this.baseUrl = DEFAULT_BASE_URL;
      this.namespaceName = System.getProperty(NAMESPACE_NAME_ENV_VAR, DEFAULT_NAMESPACE_NAME);
    }

    public Builder baseUrl(@NonNull String baseUrl) {
      return baseUrl(toUrl(baseUrl));
    }

    public Builder baseUrl(@NonNull URL baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder namespaceName(@NonNull String namespaceName) {
      this.namespaceName = namespaceName;
      return this;
    }

    /**
     * Builds a {@link MarquezClient} object with the given parameters.
     *
     * <p>If namespaceName is not set, then the builder defaults to the environment variable
     * "MARQUEZ_NAMESPACE" (if set) or "default" otherwise. If baseUrl is not set, then the builder
     * defaults to http://localhost:8080. If timeoutMs is not set, then the builder defaults to
     * 10000L.
     *
     * @return an instance of {@link MarquezClient} with the specified parameters
     */
    public MarquezClient build() {
      return new MarquezClient(
          MarquezHttp.create(baseUrl, MarquezClient.Version.get()), namespaceName);
    }

    private static URL toUrl(final String url) {
      try {
        return new URL(url);
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException("Malformed URL: " + url);
      }
    }
  }

  /**
   * Returns a builder for {@link MarquezClient}. This is the recommended way to create a new {@link
   * MarquezClient} object.
   *
   * @return a builder for {@link MarquezClient}
   */
  public static Builder builder() {
    return new Builder();
  }

  @Value
  static class Version {
    private static final String POM_PROPERTIES =
        "/META-INF/maven/io.github.marquezproject/marquez-java/pom.properties";

    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String VERSION_UNKNOWN = "unknown";

    @Getter String value;

    private Version(@NonNull final String value) {
      this.value = value;
    }

    static Version get() {
      final Properties properties = new Properties();
      try (final InputStream stream =
          MarquezClient.class.getClassLoader().getResourceAsStream(POM_PROPERTIES)) {
        if (stream != null) {
          properties.load(stream);
          return new Version(properties.getProperty(VERSION_PROPERTY_NAME, VERSION_UNKNOWN));
        }
      } catch (IOException e) {
        log.warn("Failed to load properties file: {}", POM_PROPERTIES, e);
      }
      return NO_VERSION;
    }

    public static Version NO_VERSION = new Version(VERSION_UNKNOWN);
  }

  @Value
  static final class Namespaces {
    @Getter List<Namespace> value;

    @JsonCreator
    Namespaces(@JsonProperty("namespaces") final List<Namespace> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Namespaces fromJson(final String json) {
      return JsonUtils.fromJson(json, new TypeReference<Namespaces>() {});
    }
  }

  @Value
  static final class Datasources {
    @Getter List<Datasource> value;

    @JsonCreator
    Datasources(@JsonProperty("datasources") final List<Datasource> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Datasources fromJson(final String json) {
      return JsonUtils.fromJson(json, new TypeReference<Datasources>() {});
    }
  }

  @Value
  static final class Datasets {
    @Getter List<Dataset> value;

    @JsonCreator
    Datasets(@JsonProperty("datasets") final List<Dataset> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Datasets fromJson(final String json) {
      return JsonUtils.fromJson(json, new TypeReference<Datasets>() {});
    }
  }

  @Value
  static final class Jobs {
    @Getter List<Job> value;

    @JsonCreator
    Jobs(@JsonProperty("jobs") final List<Job> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static Jobs fromJson(final String json) {
      return JsonUtils.fromJson(json, new TypeReference<Jobs>() {});
    }
  }

  @Value
  static final class JobRuns {
    @Getter List<JobRun> value;

    @JsonCreator
    JobRuns(@JsonProperty("jobs") final List<JobRun> value) {
      this.value = ImmutableList.copyOf(value);
    }

    static JobRuns fromJson(final String json) {
      return JsonUtils.fromJson(json, new TypeReference<JobRuns>() {});
    }
  }
}
