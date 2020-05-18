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

import static marquez.client.MarquezClient.Builder.NAMESPACE_NAME_ENV_VAR;
import static marquez.client.MarquezClient.DEFAULT_BASE_URL;
import static marquez.client.MarquezClient.DEFAULT_NAMESPACE_NAME;
import static marquez.client.models.ModelGenerator.newConnectionUrl;
import static marquez.client.models.ModelGenerator.newContext;
import static marquez.client.models.ModelGenerator.newDatasetName;
import static marquez.client.models.ModelGenerator.newDatasetPhysicalName;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newFields;
import static marquez.client.models.ModelGenerator.newInputs;
import static marquez.client.models.ModelGenerator.newJobName;
import static marquez.client.models.ModelGenerator.newJobType;
import static marquez.client.models.ModelGenerator.newLocation;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static marquez.client.models.ModelGenerator.newOutputs;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static marquez.client.models.ModelGenerator.newRunArgs;
import static marquez.client.models.ModelGenerator.newRunId;
import static marquez.client.models.ModelGenerator.newRunState;
import static marquez.client.models.ModelGenerator.newSchemaLocation;
import static marquez.client.models.ModelGenerator.newSourceName;
import static marquez.client.models.ModelGenerator.newStreamName;
import static marquez.client.models.ModelGenerator.newTags;
import static marquez.client.models.ModelGenerator.newTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import marquez.client.models.Dataset;
import marquez.client.models.DbTable;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobType;
import marquez.client.models.JsonGenerator;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import marquez.client.models.SourceType;
import marquez.client.models.Stream;
import marquez.client.models.StreamMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class MarquezClientTest {
  // COMMON
  private static final Instant CREATED_AT = newTimestamp();
  private static final Instant UPDATED_AT = CREATED_AT;
  private static final Instant LAST_MODIFIED_AT = newTimestamp();

  // NAMESPACE
  private static final String NAMESPACE_NAME = newNamespaceName();
  private static final String OWNER_NAME = newOwnerName();
  private static final String NAMESPACE_DESCRIPTION = newDescription();
  private static final Namespace NAMESPACE =
      new Namespace(NAMESPACE_NAME, CREATED_AT, UPDATED_AT, OWNER_NAME, NAMESPACE_DESCRIPTION);

  // SOURCE
  private static final SourceType SOURCE_TYPE = SourceType.POSTGRESQL;
  private static final String SOURCE_NAME = newSourceName();
  private static final String CONNECTION_URL = newConnectionUrl();
  private static final String SOURCE_DESCRIPTION = newDescription();
  private static final Source SOURCE =
      new Source(
          SOURCE_TYPE, SOURCE_NAME, CREATED_AT, UPDATED_AT, CONNECTION_URL, SOURCE_DESCRIPTION);

  // DB TABLE DATASET
  private static final String DB_TABLE_NAME = newDatasetName();
  private static final String DB_TABLE_PHYSICAL_NAME = newDatasetPhysicalName();
  private static final String DB_TABLE_SOURCE_NAME = newSourceName();
  private static final String DB_TABLE_DESCRIPTION = newDescription();
  private static final List<Field> FIELDS = newFields();
  private static final List<String> TAGS = newTags();

  private static final DbTable DB_TABLE =
      new DbTable(
          DB_TABLE_NAME,
          DB_TABLE_PHYSICAL_NAME,
          CREATED_AT,
          UPDATED_AT,
          DB_TABLE_SOURCE_NAME,
          FIELDS,
          TAGS,
          null,
          DB_TABLE_DESCRIPTION);
  private static final DbTable DB_TABLE_MODIFIED =
      new DbTable(
          DB_TABLE_NAME,
          DB_TABLE_PHYSICAL_NAME,
          CREATED_AT,
          UPDATED_AT,
          DB_TABLE_SOURCE_NAME,
          FIELDS,
          TAGS,
          LAST_MODIFIED_AT,
          DB_TABLE_DESCRIPTION);

  // STREAM DATASET
  private static final String STREAM_NAME = newDatasetName();
  private static final String STREAM_PHYSICAL_NAME = newStreamName();
  private static final String STREAM_SOURCE_NAME = newSourceName();
  private static final URL STREAM_SCHEMA_LOCATION = newSchemaLocation();
  private static final String STREAM_DESCRIPTION = newDescription();
  private static final Stream STREAM =
      new Stream(
          STREAM_NAME,
          STREAM_PHYSICAL_NAME,
          CREATED_AT,
          UPDATED_AT,
          STREAM_SOURCE_NAME,
          FIELDS,
          TAGS,
          null,
          STREAM_SCHEMA_LOCATION,
          STREAM_DESCRIPTION);
  private static final Stream STREAM_MODIFIED =
      new Stream(
          STREAM_NAME,
          STREAM_PHYSICAL_NAME,
          CREATED_AT,
          UPDATED_AT,
          STREAM_SOURCE_NAME,
          FIELDS,
          TAGS,
          LAST_MODIFIED_AT,
          STREAM_SCHEMA_LOCATION,
          STREAM_DESCRIPTION);

  // JOB
  private static final String JOB_NAME = newJobName();
  private static final List<String> INPUTS = newInputs(2);
  private static final List<String> OUTPUTS = newOutputs(4);
  private static final String LOCATION = newLocation();
  private static final JobType JOB_TYPE = newJobType();
  private static final String JOB_DESCRIPTION = newDescription();
  private static final Map<String, String> JOB_CONTEXT = newContext();
  private static final Job JOB =
      new Job(
          JOB_TYPE,
          JOB_NAME,
          CREATED_AT,
          UPDATED_AT,
          INPUTS,
          OUTPUTS,
          LOCATION,
          JOB_DESCRIPTION,
          JOB_CONTEXT);

  // RUN
  private static final String RUN_ID = newRunId();
  private static final Instant NOMINAL_START_TIME = newTimestamp();
  private static final Instant NOMINAL_END_TIME = newTimestamp();
  private static final Run.State RUN_STATE = newRunState();
  private static final Map<String, String> RUN_ARGS = newRunArgs();
  private static final Run RUN =
      new Run(
          RUN_ID,
          CREATED_AT,
          UPDATED_AT,
          NOMINAL_START_TIME,
          NOMINAL_END_TIME,
          RUN_STATE,
          RUN_ARGS);

  @Rule public final MockitoRule rule = MockitoJUnit.rule();

  @Mock private MarquezHttp http;
  private MarquezClient client;

  @Before
  public void setUp() {
    client = new MarquezClient(http, DEFAULT_NAMESPACE_NAME);
  }

  @Test
  public void testClientBuilder_default() {
    final MarquezClient client = MarquezClient.builder().build();
    assertThat(client.http.baseUrl).isEqualTo(DEFAULT_BASE_URL);
    assertThat(client.getNamespaceName()).isEqualTo(DEFAULT_NAMESPACE_NAME);
  }

  @Test
  public void testClientBuilder_envVar() {
    final String namespaceName = newNamespaceName();
    System.setProperty(NAMESPACE_NAME_ENV_VAR, namespaceName);

    final MarquezClient client = MarquezClient.builder().namespaceName(namespaceName).build();
    assertThat(client.getNamespaceName()).isEqualTo(namespaceName);

    System.clearProperty(NAMESPACE_NAME_ENV_VAR);
  }

  @Test
  public void testClientBuilder_throwsOnNull() {
    final String nullUrlString = null;
    assertThatNullPointerException()
        .isThrownBy(() -> MarquezClient.builder().baseUrl(nullUrlString).build());

    final URL nullUrl = null;
    assertThatNullPointerException()
        .isThrownBy(() -> MarquezClient.builder().baseUrl(nullUrl).build());

    assertThatNullPointerException()
        .isThrownBy(() -> MarquezClient.builder().namespaceName(null).build());
  }

  @Test
  public void testClientBuilder_overrideUrl() throws Exception {
    final URL url = new URL("http://test.com:8080/api/v1");
    final MarquezClient client = MarquezClient.builder().baseUrl(url).build();
    assertThat(client.http.baseUrl).isEqualTo(url);
  }

  @Test
  public void testClientBuilder_throwsOnBadUrl() throws Exception {
    final String badUrlString = "test.com/api/v1";
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> MarquezClient.builder().baseUrl(badUrlString).build());
  }

  @Test
  public void testCreateNamespace() throws Exception {
    final String pathTemplate = "/namespaces/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME)).thenReturn(url);

    final NamespaceMeta meta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String namespaceAsJson = JsonGenerator.newJsonFor(NAMESPACE);
    when(http.put(url, metaAsJson)).thenReturn(namespaceAsJson);

    final Namespace namespace = client.createNamespace(NAMESPACE_NAME, meta);
    assertThat(namespace).isEqualTo(NAMESPACE);
  }

  @Test
  public void testGetNamespace() throws Exception {
    final String pathTemplate = "/namespaces/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME)).thenReturn(url);

    final String namespaceAsJson = JsonGenerator.newJsonFor(NAMESPACE);
    when(http.get(url)).thenReturn(namespaceAsJson);

    final Namespace namespace = client.getNamespace(NAMESPACE_NAME);
    assertThat(namespace).isEqualTo(NAMESPACE);
  }

  @Test
  public void testCreateSource() throws Exception {
    final String pathTemplate = "/sources/%s";
    final String path = buildPathFor(pathTemplate, SOURCE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, SOURCE_NAME)).thenReturn(url);

    final SourceMeta meta =
        SourceMeta.builder()
            .type(SOURCE_TYPE)
            .connectionUrl(CONNECTION_URL)
            .description(SOURCE_DESCRIPTION)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String sourceAsJson = JsonGenerator.newJsonFor(SOURCE);
    when(http.put(url, metaAsJson)).thenReturn(sourceAsJson);

    final Source source = client.createSource(SOURCE_NAME, meta);
    assertThat(source).isEqualTo(SOURCE);
  }

  @Test
  public void testGetSource() throws Exception {
    final String pathTemplate = "/sources/%s";
    final String path = buildPathFor(pathTemplate, SOURCE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, SOURCE_NAME)).thenReturn(url);

    final String sourceAsJson = JsonGenerator.newJsonFor(SOURCE);
    when(http.get(url)).thenReturn(sourceAsJson);

    final Source source = client.getSource(SOURCE_NAME);
    assertThat(source).isEqualTo(SOURCE);
  }

  @Test
  public void testCreateDbTable() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME)).thenReturn(url);

    final DbTableMeta meta =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(FIELDS)
            .tags(TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String dbTableAsJson = JsonGenerator.newJsonFor(DB_TABLE);
    when(http.put(url, metaAsJson)).thenReturn(dbTableAsJson);

    final Dataset dataset = client.createDataset(DB_TABLE_NAME, meta);
    assertThat(dataset).isInstanceOf(DbTable.class);
    assertThat((DbTable) dataset).isEqualTo(DB_TABLE);
  }

  @Test
  public void testGetDbTable() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME)).thenReturn(url);

    final String dbTableAsJson = JsonGenerator.newJsonFor(DB_TABLE);
    when(http.get(url)).thenReturn(dbTableAsJson);

    final Dataset dataset = client.getDataset(DB_TABLE_NAME);
    assertThat(dataset).isInstanceOf(DbTable.class);
    assertThat((DbTable) dataset).isEqualTo(DB_TABLE);
  }

  @Test
  public void testModifiedDbTable() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, DB_TABLE_NAME)).thenReturn(url);

    final String dbTableAsJson = JsonGenerator.newJsonFor(DB_TABLE);
    when(http.get(url)).thenReturn(dbTableAsJson);

    final DbTable dataset = (DbTable) client.getDataset(DB_TABLE_NAME);

    final DbTableMeta modifiedMeta =
        DbTableMeta.builder()
            .physicalName(dataset.getPhysicalName())
            .sourceName(dataset.getSourceName())
            .fields(FIELDS)
            .tags(TAGS)
            .description(dataset.getDescription().get())
            .runId(RUN_ID)
            .build();

    final Instant beforeModified = Instant.now();
    final String modifiedMetaAsJson = JsonGenerator.newJsonFor(modifiedMeta);
    final String modifiedDbTableAsJson = JsonGenerator.newJsonFor(DB_TABLE_MODIFIED);
    when(http.put(url, modifiedMetaAsJson)).thenReturn(modifiedDbTableAsJson);

    final Dataset modifiedDataset = client.createDataset(DB_TABLE_NAME, modifiedMeta);
    assertThat(modifiedDataset).isInstanceOf(DbTable.class);
    assertThat((DbTable) modifiedDataset).isEqualTo(DB_TABLE_MODIFIED);
    assertThat(modifiedDataset.getLastModifiedAt().get().isAfter(beforeModified));
  }

  @Test
  public void testCreateStream() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME)).thenReturn(url);

    final StreamMeta meta =
        StreamMeta.builder()
            .physicalName(STREAM_PHYSICAL_NAME)
            .sourceName(STREAM_SOURCE_NAME)
            .fields(FIELDS)
            .tags(TAGS)
            .description(STREAM_DESCRIPTION)
            .schemaLocation(STREAM_SCHEMA_LOCATION)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String streamAsJson = JsonGenerator.newJsonFor(STREAM);
    when(http.put(url, metaAsJson)).thenReturn(streamAsJson);

    final Dataset dataset = client.createDataset(STREAM_NAME, meta);
    assertThat(dataset).isInstanceOf(Stream.class);
    assertThat(dataset).isEqualTo(STREAM);
  }

  @Test
  public void testGetStream() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME)).thenReturn(url);

    final String streamAsJson = JsonGenerator.newJsonFor(STREAM);
    when(http.get(url)).thenReturn(streamAsJson);

    final Dataset dataset = client.getDataset(STREAM_NAME);
    assertThat(dataset).isEqualTo(STREAM);
  }

  @Test
  public void testModifiedStream() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, STREAM_NAME)).thenReturn(url);

    final String streamAsJson = JsonGenerator.newJsonFor(STREAM);
    when(http.get(url)).thenReturn(streamAsJson);

    final Stream dataset = (Stream) client.getDataset(STREAM_NAME);

    final StreamMeta modifiedMeta =
        StreamMeta.builder()
            .physicalName(dataset.getPhysicalName())
            .sourceName(dataset.getSourceName())
            .fields(FIELDS)
            .tags(TAGS)
            .description(dataset.getDescription().get())
            .schemaLocation(dataset.getSchemaLocation())
            .runId(RUN_ID)
            .build();

    final Instant beforeModified = Instant.now();
    final String modifiedMetaAsJson = JsonGenerator.newJsonFor(modifiedMeta);
    final String modifiedStreamAsJson = JsonGenerator.newJsonFor(STREAM_MODIFIED);
    when(http.put(url, modifiedMetaAsJson)).thenReturn(modifiedStreamAsJson);

    final Dataset modifiedDataset = client.createDataset(STREAM_NAME, modifiedMeta);
    assertThat(modifiedDataset).isInstanceOf(Stream.class);
    assertThat((Stream) modifiedDataset).isEqualTo(STREAM_MODIFIED);
    assertThat(modifiedDataset.getLastModifiedAt().get().isAfter(beforeModified));
  }

  @Test
  public void testCreateJob() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final JobMeta meta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(INPUTS)
            .outputs(OUTPUTS)
            .location(LOCATION)
            .description(JOB_DESCRIPTION)
            .context(JOB_CONTEXT)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String jobAsJson = JsonGenerator.newJsonFor(JOB);
    when(http.put(url, metaAsJson)).thenReturn(jobAsJson);

    final Job job = client.createJob(JOB_NAME, meta);
    assertThat(job).isEqualTo(JOB);
  }

  @Test
  public void testGetJob() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final String jobAsJson = JsonGenerator.newJsonFor(JOB);
    when(http.get(url)).thenReturn(jobAsJson);

    final Job job = client.getJob(JOB_NAME);
    assertThat(job).isEqualTo(JOB);
  }

  @Test
  public void testCreateRun() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s/runs";
    final String path = buildPathFor(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DEFAULT_NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final RunMeta meta =
        RunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .args(RUN_ARGS)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url, metaAsJson)).thenReturn(runAsJson);

    final Run run = client.createRun(JOB_NAME, meta);
    assertThat(run).isEqualTo(RUN);
  }

  @Test
  public void testGetRun() throws Exception {
    final String pathTemplate = "/jobs/runs/%s";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.get(url)).thenReturn(runAsJson);

    final Run run = client.getRun(RUN_ID);
    assertThat(run).isEqualTo(RUN);
  }

  @Test
  public void testMarkRunAsRunning() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/start";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url)).thenReturn(runAsJson);

    final Run run = client.markRunAsRunning(RUN_ID);
    assertThat(run).isEqualTo(RUN);

    verify(http, times(1)).post(url);
  }

  @Test
  public void testMarkRunAsCompleted() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/complete";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url)).thenReturn(runAsJson);

    final Run run = client.markRunAsCompleted(RUN_ID);
    assertThat(run).isEqualTo(RUN);

    verify(http, times(1)).post(url);
  }

  @Test
  public void testMarkRunAsAborted() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/abort";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url)).thenReturn(runAsJson);

    final Run run = client.markRunAsAborted(RUN_ID);
    assertThat(run).isEqualTo(RUN);

    verify(http, times(1)).post(url);
  }

  @Test
  public void testMarkRunAsFailed() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/fail";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url)).thenReturn(runAsJson);

    final Run run = client.markRunAsFailed(RUN_ID);
    assertThat(run).isEqualTo(RUN);

    verify(http, times(1)).post(url);
  }

  private String buildPathFor(String pathTemplate, String... pathArgs) {
    return String.format(pathTemplate, (Object[]) pathArgs);
  }

  private URL buildUrlFor(String path) throws Exception {
    return new URL(DEFAULT_BASE_URL + path);
  }
}
