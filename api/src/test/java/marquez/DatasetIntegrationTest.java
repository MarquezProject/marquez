package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetVersion;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.StreamVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class DatasetIntegrationTest extends BaseIntegrationTest {

  @Before
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    createSource(STREAM_SOURCE_NAME);
  }

  @Test
  public void testApp_getTableVersion() {
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, DB_TABLE_NAME);
    assertThat(versions).hasSizeGreaterThan(0);
    DatasetVersion datasetVersion =
        client.getDatasetVersion(NAMESPACE_NAME, DB_TABLE_NAME, versions.get(0).getVersion());

    assertThat(datasetVersion.getId()).isEqualTo(new DatasetId(NAMESPACE_NAME, DB_TABLE_NAME));
    assertThat(datasetVersion.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(datasetVersion.getCreatedAt()).isNotNull();
    assertThat(datasetVersion.getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(datasetVersion.getVersion()).isNotNull();
    assertThat(datasetVersion.getPhysicalName()).isEqualTo(DB_TABLE_META.getPhysicalName());
    assertThat(datasetVersion.getSourceName()).isEqualTo(DB_TABLE_META.getSourceName());
    assertThat(datasetVersion.getDescription()).isEqualTo(DB_TABLE_META.getDescription());
    assertThat(datasetVersion.getFields()).hasSameElementsAs(DB_TABLE_META.getFields());
    assertThat(datasetVersion.getTags()).isEqualTo(DB_TABLE_META.getTags());
    assertThat(datasetVersion.getCreatedByRun()).isEqualTo(Optional.empty());
  }

  @Test
  public void testApp_getStreamVersion() {
    client.createDataset(NAMESPACE_NAME, STREAM_NAME, STREAM_META);
    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, STREAM_NAME);
    assertThat(versions).hasSizeGreaterThan(0);
    DatasetVersion datasetVersion =
        client.getDatasetVersion(NAMESPACE_NAME, STREAM_NAME, versions.get(0).getVersion());

    assertThat(datasetVersion).isInstanceOf(StreamVersion.class);
    assertThat(datasetVersion.getId()).isEqualTo(new DatasetId(NAMESPACE_NAME, STREAM_NAME));
    assertThat(datasetVersion.getName()).isEqualTo(STREAM_NAME);
    assertThat(datasetVersion.getCreatedAt()).isNotNull();
    assertThat(datasetVersion.getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(datasetVersion.getVersion()).isNotNull();
    assertThat(datasetVersion.getPhysicalName()).isEqualTo(STREAM_META.getPhysicalName());
    assertThat(datasetVersion.getSourceName()).isEqualTo(STREAM_META.getSourceName());
    assertThat(datasetVersion.getDescription()).isEqualTo(STREAM_META.getDescription());
    assertThat(datasetVersion.getFields()).hasSameElementsAs(STREAM_META.getFields());
    assertThat(datasetVersion.getTags()).isEqualTo(STREAM_META.getTags());
    assertThat(((StreamVersion) datasetVersion).getSchemaLocation())
        .isEqualTo(STREAM_META.getSchemaLocation());
    assertThat(datasetVersion.getCreatedByRun()).isEqualTo(Optional.empty());
  }

  @Test
  public void testApp_getDBTableVersionWithRun() {
    DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();
    client.createDataset(NAMESPACE_NAME, "table1", DB_TABLE_META);

    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(NAMESPACE_NAME, "table1")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();

    client.createJob(NAMESPACE_NAME, JOB_NAME, jobMeta);

    final RunMeta runMeta = RunMeta.builder().build();
    final Run run = client.createRun(NAMESPACE_NAME, JOB_NAME, runMeta);

    DbTableMeta DB_TABLE_META_WITH_RUN =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(run.getId())
            .build();
    client.createDataset(NAMESPACE_NAME, "table1", DB_TABLE_META_WITH_RUN);

    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, "table1");
    assertThat(versions).hasSizeGreaterThan(1);
    DatasetVersion version = versions.get(1);
    assertThat(version.getCreatedByRun()).isNotEqualTo(Optional.empty());
    Run createdRun = version.getCreatedByRun().get();
    assertThat(createdRun.getCreatedAt()).isEqualTo(run.getCreatedAt());
    assertThat(createdRun.getId()).isEqualTo(run.getId());
    assertThat(createdRun.getUpdatedAt()).isEqualTo(run.getUpdatedAt());
    assertThat(createdRun.getDurationMs()).isEqualTo(run.getDurationMs());
    assertThat(createdRun.getState()).isEqualTo(run.getState());
    assertThat(createdRun.getArgs()).isEqualTo(run.getArgs());
    assertThat(createdRun.getNominalStartTime()).isEqualTo(run.getNominalStartTime());
    assertThat(createdRun.getNominalEndTime()).isEqualTo(run.getNominalEndTime());
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsDatasetName() {
    client.getDataset(NAMESPACE_NAME, "not-existing");
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsDatasetVersionName() {
    client.getDatasetVersion(NAMESPACE_NAME, "not-existing", UUID.randomUUID().toString());
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsNamespace() {
    client.getDataset("non-existing", "not-existing");
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsRun() {
    DbTableMeta RUN_NOT_EXISTS =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS);
  }

  @Test(expected = Exception.class)
  public void testApp_notExistsSource() {
    DbTableMeta RUN_NOT_EXISTS =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName("not-exists")
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS);
  }
}
