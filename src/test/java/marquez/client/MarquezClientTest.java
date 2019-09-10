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

import static marquez.client.MarquezClient.Builder.DEFAULT_BASE_URL;
import static marquez.client.MarquezClient.Builder.DEFAULT_NAMESPACE_NAME;
import static marquez.client.MarquezClient.Builder.NAMESPACE_NAME_ENV_VAR;
import static marquez.client.models.ModelGenerator.newConnectionUrl;
import static marquez.client.models.ModelGenerator.newDatasetName;
import static marquez.client.models.ModelGenerator.newDatasetUrn;
import static marquez.client.models.ModelGenerator.newDatasetUrns;
import static marquez.client.models.ModelGenerator.newDatasourceName;
import static marquez.client.models.ModelGenerator.newDatasourceType;
import static marquez.client.models.ModelGenerator.newDatasourceUrn;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newJobName;
import static marquez.client.models.ModelGenerator.newJobType;
import static marquez.client.models.ModelGenerator.newLocation;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static marquez.client.models.ModelGenerator.newRunArgs;
import static marquez.client.models.ModelGenerator.newRunId;
import static marquez.client.models.ModelGenerator.newTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetMeta;
import marquez.client.models.Datasource;
import marquez.client.models.DatasourceMeta;
import marquez.client.models.DatasourceType;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobRun;
import marquez.client.models.JobRunMeta;
import marquez.client.models.JobType;
import marquez.client.models.JsonGenerator;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class MarquezClientTest {
  private static final Instant CREATED_AT = newTimestamp();
  private static final Instant UPDATED_AT = Instant.from(CREATED_AT);

  // NAMESPACE
  private static final String NAMESPACE_NAME = newNamespaceName();
  private static final String OWNER_NAME = newOwnerName();
  private static final String DESCRIPTION = newDescription();
  private static final Namespace NAMESPACE =
      new Namespace(NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

  // DATASOURCE
  private static final DatasourceType DATASOURCE_TYPE = newDatasourceType();
  private static final String DATASOURCE_NAME = newDatasourceName();
  private static final String DATASOURCE_URN = newDatasourceUrn();
  private static final String CONNECTION_URL = newConnectionUrl();
  private static final Datasource DATASOURCE =
      new Datasource(DATASOURCE_TYPE, DATASOURCE_NAME, CREATED_AT, DATASOURCE_URN, CONNECTION_URL);

  // DATASET
  private static final String DATASET_NAME = newDatasetName();
  private static final String DATASET_URN = newDatasetUrn();
  private static final Dataset DATASET =
      new Dataset(DATASET_NAME, CREATED_AT, DATASET_URN, DATASOURCE_URN, DESCRIPTION);

  // JOB
  private static final String JOB_NAME = newJobName();
  private static final List<String> INPUT_DATASET_URNS = newDatasetUrns(3);
  private static final List<String> OUTPUT_DATASET_URNS = newDatasetUrns(4);
  private static final String LOCATION = newLocation();
  private static final JobType JOB_TYPE = newJobType();
  private static final Job JOB =
      new Job(
          JOB_TYPE,
          JOB_NAME,
          CREATED_AT,
          UPDATED_AT,
          INPUT_DATASET_URNS,
          OUTPUT_DATASET_URNS,
          LOCATION,
          DESCRIPTION);

  // JOB RUN
  private static final String RUN_ID = newRunId();
  private static final Instant NOMINAL_START_TIME = newTimestamp();
  private static final Instant NOMINAL_END_TIME = newTimestamp();
  private static final String RUN_ARGS = newRunArgs();
  private static final JobRun.State RUN_STATE = JobRun.State.NEW;
  private static final JobRun RUN =
      new JobRun(RUN_ID, NOMINAL_START_TIME, NOMINAL_END_TIME, RUN_ARGS, RUN_STATE);

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
    final String nullUrlAsString = null;
    assertThatNullPointerException()
        .isThrownBy(() -> MarquezClient.builder().baseUrl(nullUrlAsString).build());

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
    final String badUrlAsString = "test.com/api/v1";
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MarquezClient.builder().baseUrl(badUrlAsString).build());
  }

  @Test
  public void testCreateNamespace() throws Exception {
    final String pathTemplate = "/namespaces/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME)).thenReturn(url);

    final NamespaceMeta meta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(DESCRIPTION).build();
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
  public void testCreateDatasource() throws Exception {
    final String path = "/datasources";
    final URL url = buildUrlFor(path);
    when(http.url(path)).thenReturn(url);

    final DatasourceMeta meta =
        DatasourceMeta.builder().name(DATASOURCE_NAME).connectionUrl(CONNECTION_URL).build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String datasourceAsJson = JsonGenerator.newJsonFor(DATASOURCE);
    when(http.post(url, metaAsJson)).thenReturn(datasourceAsJson);

    final Datasource datasource = client.createDatasource(meta);
    assertThat(datasource).isEqualTo(DATASOURCE);
  }

  @Test
  public void testGetDatasource() throws Exception {
    final String pathTemplate = "/datasources/%s";
    final String path = buildPathFor(pathTemplate, DATASOURCE_URN);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, DATASOURCE_URN)).thenReturn(url);

    final String datasourceAsJson = JsonGenerator.newJsonFor(DATASOURCE);
    when(http.get(url)).thenReturn(datasourceAsJson);

    final Datasource datasource = client.getDatasource(DATASOURCE_URN);
    assertThat(datasource).isEqualTo(DATASOURCE);
  }

  @Test
  public void testCreateDataset() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME)).thenReturn(url);

    final DatasetMeta meta =
        DatasetMeta.builder()
            .name(DATASET_NAME)
            .datasourceUrn(DATASOURCE_URN)
            .description(DESCRIPTION)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String datasetAsJson = JsonGenerator.newJsonFor(DATASET);
    when(http.post(url, metaAsJson)).thenReturn(datasetAsJson);

    final Dataset dataset = client.createDataset(NAMESPACE_NAME, meta);
    assertThat(dataset).isEqualTo(DATASET);
  }

  @Test
  public void testGetDataset() throws Exception {
    final String pathTemplate = "/namespaces/%s/datasets/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME, DATASET_URN);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME, DATASET_URN)).thenReturn(url);

    final String datasetAsJson = JsonGenerator.newJsonFor(DATASET);
    when(http.get(url)).thenReturn(datasetAsJson);

    final Dataset dataset = client.getDataset(NAMESPACE_NAME, DATASET_URN);
    assertThat(dataset).isEqualTo(DATASET);
  }

  @Test
  public void testCreateJob() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final JobMeta meta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .location(LOCATION)
            .description(DESCRIPTION)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String jobAsJson = JsonGenerator.newJsonFor(JOB);
    when(http.put(url, metaAsJson)).thenReturn(jobAsJson);

    final Job job = client.createJob(NAMESPACE_NAME, JOB_NAME, meta);
    assertThat(job).isEqualTo(JOB);
  }

  @Test
  public void testGetJob() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final String jobAsJson = JsonGenerator.newJsonFor(JOB);
    when(http.get(url)).thenReturn(jobAsJson);

    final Job job = client.getJob(NAMESPACE_NAME, JOB_NAME);
    assertThat(job).isEqualTo(JOB);
  }

  @Test
  public void testCreateJobRun() throws Exception {
    final String pathTemplate = "/namespaces/%s/jobs/%s/runs";
    final String path = buildPathFor(pathTemplate, NAMESPACE_NAME, JOB_NAME);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, NAMESPACE_NAME, JOB_NAME)).thenReturn(url);

    final JobRunMeta meta =
        JobRunMeta.builder()
            .nominalStartTime(NOMINAL_START_TIME)
            .nominalEndTime(NOMINAL_END_TIME)
            .runArgs(RUN_ARGS)
            .build();
    final String metaAsJson = JsonGenerator.newJsonFor(meta);
    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.post(url, metaAsJson)).thenReturn(runAsJson);

    final JobRun run = client.createJobRun(NAMESPACE_NAME, JOB_NAME, meta);
    assertThat(run).isEqualTo(RUN);
  }

  @Test
  public void testGetJobRun() throws Exception {
    final String pathTemplate = "/jobs/runs/%s";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    final String runAsJson = JsonGenerator.newJsonFor(RUN);
    when(http.get(url)).thenReturn(runAsJson);

    final JobRun run = client.getJobRun(RUN_ID);
    assertThat(run).isEqualTo(RUN);
  }

  @Test
  public void testMarkJobRunAsRunning() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/run";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    client.markJobRunAsRunning(RUN_ID);

    verify(http, times(1)).put(url);
  }

  @Test
  public void testMarkJobRunAsCompleted() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/complete";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    client.markJobRunAsCompleted(RUN_ID);

    verify(http, times(1)).put(url);
  }

  @Test
  public void testMarkJobRunAsAborted() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/abort";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    client.markJobRunAsAborted(RUN_ID);

    verify(http, times(1)).put(url);
  }

  @Test
  public void testMarkJobRunAsFailed() throws Exception {
    final String pathTemplate = "/jobs/runs/%s/fail";
    final String path = buildPathFor(pathTemplate, RUN_ID);
    final URL url = buildUrlFor(path);
    when(http.url(pathTemplate, RUN_ID)).thenReturn(url);

    client.markJobRunAsFailed(RUN_ID);

    verify(http, times(1)).put(url);
  }

  private String buildPathFor(String pathTemplate, String... pathArgs) {
    return String.format(pathTemplate, (Object[]) pathArgs);
  }

  private URL buildUrlFor(String path) throws Exception {
    return new URL(DEFAULT_BASE_URL + path);
  }
}
