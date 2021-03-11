package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.UUID;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class JobIntegrationTest extends BaseIntegrationTest {

  @Before
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @Test
  public void testApp_listJobs() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    List<Job> jobs = client.listJobs(NAMESPACE_NAME);
    assertThat(jobs).hasSizeGreaterThan(0);
  }

  @Test
  public void testApp_listRuns() {
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());
    List<Run> runs = client.listRuns(NAMESPACE_NAME, JOB_NAME);
    assertThat(runs).hasSizeGreaterThan(0);
  }

  @Test(expected = Exception.class)
  public void testApp_createDuplicateRun() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsJobForRun() {
    client.createRun(NAMESPACE_NAME, "NotExists", RunMeta.builder().build());
  }

  @Test(expected = Exception.class)
  public void testApp_createNonMatchingJobWithRun() {
    String runId = UUID.randomUUID().toString();
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    client.createJob(NAMESPACE_NAME, "DIFFERENT_JOB", JOB_META);

    client.createRun(NAMESPACE_NAME, "DIFFERENT_JOB", RunMeta.builder().id(runId).build());

    final JobMeta JOB_META_WITH_RUN =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(runId)
            .build();
    // associate wrong run
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META_WITH_RUN);
  }

  @Test(expected = Exception.class)
  public void testApp_createJobWithMissingRun() {
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
  }

  @Test(expected = Exception.class)
  public void testApp_createNotExistingDataset() {
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(NAMESPACE_NAME, "does-not-exist")
            .outputs(NAMESPACE_NAME, "does-not-exist")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsJob() {
    client.getJob(NAMESPACE_NAME, "not-existing");
  }

  @Test
  public void getSource() {
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(SOURCE_TYPE)
            .connectionUrl(CONNECTION_URL)
            .description(SOURCE_DESCRIPTION)
            .build();
    Source createdSource = client.createSource("sourceName", sourceMeta);
    assertThat(createdSource.getCreatedAt()).isNotNull();
    assertThat(createdSource.getName()).isEqualTo("sourceName");
    assertThat(createdSource.getType()).isEqualTo(sourceMeta.getType());
    assertThat(createdSource.getUpdatedAt()).isNotNull();
    assertThat(createdSource.getDescription()).isEqualTo(sourceMeta.getDescription());
    assertThat(createdSource.getConnectionUrl()).isEqualTo(sourceMeta.getConnectionUrl());

    Source source = client.getSource("sourceName");
    assertThat(source.getCreatedAt()).isNotNull();
    assertThat(source.getName()).isEqualTo("sourceName");
    assertThat(source.getType()).isEqualTo(sourceMeta.getType());
    assertThat(source.getUpdatedAt()).isNotNull();
    assertThat(source.getDescription()).isEqualTo(sourceMeta.getDescription());
    assertThat(source.getConnectionUrl()).isEqualTo(sourceMeta.getConnectionUrl());
  }
}
