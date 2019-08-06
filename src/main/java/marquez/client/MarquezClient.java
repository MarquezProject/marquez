package marquez.client;

import static marquez.client.Preconditions.checkNotBlank;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.client.exceptions.MarquezException;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetMeta;
import marquez.client.models.Datasets;
import marquez.client.models.Datasource;
import marquez.client.models.DatasourceMeta;
import marquez.client.models.Datasources;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobRun;
import marquez.client.models.JobRunMeta;
import marquez.client.models.JobRuns;
import marquez.client.models.Jobs;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Namespaces;

@Slf4j
public class MarquezClient {
  private final MarquezHttp http;
  private final Long timeoutMs;
  /** Namespace name used when a namespace is not specified by client methods. */
  @Getter private final String namespaceName;

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new Jdk8Module())
          .registerModule(new JavaTimeModule())
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  MarquezClient(@NonNull final MarquezHttp http, final Long timeoutMs, final String namespaceName) {
    this.http = http;
    this.timeoutMs = timeoutMs;
    this.namespaceName = checkNotBlank(namespaceName);
  }

  /**
   * Creates and returns a new {@link Namespace} object with the default {@code namespaceName}.
   *
   * @param namespaceMeta metadata for the new {@link Namespace}
   * @return the new {@link Namespace} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespace createNamespace(@NonNull NamespaceMeta namespaceMeta)
      throws IOException, MarquezException, URISyntaxException {
    return createNamespace(namespaceName, namespaceMeta);
  }

  /**
   * Creates and returns a new {@link Namespace} object with the given {@code namespaceName}.
   *
   * @param namespaceName name for the new {@link Namespace} object
   * @param namespaceMeta metadata for the new {@link Namespace}
   * @return the new {@link Namespace} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespace createNamespace(String namespaceName, @NonNull NamespaceMeta namespaceMeta)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final String path = String.format("/namespaces/%s", namespaceName);
    final String payload = MAPPER.writeValueAsString(namespaceMeta);
    final String json = http.put(http.url(path), payload);

    return MAPPER.readValue(json, Namespace.class);
  }

  /**
   * Retrieves the {@link Namespace} object with the default {@code namespaceName}.
   *
   * @return the retrieved {@link Namespace} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespace getNamespace() throws IOException, MarquezException, URISyntaxException {
    return getNamespace(namespaceName);
  }

  /**
   * Retrieves the {@link Namespace} object with the given {@code namespaceName}.
   *
   * @param namespaceName name of the {@link Namespace} object to retrieve
   * @return the retrieved {@link Namespace} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespace getNamespace(String namespaceName)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final String path = String.format("/namespaces/%s", namespaceName);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Namespace.class);
  }

  /**
   * Retrieves a list of all {@link Namespace} objects.
   *
   * @return a {@link Namespaces} object containing {@link Namespace} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespaces listNamespaces() throws IOException, MarquezException, URISyntaxException {
    final String path = "/namespaces";
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Namespaces.class);
  }

  /**
   * Retrieves a list of all {@link Namespace} objects with {@code limit} and {@code offset}.
   *
   * @param limit max number of {@link Namespace} objects in the returned list
   * @param offset number of {@link Namespace} objects to skip before adding {@link Namespace}
   *     objects to the returned list
   * @return a {@link Namespaces} object containing {@link Namespace} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Namespaces listNamespaces(@NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("limit", limit);
    params.put("offset", offset);

    final String path = "/namespaces";
    final String json = http.get(http.url(path, params));

    return MAPPER.readValue(json, Namespaces.class);
  }

  /**
   * Creates and returns a new {@link Job} object with the given metadata in the default namespace.
   *
   * @param jobName name for the new {@link Job} object
   * @param jobMeta metadata for the new {@link Job} object
   * @return the new {@link Job} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Job createJob(String jobName, @NonNull JobMeta jobMeta)
      throws IOException, MarquezException, URISyntaxException {
    return createJob(namespaceName, jobName, jobMeta);
  }

  /**
   * Creates and returns a new {@link Job} object with the given metadata in the given namespace.
   *
   * @param namespaceName namespace for the new {@link Job} object
   * @param jobName name for the new {@link Job} object
   * @param jobMeta metadata for the new {@link Job} object
   * @return the new {@link Job} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Job createJob(String namespaceName, String jobName, @NonNull JobMeta jobMeta)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(jobName);

    final String path = String.format("/namespaces/%s/jobs/%s", namespaceName, jobName);
    final String payload = MAPPER.writeValueAsString(jobMeta);
    final String json = http.put(http.url(path), payload);

    return MAPPER.readValue(json, Job.class);
  }

  /**
   * Retrieves the {@link Job} object with the given name in the default namespace.
   *
   * @param jobName name of the {@link Job} object to retrieve
   * @return the retrieved {@link Job} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Job getJob(String jobName) throws IOException, MarquezException, URISyntaxException {
    return getJob(namespaceName, jobName);
  }

  /**
   * Retrieves the {@link Job} object with the given name in the given namespace.
   *
   * @param namespaceName namespace for the new {@link Job} object
   * @param jobName name of the {@link Job} object to retrieve
   * @return the retrieved {@link Job} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Job getJob(String namespaceName, String jobName)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(jobName);

    final String path = String.format("/namespaces/%s/jobs/%s", namespaceName, jobName);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Job.class);
  }

  /**
   * Retrieves a list of all {@link Job} objects in the default namespace.
   *
   * @return a {@link Jobs} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Jobs listJobs() throws IOException, MarquezException, URISyntaxException {
    return listJobs(namespaceName);
  }

  /**
   * Retrieves a list of {@link Job} objects in the default namespace with {@code limit} and {@code
   * offset}.
   *
   * @param limit max number of {@link Job} objects in the returned list
   * @param offset number of {@link Job} objects to skip before adding {@link Job} objects to the
   *     returned list
   * @return a {@link Jobs} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Jobs listJobs(@NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    return listJobs(namespaceName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link Job} objects in the default namespace.
   *
   * @param namespaceName namespace from which to retrieve {@link Job} objects
   * @return a {@link Jobs} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Jobs listJobs(String namespaceName)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final String path = String.format("/namespaces/%s/jobs", namespaceName);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Jobs.class);
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
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Jobs listJobs(String namespaceName, @NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("limit", limit);
    params.put("offset", offset);

    final String path = String.format("/namespaces/%s/jobs", namespaceName);
    final String json = http.get(http.url(path, params));

    return MAPPER.readValue(json, Jobs.class);
  }

  /**
   * Creates and returns a new {@link JobRun} object with the given metadata for the given job in
   * the default namespace.
   *
   * @param jobName job associated with the new {@link JobRun} object
   * @param jobRunMeta metadata for the new {@link JobRun} object
   * @return the new {@link JobRun} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRun createJobRun(String jobName, @NonNull JobRunMeta jobRunMeta)
      throws IOException, MarquezException, URISyntaxException {
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
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRun createJobRun(String namespaceName, String jobName, @NonNull JobRunMeta jobRunMeta)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(jobName);

    final String path = String.format("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
    final String payload = MAPPER.writeValueAsString(jobRunMeta);
    final String json = http.post(http.url(path), payload);

    return MAPPER.readValue(json, JobRun.class);
  }

  /**
   * Retrieves the {@link JobRun} object with the given {@code runId}.
   *
   * @param runId id for the {@link JobRun} object to retrieve
   * @return the retrieved {@link JobRun} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRun getJobRun(String runId) throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(runId);

    final String path = String.format("/jobs/runs/%s", runId);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, JobRun.class);
  }

  /**
   * Retrieves a list of all {@link JobRun} objects for the given {@link Job} in the default
   * namespace.
   *
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRuns listJobRuns(@NonNull String jobName)
      throws IOException, MarquezException, URISyntaxException {
    return listJobRuns(namespaceName, jobName);
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
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRuns listJobRuns(
      @NonNull String jobName, @NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    return listJobRuns(namespaceName, jobName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link JobRun} objects for the given {@link Job} in the given
   * namespace.
   *
   * @param namespaceName namespace from which to retrieve {@link JobRun} objects
   * @param jobName job for which to retrieve {@link JobRun} objects
   * @return a {@link JobRuns} object containing {@link JobRun} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRuns listJobRuns(String namespaceName, String jobName)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(jobName);

    final String path = String.format("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
    final String json = http.get(http.url(path));

    return new JobRuns(MAPPER.readValue(json, new TypeReference<List<JobRun>>() {}));
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
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public JobRuns listJobRuns(
      String namespaceName, String jobName, @NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(jobName);

    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("limit", limit);
    params.put("offset", offset);

    final String path = String.format("/namespaces/%s/jobs/%s/runs", namespaceName, jobName);
    final String json = http.get(http.url(path, params));

    return new JobRuns(MAPPER.readValue(json, new TypeReference<List<JobRun>>() {}));
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as running.
   *
   * @param runId id for the {@link JobRun} to mark
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public void markJobRunAsRunning(String runId)
      throws IOException, MarquezException, URISyntaxException {
    markJobRunAs(runId, "run");
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as completed.
   *
   * @param runId id for the {@link JobRun} to mark
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public void markJobRunAsCompleted(String runId)
      throws IOException, MarquezException, URISyntaxException {
    markJobRunAs(runId, "complete");
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as failed.
   *
   * @param runId id for the {@link JobRun} to mark
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public void markJobRunAsFailed(String runId)
      throws IOException, MarquezException, URISyntaxException {
    markJobRunAs(runId, "fail");
  }

  /**
   * Sets the {@code runState} of this {@link JobRun} as aborted.
   *
   * @param runId id for the {@link JobRun} to mark
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public void markJobRunAsAborted(String runId)
      throws IOException, MarquezException, URISyntaxException {
    markJobRunAs(runId, "abort");
  }

  private void markJobRunAs(String runId, String runState)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(runId);
    checkNotBlank(runState);

    final String path = String.format("/jobs/runs/%s/%s", runId, runState);
    http.put(http.url(path));
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata.
   *
   * @param datasourceMeta metadata for the new {@link Datasource} object
   * @return the new {@link Datasource} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasource createDatasource(@NonNull DatasourceMeta datasourceMeta)
      throws IOException, MarquezException, URISyntaxException {
    final String path = "/datasources";
    final String payload = MAPPER.writeValueAsString(datasourceMeta);
    final String json = http.post(http.url(path), payload);

    return MAPPER.readValue(json, Datasource.class);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasourceUrn}.
   *
   * @param datasourceUrn urn for the {@link Datasource} object to retrieve
   * @return the retrieved {@link Datasource} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasource getDatasource(String datasourceUrn)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(datasourceUrn);

    final String path = String.format("/datasources/%s", datasourceUrn);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Datasource.class);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects.
   *
   * @return {@link Datasources} object containing {@link Datasource} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasources listDatasources() throws IOException, MarquezException, URISyntaxException {
    final String path = "/datasources";
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Datasources.class);
  }

  /**
   * Retrieves a list of all {@link Datasource} objects with {@code limit} and {@code offset}.
   *
   * @param limit max number of {@link Datasource} objects in the returned list
   * @param offset number of {@link Datasource} objects to skip before adding {@link Datasource}
   *     objects to the returned list
   * @return {@link Datasources} object containing {@link Datasource} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasources listDatasources(@NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("limit", limit);
    params.put("offset", offset);

    final String path = "/datasources";
    final String json = http.get(http.url(path, params));

    return MAPPER.readValue(json, Datasources.class);
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata in the default
   * namespace.
   *
   * @param datasetMeta metadata for the new {@link Dataset} object
   * @return the new {@link Dataset} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Dataset createDataset(@NonNull DatasetMeta datasetMeta)
      throws IOException, MarquezException, URISyntaxException {
    return createDataset(namespaceName, datasetMeta);
  }

  /**
   * Creates and returns a new {@link Datasource} object with the given metadata in the given
   * namespace.
   *
   * @param namespaceName namespace in which to create the new {@link Dataset} object
   * @param datasetMeta metadata for the new {@link Dataset} object
   * @return the new {@link Dataset} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Dataset createDataset(String namespaceName, @NonNull DatasetMeta datasetMeta)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final String path = String.format("/namespaces/%s/datasets", namespaceName);
    final String payload = MAPPER.writeValueAsString(datasetMeta);
    final String json = http.post(http.url(path), payload);

    return MAPPER.readValue(json, Dataset.class);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasetUrn} in the default
   * namespace.
   *
   * @param datasetUrn urn for the {@link Dataset} object to retrieve
   * @return the retrieved {@link Dataset} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Dataset getDataset(String datasetUrn)
      throws IOException, MarquezException, URISyntaxException {
    return getDataset(namespaceName, datasetUrn);
  }

  /**
   * Retrieves the {@link Datasource} object with the given {@code datasetUrn} in the given
   * namespace.
   *
   * @param namespaceName namespace from which to retrieve the {@link Dataset} object
   * @param datasetUrn urn for the {@link Dataset} object to retrieve
   * @return the retrieved {@link Dataset} object
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Dataset getDataset(String namespaceName, String datasetUrn)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);
    checkNotBlank(datasetUrn);

    final String path = String.format("/namespaces/%s/datasets/%s", namespaceName, datasetUrn);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Dataset.class);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the default namespace.
   *
   * @return {@link Datasets} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasets listDatasets() throws IOException, MarquezException, URISyntaxException {
    return listDatasets(namespaceName);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the default namespace with {@code limit} and
   * {@code offset}.
   *
   * @param limit max number of {@link Dataset} objects in the returned list
   * @param offset number of {@link Dataset} objects to skip before adding {@link Dataset} objects
   *     to the returned list
   * @return {@link Datasets} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasets listDatasets(@NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    return listDatasets(namespaceName, limit, offset);
  }

  /**
   * Retrieves a list of all {@link Dataset} objects in the given namespace.
   *
   * @param namespaceName namespace from which to retrieve {@link Dataset} objects
   * @return {@link Datasets} object containing {@link Job} objects
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasets listDatasets(String namespaceName)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final String path = String.format("/namespaces/%s/datasets", namespaceName);
    final String json = http.get(http.url(path));

    return MAPPER.readValue(json, Datasets.class);
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
   * @throws IOException
   * @throws MarquezException
   * @throws URISyntaxException
   */
  public Datasets listDatasets(
      String namespaceName, @NonNull Integer limit, @NonNull Integer offset)
      throws IOException, MarquezException, URISyntaxException {
    checkNotBlank(namespaceName);

    final Map<String, Object> params = new HashMap<String, Object>();
    params.put("limit", limit);
    params.put("offset", offset);

    final String path = String.format("/namespaces/%s/datasets", namespaceName);
    final String json = http.get(http.url(path, params));

    return MAPPER.readValue(json, Datasets.class);
  }

  public static final class Builder {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final long DEFAULT_TIMEOUT_MS = 10000L;
    private static final String DEFAULT_NAMESPACE_NAME = "default";

    private String baseUrl;
    private Long timeoutMs;
    private String namespaceName;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder timeoutMs(Long timeoutMs) {
      this.timeoutMs = timeoutMs;
      return this;
    }

    public Builder namespaceName(String namespaceName) {
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
      if (baseUrl == null) {
        baseUrl = DEFAULT_BASE_URL;
      }

      if (timeoutMs == null) {
        timeoutMs = DEFAULT_TIMEOUT_MS;
      }

      if (namespaceName == null) {
        namespaceName = System.getProperty("MARQUEZ_NAMESPACE", DEFAULT_NAMESPACE_NAME);
      }

      final MarquezHttp http = new MarquezHttp(baseUrl);
      return new MarquezClient(http, timeoutMs, namespaceName);
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
}
