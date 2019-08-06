package marquez.client;

import static marquez.client.JsonGenerator.newJsonFor;
import static marquez.client.models.ModelGenerator.newConnectionUrl;
import static marquez.client.models.ModelGenerator.newDatasetName;
import static marquez.client.models.ModelGenerator.newDatasetUrn;
import static marquez.client.models.ModelGenerator.newDatasetUrns;
import static marquez.client.models.ModelGenerator.newDatasets;
import static marquez.client.models.ModelGenerator.newDatasourceName;
import static marquez.client.models.ModelGenerator.newDatasourceUrn;
import static marquez.client.models.ModelGenerator.newDatasources;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newJobName;
import static marquez.client.models.ModelGenerator.newJobRuns;
import static marquez.client.models.ModelGenerator.newJobs;
import static marquez.client.models.ModelGenerator.newLocation;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static marquez.client.models.ModelGenerator.newNamespaces;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static marquez.client.models.ModelGenerator.newRunArgs;
import static marquez.client.models.ModelGenerator.newRunId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import marquez.client.models.RunState;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class MarquezClientTest {
  private MarquezHttp mockMarquezHttp = mock(MarquezHttp.class);
  private final MarquezClient marquezClient = new MarquezClient(mockMarquezHttp, 10000L, "default");

  private static final String DEFAULT_NAMESPACE_NAME = "default";
  private static final String NAMESPACE_NAME = newNamespaceName();
  private static final Instant CREATED_AT = Instant.now();
  private static final String OWNER_NAME = newOwnerName();
  private static final String DESCRIPTION = newDescription();

  private static final String JOB_NAME = newJobName();
  private static final Instant UPDATED_AT = Instant.from(CREATED_AT);
  private static final List<String> INPUT_DATASET_URNS = newDatasetUrns(3);
  private static final List<String> OUTPUT_DATASET_URNS = newDatasetUrns(4);
  private static final String LOCATION = newLocation();

  private static final String RUN_ID = newRunId();
  private static final Instant NOMINAL_START_TIME = Instant.now();
  private static final Instant NOMINAL_END_TIME = Instant.now();
  private static final String RUN_ARGS = newRunArgs();
  private static final RunState RUN_STATE = RunState.NEW;

  private static final String DATASOURCE_NAME = newDatasourceName();
  private static final String URN = newDatasetUrn();
  private static final String CONNECTION_URL = newConnectionUrl();

  private static final String DATASET_NAME = newDatasetName();
  private static final String DATASOURCE_URN = newDatasourceUrn();

  private static final Integer DEFAULT_LIMIT = 2;
  private static final Integer DEFAULT_OFFSET = 0;

  private URL getUrl(String path) throws MalformedURLException {
    return getUrl(path, "");
  }

  private URL getUrl(String path, String queryParams) throws MalformedURLException {
    return new URL(String.format("http://localhost:5000/api/v1%s%s", path, queryParams));
  }

  private Map<String, Object> getQueryParams(int limit, int offset) {
    Map<String, Object> queryParams = new HashMap<String, Object>();
    queryParams.put("limit", limit);
    queryParams.put("offset", offset);
    return queryParams;
  }

  private String getQueryParamsPath(int limit, int offset) {
    return String.format("?limit=%s&offset=%s", limit, offset);
  }

  @Test
  public void testNewMarquezClient_default() {
    MarquezClient mc = MarquezClient.builder().build();

    assertThat(mc.getNamespaceName()).isEqualTo("default");
  }

  @Test
  public void testNewMarquezClient_envVars() {
    System.setProperty("MARQUEZ_NAMESPACE", "temp");

    MarquezClient mc = MarquezClient.builder().build();
    assertThat(mc.getNamespaceName()).isEqualTo("temp");

    System.clearProperty("MARQUEZ_NAMESPACE");
  }

  @Test
  public void testNewMarquezClient_builder() {
    MarquezClient mc =
        MarquezClient.builder()
            .baseUrl("http://marquez.staging:4000")
            .timeoutMs(2000L)
            .namespaceName("wework")
            .build();

    assertThat(mc.getNamespaceName()).isEqualTo("wework");
  }

  @Test
  public void testNewMarquezClient_builder_nullNamespace() {
    MarquezClient mc = MarquezClient.builder().namespaceName(null).build();

    assertThat(mc.getNamespaceName()).isEqualTo("default");
  }

  @Test
  public void testNewMarquezClient_builder_blankNamespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MarquezClient.builder().namespaceName(" ").build());
  }

  @Test
  public void testCreateNamespace_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final NamespaceMeta namespaceMeta = NamespaceMeta.builder().ownerName(OWNER_NAME).build();
    final Namespace namespace =
        new Namespace(DEFAULT_NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

    final String namespaceMetaJson = newJsonFor(namespaceMeta);
    final String namespaceJson = JsonGenerator.newJsonFor(namespace);

    final String urlPath = String.format("/namespaces/%s", DEFAULT_NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.put(url, namespaceMetaJson)).thenReturn(namespaceJson);

    final Namespace returnNamespace = marquezClient.createNamespace(namespaceMeta);
    assertThat(returnNamespace).isEqualTo(namespace);
  }

  @Test
  public void testCreateNamespace_nullNamespaceMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createNamespace(null));
  }

  @Test
  public void testCreateNamespace() throws IOException, MarquezException, URISyntaxException {
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(DESCRIPTION).build();
    final Namespace namespace = new Namespace(NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

    final String namespaceMetaJson = newJsonFor(namespaceMeta);
    final String namespaceJson = JsonGenerator.newJsonFor(namespace);

    final String urlPath = String.format("/namespaces/%s", NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.put(url, namespaceMetaJson)).thenReturn(namespaceJson);

    final Namespace returnNamespace = marquezClient.createNamespace(NAMESPACE_NAME, namespaceMeta);
    assertThat(returnNamespace).isEqualTo(namespace);
  }

  @Test
  public void testCreateNamespace_defaultNamespaceName_nullNamespaceName() {
    final NamespaceMeta namespaceMeta = NamespaceMeta.builder().ownerName(OWNER_NAME).build();
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createNamespace(null, namespaceMeta));
  }

  @Test
  public void testCreateNamespace_defaultNamespaceName_blankNamespaceName() {
    final NamespaceMeta namespaceMeta = NamespaceMeta.builder().ownerName(OWNER_NAME).build();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createNamespace(" ", namespaceMeta));
  }

  @Test
  public void testCreateNamespace_defaultNamespaceName_nullNamespaceMeta() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createNamespace(NAMESPACE_NAME, null));
  }

  @Test
  public void testGetNamespace_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final Namespace namespace =
        new Namespace(DEFAULT_NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

    final String namespaceJson = JsonGenerator.newJsonFor(namespace);

    final String urlPath = String.format("/namespaces/%s", DEFAULT_NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(namespaceJson);

    final Namespace returnNamespace = marquezClient.getNamespace();
    assertThat(returnNamespace).isEqualTo(namespace);
  }

  @Test
  public void testGetNamespace() throws IOException, MarquezException, URISyntaxException {
    final Namespace namespace = new Namespace(NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

    final String namespaceJson = JsonGenerator.newJsonFor(namespace);

    final String urlPath = String.format("/namespaces/%s", NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(namespaceJson);

    final Namespace returnNamespace = marquezClient.getNamespace(NAMESPACE_NAME);
    assertThat(returnNamespace).isEqualTo(namespace);
  }

  @Test
  public void testGetNamespace_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getNamespace(null));
  }

  @Test
  public void testGetNamespace_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getNamespace(" "));
  }

  @Test
  public void testListNamespaces_defaultLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Namespace> namespaceList = newNamespaces(DEFAULT_LIMIT);
    final Namespaces namespaces = new Namespaces(namespaceList);

    final String namespacesJson = JsonGenerator.newJsonFor(namespaces);
    final String urlPath = String.format("/namespaces");
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(namespacesJson);

    final Namespaces returnNamespaces = marquezClient.listNamespaces();
    assertThat(returnNamespaces).isEqualTo(namespaces);
  }

  @Test
  public void testListNamespaces() throws IOException, MarquezException, URISyntaxException {
    final List<Namespace> namespaceList = newNamespaces(DEFAULT_LIMIT);
    final Namespaces namespaces = new Namespaces(namespaceList);

    final String namespacesJson = JsonGenerator.newJsonFor(namespaces);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = "/namespaces";
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);

    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(namespacesJson);

    final Namespaces returnNamespaces = marquezClient.listNamespaces(DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnNamespaces).isEqualTo(namespaces);
  }

  @Test
  public void testListNamespaces_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listNamespaces(null, DEFAULT_OFFSET));
  }

  @Test
  public void testListNamespaces_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listNamespaces(DEFAULT_LIMIT, null));
  }

  @Test
  public void testCreateJob_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            DESCRIPTION);

    final String jobMetaJson = JsonGenerator.newJsonFor(jobMeta);
    final String jobJson = JsonGenerator.newJsonFor(job);

    final String urlPath =
        String.format("/namespaces/%s/jobs/%s", DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.put(url, jobMetaJson)).thenReturn(jobJson);

    final Job returnJob = marquezClient.createJob(JOB_NAME, jobMeta);
    assertThat(returnJob).isEqualTo(job);
  }

  @Test
  public void testCreateJob_defaultNamespaceName_nullJobName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatNullPointerException().isThrownBy(() -> marquezClient.createJob(null, jobMeta));
  }

  @Test
  public void testCreateJob_defaultNamespaceName_blankJobName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.createJob(" ", jobMeta));
  }

  @Test
  public void testCreateJob_defaultNamespaceName_nullJobMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createJob(JOB_NAME, null));
  }

  @Test
  public void testCreateJob() throws IOException, MarquezException, URISyntaxException {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            DESCRIPTION);

    final String jobMetaJson = JsonGenerator.newJsonFor(jobMeta);
    final String jobJson = JsonGenerator.newJsonFor(job);

    final String urlPath = String.format("/namespaces/%s/jobs/%s", NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.put(url, jobMetaJson)).thenReturn(jobJson);

    final Job returnJob = marquezClient.createJob(NAMESPACE_NAME, JOB_NAME, jobMeta);
    assertThat(returnJob).isEqualTo(job);
  }

  @Test
  public void testCreateJob_nullNamespaceName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJob(null, JOB_NAME, jobMeta));
  }

  @Test
  public void testCreateJob_blankNamespaceName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createJob(" ", JOB_NAME, jobMeta));
  }

  @Test
  public void testCreateJob_nullJobName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJob(NAMESPACE_NAME, null, jobMeta));
  }

  @Test
  public void testCreateJob_blankJobName() {
    final JobMeta jobMeta =
        JobMeta.builder()
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();

    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createJob(NAMESPACE_NAME, " ", jobMeta));
  }

  @Test
  public void testCreateJob_nullJobMeta() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJob(NAMESPACE_NAME, JOB_NAME, null));
  }

  @Test
  public void testGetJob_defaultNamespace()
      throws IOException, MarquezException, URISyntaxException {
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            DESCRIPTION);

    final String jobJson = JsonGenerator.newJsonFor(job);

    final String urlPath =
        String.format("/namespaces/%s/jobs/%s", DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobJson);

    final Job returnJob = marquezClient.getJob(JOB_NAME);
    assertThat(returnJob).isEqualTo(job);
  }

  @Test
  public void testGetJob_defaultNamespace_nullJobName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getJob(null));
  }

  @Test
  public void testGetJob_defaultNamespace_blankJobName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getJob(" "));
  }

  @Test
  public void testGetJob() throws IOException, MarquezException, URISyntaxException {
    final Job job =
        new Job(
            JOB_NAME,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION,
            DESCRIPTION);

    final String jobJson = JsonGenerator.newJsonFor(job);

    final String urlPath = String.format("/namespaces/%s/jobs/%s", NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobJson);

    final Job returnJob = marquezClient.getJob(NAMESPACE_NAME, JOB_NAME);
    assertThat(returnJob).isEqualTo(job);
  }

  @Test
  public void testGetJob_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getJob(null, JOB_NAME));
  }

  @Test
  public void testGetJob_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getJob(" ", JOB_NAME));
  }

  @Test
  public void testGetJob_nullJobName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getJob(NAMESPACE_NAME, null));
  }

  @Test
  public void testGetJob_blankJobName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.getJob(NAMESPACE_NAME, " "));
  }

  @Test
  public void testListJobs_defaultNamespaceNameLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Job> jobList = newJobs(DEFAULT_LIMIT);
    final Jobs jobs = new Jobs(jobList);

    final String jobsJson = JsonGenerator.newJsonFor(jobs);

    final String urlPath = String.format("/namespaces/%s/jobs", DEFAULT_NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobsJson);

    final Jobs returnJobs = marquezClient.listJobs();
    assertThat(returnJobs).isEqualTo(jobs);
  }

  @Test
  public void testListJobs_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final List<Job> jobList = newJobs(DEFAULT_LIMIT);
    final Jobs jobs = new Jobs(jobList);

    final String jobsJson = JsonGenerator.newJsonFor(jobs);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = String.format("/namespaces/%s/jobs", DEFAULT_NAMESPACE_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobsJson);

    final Jobs returnJobs = marquezClient.listJobs(DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnJobs).isEqualTo(jobs);
  }

  @Test
  public void testListJobs_defaultNamespaceName_nullLimit() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobs(null, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobs_defaultNamespaceName_nullOffset() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobs(DEFAULT_LIMIT, null));
  }

  @Test
  public void testListJobs_defaultLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Job> jobList = newJobs(DEFAULT_LIMIT);
    final Jobs jobs = new Jobs(jobList);

    final String jobsJson = JsonGenerator.newJsonFor(jobs);

    final String urlPath = String.format("/namespaces/%s/jobs", NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobsJson);

    final Jobs returnJobs = marquezClient.listJobs(NAMESPACE_NAME);
    assertThat(returnJobs).isEqualTo(jobs);
  }

  @Test
  public void testListJobs_defaultLimitOffset_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobs(null));
  }

  @Test
  public void testListJobs_defaultLimitOffset_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.listJobs(" "));
  }

  @Test
  public void testListJobs() throws IOException, MarquezException, URISyntaxException {
    final List<Job> jobList = newJobs(DEFAULT_LIMIT);
    final Jobs jobs = new Jobs(jobList);

    final String jobsJson = JsonGenerator.newJsonFor(jobs);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = String.format("/namespaces/%s/jobs", NAMESPACE_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobsJson);

    final Jobs returnJobs = marquezClient.listJobs(NAMESPACE_NAME, DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnJobs).isEqualTo(jobs);
  }

  @Test
  public void testListJobs_nullNamespaceName() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobs(null, DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobs_blankNamespaceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.listJobs(" ", DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobs_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobs(NAMESPACE_NAME, null, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobs_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobs(NAMESPACE_NAME, DEFAULT_LIMIT, null));
  }

  @Test
  public void testCreateJobRun_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    final JobRun jobRun =
        new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

    final String jobRunMetaJson = JsonGenerator.newJsonFor(jobRunMeta);
    final String jobRunJson = JsonGenerator.newJsonFor(jobRun);

    final String urlPath =
        String.format("/namespaces/%s/jobs/%s/runs", DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.post(url, jobRunMetaJson)).thenReturn(jobRunJson);

    final JobRun returnJobRun = marquezClient.createJobRun(JOB_NAME, jobRunMeta);
    assertThat(returnJobRun).isEqualTo(jobRun);
  }

  @Test
  public void testCreateJobRun_defaultNamespaceName_nullJobName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createJobRun(null, jobRunMeta));
  }

  @Test
  public void testCreateJobRun_defaultNamespaceName_blankJobName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createJobRun(" ", jobRunMeta));
  }

  @Test
  public void testCreateJobRun_defaultNamespaceName_nullJobRunMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createJobRun(JOB_NAME, null));
  }

  @Test
  public void testCreateJobRun() throws IOException, MarquezException, URISyntaxException {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    final JobRun jobRun =
        new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

    final String jobRunMetaJson = JsonGenerator.newJsonFor(jobRunMeta);
    final String jobRunJson = JsonGenerator.newJsonFor(jobRun);

    final String urlPath = String.format("/namespaces/%s/jobs/%s/runs", NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.post(url, jobRunMetaJson)).thenReturn(jobRunJson);

    final JobRun returnJobRun = marquezClient.createJobRun(NAMESPACE_NAME, JOB_NAME, jobRunMeta);
    assertThat(returnJobRun).isEqualTo(jobRun);
  }

  @Test
  public void testCreateJobRun_nullNamespaceName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJobRun(null, JOB_NAME, jobRunMeta));
  }

  @Test
  public void testCreateJobRun_blankNamespaceName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createJobRun(" ", JOB_NAME, jobRunMeta));
  }

  @Test
  public void testCreateJobRun_nullJobName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJobRun(NAMESPACE_NAME, null, jobRunMeta));
  }

  @Test
  public void testCreateJobRun_blankJobName() {
    final JobRunMeta jobRunMeta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createJobRun(NAMESPACE_NAME, " ", jobRunMeta));
  }

  @Test
  public void testCreateJobRun_nullJobRunMeta() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createJobRun(NAMESPACE_NAME, JOB_NAME, null));
  }

  @Test
  public void testGetJobRun() throws IOException, MarquezException, URISyntaxException {
    final JobRun jobRun =
        new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

    final String jobRunJson = JsonGenerator.newJsonFor(jobRun);

    final String urlPath = String.format("/jobs/runs/%s", RUN_ID);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobRunJson);

    final JobRun returnJobRun = marquezClient.getJobRun(RUN_ID);
    assertThat(returnJobRun).isEqualTo(jobRun);
  }

  @Test
  public void testGetJobRun_nullRunId() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getJobRun(null));
  }

  @Test
  public void testGetJobRun_blankRunId() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getJobRun(" "));
  }

  @Test
  public void testListJobRuns_defaultNamespaceLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<JobRun> jobRunList = newJobRuns(DEFAULT_LIMIT);
    final JobRuns jobRuns = new JobRuns(jobRunList);

    final String jobRunsJson = JsonGenerator.newJsonFor(jobRuns);

    final String urlPath =
        String.format("/namespaces/%s/jobs/%s/runs", DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobRunsJson);

    final JobRuns returnJobRuns = marquezClient.listJobRuns(JOB_NAME);
    assertThat(returnJobRuns).isEqualTo(jobRuns);
  }

  @Test
  public void testListJobRuns_defaultNamespaceLimitOffset_nullJobName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobRuns(null));
  }

  @Test
  public void testListJobRuns_defaultNamespaceLimitOffset_blankJobName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.listJobRuns(" "));
  }

  @Test
  public void testListJobRuns_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final List<JobRun> jobRunList = newJobRuns(DEFAULT_LIMIT);
    final JobRuns jobRuns = new JobRuns(jobRunList);

    final String jobRunsJson = JsonGenerator.newJsonFor(jobRuns);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath =
        String.format("/namespaces/%s/jobs/%s/runs", DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobRunsJson);

    final JobRuns returnJobRuns =
        marquezClient.listJobRuns(JOB_NAME, DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnJobRuns).isEqualTo(jobRuns);
  }

  @Test
  public void testListJobRuns_defaultNamespaceName_nullJobName() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobRuns(null, DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_defaultNamespaceName_blankJobName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.listJobRuns(" ", DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_defaultNamespaceName_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobRuns(JOB_NAME, null, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_defaultNamespaceName_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobRuns(JOB_NAME, DEFAULT_LIMIT, null));
  }

  @Test
  public void testListJobRuns_defaultLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<JobRun> jobRunList = newJobRuns(DEFAULT_LIMIT);
    final JobRuns jobRuns = new JobRuns(jobRunList);

    final String jobRunsJson = JsonGenerator.newJsonFor(jobRuns);

    final String urlPath = String.format("/namespaces/%s/jobs/%s/runs", NAMESPACE_NAME, JOB_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobRunsJson);

    final JobRuns returnJobRuns = marquezClient.listJobRuns(NAMESPACE_NAME, JOB_NAME);
    assertThat(returnJobRuns).isEqualTo(jobRuns);
  }

  @Test
  public void testListJobRuns_defaultLimitOffset_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobRuns(null, JOB_NAME));
  }

  @Test
  public void testListJobRuns_defaultLimitOffset_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.listJobRuns(" ", JOB_NAME));
  }

  @Test
  public void testListJobRuns_defaultLimitOffset_nullJobName() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobRuns(NAMESPACE_NAME, null));
  }

  @Test
  public void testListJobRuns_defaultLimitOffset_blankJobName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.listJobRuns(NAMESPACE_NAME, " "));
  }

  @Test
  public void testListJobRuns() throws IOException, MarquezException, URISyntaxException {
    final List<JobRun> jobRunList = newJobRuns(DEFAULT_LIMIT);
    final JobRuns jobRuns = new JobRuns(jobRunList);

    final String jobRunsJson = JsonGenerator.newJsonFor(jobRuns);

    final Map<String, Object> params = getQueryParams(2, DEFAULT_OFFSET);
    final String urlPath = String.format("/namespaces/%s/jobs/%s/runs", NAMESPACE_NAME, JOB_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(jobRunsJson);

    final JobRuns returnJobRuns =
        marquezClient.listJobRuns(NAMESPACE_NAME, JOB_NAME, DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnJobRuns).isEqualTo(jobRuns);
  }

  @Test
  public void testListJobRuns_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listJobRuns(null, JOB_NAME));
  }

  @Test
  public void testListJobRuns_blankNamespaceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.listJobRuns(" ", JOB_NAME, DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_nullJobName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> marquezClient.listJobRuns(NAMESPACE_NAME, null, DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_blankJobName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> marquezClient.listJobRuns(NAMESPACE_NAME, " ", DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> marquezClient.listJobRuns(NAMESPACE_NAME, JOB_NAME, null, DEFAULT_OFFSET));
  }

  @Test
  public void testListJobRuns_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listJobRuns(NAMESPACE_NAME, JOB_NAME, DEFAULT_LIMIT, null));
  }

  @Test
  public void testMarkJobAsRunning_nullRunId() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.markJobRunAsRunning(null));
  }

  @Test
  public void testMarkJobAsRunning_blankRunId() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.markJobRunAsRunning(" "));
  }

  @Test
  public void testMarkJobAsCompleted_nullRunId() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.markJobRunAsCompleted(null));
  }

  @Test
  public void testMarkJobAsCompleted_blankRunId() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.markJobRunAsCompleted(" "));
  }

  @Test
  public void testMarkJobAsFailed_nullRunId() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.markJobRunAsFailed(null));
  }

  @Test
  public void testMarkJobAsFailed_blankRunId() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.markJobRunAsFailed(" "));
  }

  @Test
  public void testMarkJobAsAborted_nullRunId() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.markJobRunAsAborted(null));
  }

  @Test
  public void testMarkJobAsAborted_blankRunId() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.markJobRunAsAborted(" "));
  }

  @Test
  public void testCreateDatasource() throws IOException, MarquezException, URISyntaxException {
    final DatasourceMeta datasourceMeta =
        DatasourceMeta.builder().name(DATASOURCE_NAME).connectionUrl(CONNECTION_URL).build();
    final Datasource datasource = new Datasource(DATASOURCE_NAME, CREATED_AT, URN, CONNECTION_URL);

    final String datasourceMetaJson = JsonGenerator.newJsonFor(datasourceMeta);
    final String datasourceJson = JsonGenerator.newJsonFor(datasource);

    final String urlPath = "/datasources";
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.post(url, datasourceMetaJson)).thenReturn(datasourceJson);

    final Datasource returnDatasource = marquezClient.createDatasource(datasourceMeta);
    assertThat(returnDatasource).isEqualTo(datasource);
  }

  @Test
  public void testCreateDatasource_nullDatasourceMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createDatasource(null));
  }

  @Test
  public void testGetDatasource() throws IOException, MarquezException, URISyntaxException {
    final Datasource datasource = new Datasource(DATASOURCE_NAME, CREATED_AT, URN, CONNECTION_URL);

    final String datasourceJson = JsonGenerator.newJsonFor(datasource);

    final String urlPath = String.format("/datasources/%s", URN);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasourceJson);

    final Datasource returnDatasource = marquezClient.getDatasource(URN);
    assertThat(returnDatasource).isEqualTo(datasource);
  }

  @Test
  public void testGetDatasource_nullDatasourceUrn() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getDatasource(null));
  }

  @Test
  public void testGetDatasource_blankDatasourceUrn() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getDatasource(" "));
  }

  @Test
  public void testListDatasources_defaultLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Datasource> datasourceList = newDatasources(DEFAULT_LIMIT);
    final Datasources datasources = new Datasources(datasourceList);

    final String datasourcesJson = JsonGenerator.newJsonFor(datasources);

    final String urlPath = "/datasources";
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasourcesJson);

    final Datasources returnDatasources = marquezClient.listDatasources();
    assertThat(returnDatasources).isEqualTo(datasources);
  }

  @Test
  public void testListDatasources() throws IOException, MarquezException, URISyntaxException {
    final List<Datasource> datasourceList = newDatasources(DEFAULT_LIMIT);
    final Datasources datasources = new Datasources(datasourceList);

    final String datasourcesJson = JsonGenerator.newJsonFor(datasources);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = "/datasources";
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasourcesJson);

    final Datasources returnDatasources =
        marquezClient.listDatasources(DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnDatasources).isEqualTo(datasources);
  }

  @Test
  public void testListDatasources_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasources(null, DEFAULT_OFFSET));
  }

  @Test
  public void testListDatasources_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasources(DEFAULT_LIMIT, null));
  }

  @Test
  public void testCreateDataset_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);

    final String datasetMetaJson = JsonGenerator.newJsonFor(datasetMeta);
    final String datasetJson = JsonGenerator.newJsonFor(dataset);

    final String urlPath = String.format("/namespaces/%s/datasets", DEFAULT_NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.post(url, datasetMetaJson)).thenReturn(datasetJson);

    final Dataset returnDataset = marquezClient.createDataset(datasetMeta);
    assertThat(returnDataset).isEqualTo(dataset);
  }

  @Test
  public void testCreateDataset_defaultNamespaceName_nullDatasetMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createDataset(null));
  }

  @Test
  public void testCreateDataset() throws IOException, MarquezException, URISyntaxException {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);

    final String datasetMetaJson = JsonGenerator.newJsonFor(datasetMeta);
    final String datasetJson = JsonGenerator.newJsonFor(dataset);

    final String urlPath = String.format("/namespaces/%s/datasets", NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.post(url, datasetMetaJson)).thenReturn(datasetJson);

    final Dataset returnDataset = marquezClient.createDataset(NAMESPACE_NAME, datasetMeta);
    assertThat(returnDataset).isEqualTo(dataset);
  }

  @Test
  public void testCreateDataset_nullDatasetMeta() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.createDataset(null));
  }

  @Test
  public void testCreateDataset_nullNamespaceName() {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.createDataset(null, datasetMeta));
  }

  @Test
  public void testCreateDataset_blankNamespaceName() {
    final DatasetMeta datasetMeta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.createDataset(" ", datasetMeta));
  }

  @Test
  public void testGetDataset_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);

    final String datasetJson = JsonGenerator.newJsonFor(dataset);

    final String urlPath = String.format("/namespaces/%s/datasets/%s", DEFAULT_NAMESPACE_NAME, URN);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetJson);

    final Dataset returnDataset = marquezClient.getDataset(URN);
    assertThat(returnDataset).isEqualTo(dataset);
  }

  @Test
  public void testGetDataset_defaultNamespaceName_nullUrn() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getDataset(null));
  }

  @Test
  public void testGetDataset_defaultNamespaceName_blankUrn() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getDataset(" "));
  }

  @Test
  public void testGetDataset() throws IOException, MarquezException, URISyntaxException {
    final Dataset dataset = new Dataset(DATASET_NAME, CREATED_AT, URN, DATASOURCE_URN, DESCRIPTION);

    final String datasetJson = JsonGenerator.newJsonFor(dataset);

    final String urlPath = String.format("/namespaces/%s/datasets/%s", NAMESPACE_NAME, URN);

    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetJson);

    final Dataset returnDataset = marquezClient.getDataset(NAMESPACE_NAME, URN);
    assertThat(returnDataset).isEqualTo(dataset);
  }

  @Test
  public void testGetDataset_nullUrn() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.getDataset(NAMESPACE_NAME, null));
  }

  @Test
  public void testGetDataset_blankUrn() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.getDataset(NAMESPACE_NAME, " "));
  }

  @Test
  public void testGetDataset_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.getDataset(null, URN));
  }

  @Test
  public void testGetDataset_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.getDataset(" ", URN));
  }

  @Test
  public void testListDatasets_defaultNamespaceNameLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Dataset> datasetList = newDatasets(DEFAULT_LIMIT);
    final Datasets datasets = new Datasets(datasetList);

    final String datasetsJson = JsonGenerator.newJsonFor(datasets);

    final String urlPath = String.format("/namespaces/%s/datasets", DEFAULT_NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetsJson);

    final Datasets returnDatasets = marquezClient.listDatasets();
    assertThat(returnDatasets).isEqualTo(datasets);
  }

  @Test
  public void testListDatasets_defaultNamespaceName()
      throws IOException, MarquezException, URISyntaxException {
    final List<Dataset> datasetList = newDatasets(DEFAULT_LIMIT);
    final Datasets datasets = new Datasets(datasetList);

    final String datasetsJson = JsonGenerator.newJsonFor(datasets);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = String.format("/namespaces/%s/datasets", DEFAULT_NAMESPACE_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetsJson);

    final Datasets returnDatasets = marquezClient.listDatasets(DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnDatasets).isEqualTo(datasets);
  }

  @Test
  public void testListDatasets_defaultNamespaceName_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasets(null, DEFAULT_OFFSET));
  }

  @Test
  public void testListDatasets_defaultNamespaceName_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasets(DEFAULT_LIMIT, null));
  }

  @Test
  public void testListDatasets_defaultLimitOffset()
      throws IOException, MarquezException, URISyntaxException {
    final List<Dataset> datasetList = newDatasets(DEFAULT_LIMIT);
    final Datasets datasets = new Datasets(datasetList);

    final String datasetsJson = JsonGenerator.newJsonFor(datasets);

    final String urlPath = String.format("/namespaces/%s/datasets", NAMESPACE_NAME);
    final URL url = getUrl(urlPath);
    when(mockMarquezHttp.url(urlPath)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetsJson);

    final Datasets returnDatasets = marquezClient.listDatasets(NAMESPACE_NAME);
    assertThat(returnDatasets).isEqualTo(datasets);
  }

  @Test
  public void testListDatasets_defaultLimitOffset_nullNamespaceName() {
    assertThatNullPointerException().isThrownBy(() -> marquezClient.listDatasets(null));
  }

  @Test
  public void testListDatasets_defaultLimitOffset_blankNamespaceName() {
    assertThatIllegalArgumentException().isThrownBy(() -> marquezClient.listDatasets(" "));
  }

  @Test
  public void testListDatasets() throws IOException, MarquezException, URISyntaxException {
    final List<Dataset> datasetList = newDatasets(DEFAULT_LIMIT);
    final Datasets datasets = new Datasets(datasetList);

    final String datasetsJson = JsonGenerator.newJsonFor(datasets);

    final Map<String, Object> params = getQueryParams(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final String urlPath = String.format("/namespaces/%s/datasets", NAMESPACE_NAME);
    final String urlQuery = getQueryParamsPath(DEFAULT_LIMIT, DEFAULT_OFFSET);
    final URL url = getUrl(urlPath, urlQuery);
    when(mockMarquezHttp.url(urlPath, params)).thenReturn(url);
    when(mockMarquezHttp.get(url)).thenReturn(datasetsJson);

    final Datasets returnDatasets =
        marquezClient.listDatasets(NAMESPACE_NAME, DEFAULT_LIMIT, DEFAULT_OFFSET);
    assertThat(returnDatasets).isEqualTo(datasets);
  }

  @Test
  public void testListDatasets_nullNamespaceName() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasets(null, DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListDatasets_blankNamespaceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> marquezClient.listDatasets(" ", DEFAULT_LIMIT, DEFAULT_OFFSET));
  }

  @Test
  public void testListDatasets_nullLimit() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasets(NAMESPACE_NAME, null, DEFAULT_OFFSET));
  }

  @Test
  public void testListDatasets_nullOffset() {
    assertThatNullPointerException()
        .isThrownBy(() -> marquezClient.listDatasets(NAMESPACE_NAME, DEFAULT_LIMIT, null));
  }
}
