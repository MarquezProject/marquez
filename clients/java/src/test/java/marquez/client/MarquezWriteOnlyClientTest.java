package marquez.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.UUID;
import marquez.client.models.DatasetMeta;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.JobType;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SourceMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class MarquezWriteOnlyClientTest {
  @Mock private Backend backend;
  private MarquezWriteOnlyClient client;

  @Before
  public void setUp() {
    client = Clients.newWriteOnlyClient(backend);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(backend);
  }

  @Test
  public void testCreateNamespace() {
    NamespaceMeta namespaceMeta = new NamespaceMeta("owner", "description");
    client.createNamespace("foo", namespaceMeta);
    verify(backend, times(1)).put("/api/v1/namespaces/foo", namespaceMeta.toJson());
  }

  @Test
  public void testCreateSource() throws URISyntaxException {
    SourceMeta sourceMeta = new SourceMeta("type", new URI("connection:uri"), "description");
    client.createSource("sourceFoo", sourceMeta);
    verify(backend, times(1)).put("/api/v1/sources/sourceFoo", sourceMeta.toJson());
  }

  @Test
  public void testCreateDataset() throws URISyntaxException {
    DatasetMeta datasetMeta =
        DbTableMeta.builder().physicalName("physical").sourceName("source").build();
    client.createDataset("namespaceName", "datasetName", datasetMeta);
    verify(backend, times(1))
        .put("/api/v1/namespaces/namespaceName/datasets/datasetName", datasetMeta.toJson());
  }

  @Test
  public void testCreateJob() {
    JobMeta jobMeta = JobMeta.builder().type(JobType.BATCH).build();
    client.createJob("namespaceName", "jobName", jobMeta);
    verify(backend, times(1))
        .put("/api/v1/namespaces/namespaceName/jobs/jobName", jobMeta.toJson());
  }

  @Test
  public void testCreateRun() {
    RunMeta runMeta = RunMeta.builder().build();
    client.createRun("namespaceName", "jobName", runMeta);
    verify(backend, times(1))
        .post("/api/v1/namespaces/namespaceName/jobs/jobName/runs", runMeta.toJson());
  }

  @Test
  public void testCreateRunWithId() {
    String id = UUID.randomUUID().toString();
    RunMeta runMeta = RunMeta.builder().id(id).build();
    client.createRun("namespaceName", "jobName", runMeta);
    verify(backend, times(1))
        .post("/api/v1/namespaces/namespaceName/jobs/jobName/runs", runMeta.toJson());
  }

  @Test
  public void testMarkRunAs() throws UnsupportedEncodingException {
    String runId = UUID.randomUUID().toString();
    Instant at = Instant.now();
    String atParam = URLEncoder.encode(String.valueOf(at), UTF_8.name());
    client.markRunAsRunning(runId, at);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/start?at=" + atParam);
    client.markRunAsAborted(runId, at);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/abort?at=" + atParam);
    client.markRunAsCompleted(runId, at);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/complete?at=" + atParam);
    client.markRunAsFailed(runId, at);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/fail?at=" + atParam);
  }

  @Test
  public void testMarkRunAsNoAt() {
    String runId = UUID.randomUUID().toString();
    client.markRunAsRunning(runId);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/start");
    client.markRunAsAborted(runId);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/abort");
    client.markRunAsCompleted(runId);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/complete");
    client.markRunAsFailed(runId);
    verify(backend, times(1)).post("/api/v1/jobs/runs/" + runId + "/fail");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarkRunAsNew() {
    String runId = UUID.randomUUID().toString();
    Instant at = Instant.now();
    client.markRunAs(runId, RunState.NEW, at);
  }

  @Test
  public void testClose() throws IOException {
    client.close();
    verify(backend, times(1)).close();
  }
}
