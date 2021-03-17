package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.UUID;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class RunIntegrationTest extends BaseIntegrationTest {

  @Before
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @Test
  public void testApp_markAsFailed() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    client.markRunAs(runId, RunState.FAILED);
  }

  @Test
  public void testApp_markAsAborted() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    Run run = client.markRunAs(runId, RunState.ABORTED);

    assertThat(run.getId()).isEqualTo(runId);
  }

  @Test
  public void testApp_updateOutputDataset() {
    final DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    client.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META);
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(NAMESPACE_NAME, "my-output-ds")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);

    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());

    final DbTableMeta DB_TABLE_META_UPDATED =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(createdRun.getId())
            .build();
    client.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META_UPDATED);

    client.markRunAs(createdRun.getId(), RunState.COMPLETED);
  }
}
