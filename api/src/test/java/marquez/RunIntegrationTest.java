package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.UUID;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class RunIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @Test
  public void testApp_markAsFailed() {
    marquezClient.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    marquezClient.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    marquezClient.markRunAs(runId, RunState.FAILED);
  }

  @Test
  public void testApp_markAsAborted() {
    marquezClient.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    marquezClient.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    Run run = marquezClient.markRunAs(runId, RunState.ABORTED);

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

    marquezClient.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META);
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(NAMESPACE_NAME, "my-output-ds")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    marquezClient.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);

    Run createdRun = marquezClient.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());

    final DbTableMeta DB_TABLE_META_UPDATED =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(createdRun.getId())
            .build();
    marquezClient.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META_UPDATED);

    marquezClient.markRunAs(createdRun.getId(), RunState.COMPLETED);
  }
}
