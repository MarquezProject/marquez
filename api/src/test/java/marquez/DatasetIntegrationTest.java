package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetVersion;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.StreamVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class DatasetIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    createSource(STREAM_SOURCE_NAME);
  }

  @Test
  public void testApp_testTags() {
    DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(ImmutableList.of(newFieldWith(SENSITIVE.getName()), newField()))
            .tags(ImmutableSet.of(PII.getName()))
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset dataset = client.createDataset(NAMESPACE_NAME, "test-dataset-tags", DB_TABLE_META);
    assertThat(dataset.getFields().get(0).getTags())
        .isEqualTo(ImmutableSet.of(SENSITIVE.getName()));
    assertThat(dataset.getFields().get(1).getTags()).isEmpty();
    assertThat(dataset.getTags()).isEqualTo(ImmutableSet.of(PII.getName()));

    DbTableMeta UPDATED_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(
                ImmutableList.of(
                    newFieldWith(PII.getName()),
                    DB_TABLE_META.getFields().get(0))) // changed fields
            .tags(ImmutableSet.of(SENSITIVE.getName())) // added dataset tag
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset updateDataset = client.createDataset(NAMESPACE_NAME, "test-dataset-tags", UPDATED_META);
    assertThat(updateDataset.getTags())
        .isEqualTo(ImmutableSet.of(SENSITIVE.getName(), PII.getName()));
    assertThat(updateDataset.getFields()).isEqualTo(UPDATED_META.getFields());

    Dataset getDataset = client.getDataset(NAMESPACE_NAME, "test-dataset-tags");
    assertThat(getDataset.getFields()).isEqualTo(UPDATED_META.getFields());
    assertThat(getDataset.getTags()).isEqualTo(ImmutableSet.of(SENSITIVE.getName(), PII.getName()));
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
    DatasetVersion version = versions.get(0); // most recent dataset version
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

  @Test
  public void testApp_notExistsDatasetName() {
    Assertions.assertThrows(
        Exception.class, () -> client.getDataset(NAMESPACE_NAME, "not-existing"));
  }

  @Test
  public void testApp_notExistsDatasetVersionName() {
    Assertions.assertThrows(
        Exception.class,
        () ->
            client.getDatasetVersion(NAMESPACE_NAME, "not-existing", UUID.randomUUID().toString()));
  }

  @Test
  public void testApp_notExistsNamespace() {
    Assertions.assertThrows(
        Exception.class, () -> client.getDataset("non-existing", "not-existing"));
  }

  @Test
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
    Assertions.assertThrows(
        Exception.class, () -> client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS));
  }

  @Test
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
    Assertions.assertThrows(
        Exception.class, () -> client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS));
  }

  @Test
  public void testApp_upsertDescription() {
    DbTableMeta DESCRIPTION =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset dataset = client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DESCRIPTION);
    assertThat(dataset.getDescription()).isEqualTo(DESCRIPTION.getDescription());

    DbTableMeta WO_DESCRIPTION =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .build();

    Dataset dataset2 = client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, WO_DESCRIPTION);
    // Description stays
    assertThat(dataset2.getDescription()).isEqualTo(DESCRIPTION.getDescription());
  }
}
