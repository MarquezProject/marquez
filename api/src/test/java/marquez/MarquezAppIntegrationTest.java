/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static java.time.Instant.EPOCH;
import static marquez.Generator.newTimestamp;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newFieldType;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newSchemaLocation;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetId;
import marquez.client.models.DbTable;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SearchResult;
import marquez.client.models.SearchResults;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import marquez.client.models.Stream;
import marquez.client.models.StreamMeta;
import marquez.client.models.Tag;
import marquez.common.models.DatasetName;
import marquez.common.models.JobType;
import marquez.db.JobDao;
import marquez.db.NamespaceDao;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.postgresql.util.PGobject;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class MarquezAppIntegrationTest extends BaseIntegrationTest {

  @ParameterizedTest
  @ValueSource(strings = {"DEFAULT", "database://localhost:1234", "s3://bucket", "bigquery:"})
  public void testApp_createNamespace(String namespaceName) {
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    final Namespace namespace = client.createNamespace(namespaceName, namespaceMeta);
    assertThat(namespace.getName()).isEqualTo(namespaceName);
    assertThat(namespace.getCreatedAt()).isAfter(EPOCH);
    assertThat(namespace.getUpdatedAt()).isAfter(EPOCH);
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getDescription()).isEqualTo(Optional.of(NAMESPACE_DESCRIPTION));

    assertThat(
            client.listNamespaces().stream()
                .filter(other -> other.getName().equals(namespaceName))
                .count())
        .isEqualTo(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {"test_source312", "s3://bucket", "asdf", "bigquery:"})
  public void testApp_createSource(String sourceName) {
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(SOURCE_TYPE)
            .connectionUrl(CONNECTION_URL)
            .description(SOURCE_DESCRIPTION)
            .build();

    final Source source = client.createSource(sourceName, sourceMeta);
    assertThat(source.getType()).isEqualTo(SOURCE_TYPE);
    assertThat(source.getName()).isEqualTo(sourceName);
    assertThat(source.getCreatedAt()).isAfter(EPOCH);
    assertThat(source.getUpdatedAt()).isAfter(EPOCH);
    assertThat(source.getConnectionUrl()).isEqualTo(CONNECTION_URL);
    assertThat(source.getDescription()).isEqualTo(Optional.of(SOURCE_DESCRIPTION));

    assertThat(
            client.listSources().stream()
                .filter(other -> other.getName().equals(sourceName))
                .count())
        .isEqualTo(1);
  }

  @Test
  public void testDatasetCustomTag() {
    createNamespace(NAMESPACE_NAME);

    String datasetName = "public.mytable";
    Set<String> datasetTag = ImmutableSet.of("ANY_DATASET");
    Set<String> fieldTag = ImmutableSet.of("ANY");
    List<Field> fields =
        ImmutableList.of(Field.builder().name("a").type("INTEGER").tags(fieldTag).build());
    final DbTableMeta dbTableMeta =
        DbTableMeta.builder()
            .physicalName(datasetName)
            .sourceName("my-source")
            .fields(fields)
            .tags(datasetTag)
            .build();
    client.createDataset(NAMESPACE_NAME, datasetName, dbTableMeta);
    Dataset dataset = client.getDataset(NAMESPACE_NAME, datasetName);
    assertThat(dataset.getTags()).isEqualTo(datasetTag);
    assertThat(dataset.getFields().get(0).getTags()).isEqualTo(fieldTag);
  }

  @Test
  public void testDatasetFieldChange() {
    createNamespace(NAMESPACE_NAME);
    createSource("my-source");

    String datasetName = "public.mytable";
    List<Field> fields =
        ImmutableList.of(
            Field.builder().name("a").type("INTEGER").build(),
            Field.builder().name("b").type("TIMESTAMP").build());
    final DbTableMeta dbTableMeta =
        DbTableMeta.builder()
            .physicalName(datasetName)
            .sourceName("my-source")
            .fields(fields)
            .build();

    client.createDataset(NAMESPACE_NAME, datasetName, dbTableMeta);
    Dataset dataset = client.getDataset(NAMESPACE_NAME, datasetName);
    assertThat(dataset.getFields()).hasSameElementsAs(fields);

    List<Field> newFields =
        ImmutableList.of(
            Field.builder().name("a").type("INTEGER").build(),
            Field.builder().name("b-fix").type("STRING").build());

    final DbTableMeta newDbTableMeta =
        DbTableMeta.builder()
            .physicalName(datasetName)
            .sourceName("my-source")
            .fields(newFields)
            .build();

    client.createDataset(NAMESPACE_NAME, datasetName, newDbTableMeta);
    Dataset updatedDataset = client.getDataset(NAMESPACE_NAME, datasetName);
    assertThat(updatedDataset.getFields()).hasSameElementsAs(newFields);
  }

  @Test
  public void testDatasetWithUnknownFieldType() {
    // (1) Create namespace for db table
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // (2) Create source for db table
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(STREAM_SOURCE_TYPE)
            .connectionUrl(STREAM_CONNECTION_URL)
            .description(STREAM_SOURCE_DESCRIPTION)
            .build();

    client.createSource(DB_TABLE_SOURCE_NAME, sourceMeta);

    // (3) Create db table with invalid field type
    final Field field0 = Field.builder().name("field0").type(newFieldType()).build();
    final Field field1 = Field.builder().name("field1").type(null).build();
    final DatasetName datasetName = newDatasetName();
    final DbTableMeta dbTableMeta =
        DbTableMeta.builder()
            .physicalName(datasetName.getValue())
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(ImmutableList.of(field0, field1))
            .build();
    final Dataset dataset =
        client.createDataset(NAMESPACE_NAME, datasetName.getValue(), dbTableMeta);
    assertThat(dataset.getFields()).containsExactly(field0, field1);
  }

  @Test
  public void testApp_createDbTable() {
    createNamespace(NAMESPACE_NAME);

    // (2) Create source for db table
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(DB_TABLE_SOURCE_TYPE)
            .connectionUrl(DB_TABLE_CONNECTION_URL)
            .description(DB_TABLE_SOURCE_DESCRIPTION)
            .build();

    client.createSource(DB_TABLE_SOURCE_NAME, sourceMeta);

    final DbTable dbTable =
        (DbTable) client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);
    assertThat(dbTable.getId()).isEqualTo(DB_TABLE_ID);
    assertThat(dbTable.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(dbTable.getPhysicalName()).isEqualTo(DB_TABLE_PHYSICAL_NAME);
    assertThat(dbTable.getCreatedAt()).isAfter(EPOCH);
    assertThat(dbTable.getUpdatedAt()).isAfter(EPOCH);
    assertThat(dbTable.getSourceName()).isEqualTo(DB_TABLE_SOURCE_NAME);
    assertThat(dbTable.getFields()).containsExactlyInAnyOrderElementsOf(DB_TABLE_FIELDS);
    assertThat(dbTable.getTags()).isEqualTo(DB_TABLE_TAGS);
    assertThat(dbTable.getLastModifiedAt()).isEmpty();
    assertThat(dbTable.getDescription()).isEqualTo(Optional.of(DB_TABLE_DESCRIPTION));

    assertThat(
            client.listDatasets(NAMESPACE_NAME).stream()
                .filter(other -> other.getName().equals(DB_TABLE_NAME))
                .count())
        .isEqualTo(1);

    assertThat(client.listDatasetVersions(NAMESPACE_NAME, DB_TABLE_NAME)).hasSize(1);

    // (4) Add field to db table
    final List<Field> original = dbTable.getFields();
    final List<Field> added = ImmutableList.of(newFieldWith(PII.getName()), newField(), newField());
    final List<Field> modifiedFields = Lists.newArrayList(Iterables.concat(original, added));

    final DbTableMeta modifiedDbTableMeta =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(modifiedFields)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    final DbTable modifiedDbTable =
        (DbTable) client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, modifiedDbTableMeta);
    assertThat(modifiedDbTable.getId()).isEqualTo(DB_TABLE_ID);
    assertThat(modifiedDbTable.getName()).isEqualTo(DB_TABLE_NAME);
    assertThat(modifiedDbTable.getPhysicalName()).isEqualTo(DB_TABLE_PHYSICAL_NAME);
    assertThat(modifiedDbTable.getCreatedAt()).isAfter(EPOCH);
    assertThat(modifiedDbTable.getUpdatedAt()).isAfter(EPOCH);
    assertThat(modifiedDbTable.getSourceName()).isEqualTo(DB_TABLE_SOURCE_NAME);
    assertThat(modifiedDbTable.getFields()).containsExactlyInAnyOrderElementsOf(modifiedFields);
    assertThat(modifiedDbTable.getTags()).isEqualTo(DB_TABLE_TAGS);
    assertThat(modifiedDbTable.getLastModifiedAt()).isEmpty();
    assertThat(modifiedDbTable.getDescription()).isEqualTo(Optional.of(DB_TABLE_DESCRIPTION));

    assertThat(client.listDatasetVersions(NAMESPACE_NAME, DB_TABLE_NAME)).hasSize(2);
  }

  @Test
  public void testApp_createStream() {
    // (1) Create namespace for stream
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // (2) Create source for stream
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(STREAM_SOURCE_TYPE)
            .connectionUrl(STREAM_CONNECTION_URL)
            .description(STREAM_SOURCE_DESCRIPTION)
            .build();

    client.createSource(STREAM_SOURCE_NAME, sourceMeta);

    // (3) Add stream to namespace and associate with source

    final Stream stream = (Stream) client.createDataset(NAMESPACE_NAME, STREAM_NAME, STREAM_META);
    assertThat(stream.getId()).isEqualTo(STREAM_ID);
    assertThat(stream.getName()).isEqualTo(STREAM_NAME);
    assertThat(stream.getPhysicalName()).isEqualTo(STREAM_PHYSICAL_NAME);
    assertThat(stream.getCreatedAt()).isAfter(EPOCH);
    assertThat(stream.getUpdatedAt()).isAfter(EPOCH);
    assertThat(stream.getSourceName()).isEqualTo(STREAM_SOURCE_NAME);
    assertThat(stream.getFields()).isEmpty();
    assertThat(stream.getTags()).isEmpty();
    assertThat(stream.getLastModifiedAt()).isEmpty();
    assertThat(stream.getSchemaLocation()).isEqualTo(Optional.of(STREAM_SCHEMA_LOCATION));
    assertThat(stream.getDescription()).isEqualTo(Optional.of(STREAM_DESCRIPTION));

    assertThat(
            client.listDatasets(NAMESPACE_NAME).stream()
                .filter(other -> other.getName().equals(STREAM_NAME))
                .count())
        .isEqualTo(1);

    assertThat(client.listDatasetVersions(NAMESPACE_NAME, STREAM_NAME)).hasSize(1);

    // (4) Change schema location
    final URL modifiedSchemaLocation = newSchemaLocation();
    final StreamMeta modifiedStreamMeta =
        StreamMeta.builder()
            .physicalName(STREAM_PHYSICAL_NAME)
            .sourceName(STREAM_SOURCE_NAME)
            .schemaLocation(modifiedSchemaLocation)
            .description(STREAM_DESCRIPTION)
            .build();

    final Stream modifiedStream =
        (Stream) client.createDataset(NAMESPACE_NAME, STREAM_NAME, modifiedStreamMeta);
    assertThat(modifiedStream.getId()).isEqualTo(STREAM_ID);
    assertThat(modifiedStream.getName()).isEqualTo(STREAM_NAME);
    assertThat(modifiedStream.getPhysicalName()).isEqualTo(STREAM_PHYSICAL_NAME);
    assertThat(modifiedStream.getCreatedAt()).isAfter(EPOCH);
    assertThat(modifiedStream.getUpdatedAt()).isAfter(EPOCH);
    assertThat(modifiedStream.getSourceName()).isEqualTo(STREAM_SOURCE_NAME);
    assertThat(modifiedStream.getFields()).isEmpty();
    assertThat(modifiedStream.getTags()).isEmpty();
    assertThat(modifiedStream.getLastModifiedAt()).isEmpty();
    assertThat(modifiedStream.getSchemaLocation()).isEqualTo(Optional.of(modifiedSchemaLocation));
    assertThat(modifiedStream.getDescription()).isEqualTo(Optional.of(STREAM_DESCRIPTION));

    assertThat(client.listDatasetVersions(NAMESPACE_NAME, STREAM_NAME)).hasSize(2);
  }

  @Test
  public void testApp_createJobAndMarkRunAsComplete() {
    // (1) Create namespace for job
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // (2) Create source for input / output db tables
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(STREAM_SOURCE_TYPE)
            .connectionUrl(STREAM_CONNECTION_URL)
            .description(STREAM_SOURCE_DESCRIPTION)
            .build();

    client.createSource(STREAM_SOURCE_NAME, sourceMeta);

    // (3) Add input db tables to namespace and associate with source
    final ImmutableSet<DatasetId> inputs = newDatasetIdsWith(NAMESPACE_NAME, 4);
    for (final DatasetId input : inputs) {
      final DbTableMeta dbTableMeta =
          DbTableMeta.builder()
              .physicalName(input.getName())
              .sourceName(STREAM_SOURCE_NAME)
              .description(newDescription())
              .build();

      client.createDataset(input.getNamespace(), input.getName(), dbTableMeta);
    }

    // (4) Add output db tables to namespace and associate with source
    final ImmutableSet<DatasetId> outputs = newDatasetIdsWith(NAMESPACE_NAME, 2);
    for (final DatasetId output : outputs) {
      final DbTableMeta dbTableMeta =
          DbTableMeta.builder()
              .physicalName(output.getName())
              .sourceName(STREAM_SOURCE_NAME)
              .description(newDescription())
              .build();

      client.createDataset(output.getNamespace(), output.getName(), dbTableMeta);
    }

    // (5) Add job to namespace
    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(inputs)
            .outputs(outputs)
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();

    final Job job = client.createJob(NAMESPACE_NAME, JOB_NAME, jobMeta);
    assertThat(job.getId()).isEqualTo(JOB_ID);
    assertThat(job.getType()).isEqualTo(JOB_TYPE);
    assertThat(job.getName()).isEqualTo(JOB_NAME);
    assertThat(job.getCreatedAt()).isAfter(EPOCH);
    assertThat(job.getUpdatedAt()).isAfter(EPOCH);
    assertThat(job.getInputs()).isEqualTo(inputs);
    assertThat(job.getOutputs()).isEmpty();
    assertThat(job.getLocation()).isEqualTo(Optional.of(JOB_LOCATION));
    assertThat(job.getContext()).isEqualTo(JOB_CONTEXT);
    assertThat(job.getDescription()).isEqualTo(Optional.of(JOB_DESCRIPTION));
    assertThat(job.getLatestRun()).isEmpty();

    // (6) Create a run
    final RunMeta runMeta = RunMeta.builder().build();
    final Run run = client.createRun(NAMESPACE_NAME, JOB_NAME, runMeta);
    assertThat(run.getId()).isNotNull();
    assertThat(run.getCreatedAt()).isAfter(EPOCH);
    assertThat(run.getUpdatedAt()).isAfter(EPOCH);
    assertThat(run.getNominalStartTime()).isEmpty();
    assertThat(run.getNominalEndTime()).isEmpty();
    assertThat(run.getState()).isEqualTo(RunState.NEW);
    assertThat(run.getStartedAt()).isEmpty();
    assertThat(run.getEndedAt()).isEmpty();
    assertThat(run.getDurationMs()).isEmpty();
    assertThat(run.getArgs()).isEmpty();

    // (7) Start a run
    final Instant startedAt = newTimestamp().truncatedTo(ChronoUnit.MICROS);
    final Run runStarted = client.markRunAsRunning(run.getId(), startedAt);
    assertThat(runStarted.getId()).isEqualTo(run.getId());
    assertThat(runStarted.getCreatedAt()).isAfter(EPOCH);
    assertThat(runStarted.getUpdatedAt()).isAfter(EPOCH);
    assertThat(runStarted.getNominalStartTime()).isEmpty();
    assertThat(runStarted.getNominalEndTime()).isEmpty();
    assertThat(runStarted.getState()).isEqualTo(RunState.RUNNING);
    assertThat(runStarted.getStartedAt().get()).isEqualTo(startedAt);
    assertThat(runStarted.getEndedAt()).isEmpty();
    assertThat(runStarted.getDurationMs()).isEmpty();
    assertThat(runStarted.getArgs()).isEmpty();

    // (8) Modify context in job metadata to create new job version; the version
    //     will be linked to the provided run ID
    final ImmutableMap<String, String> modifiedJobContext =
        new ImmutableMap.Builder<String, String>()
            .putAll(JOB_CONTEXT)
            .put("key0", "value0")
            .build();
    final JobMeta jobMetaWithRunId =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(inputs)
            .outputs(outputs)
            .location(JOB_LOCATION)
            .context(modifiedJobContext)
            .description(JOB_DESCRIPTION)
            .runId(runStarted.getId())
            .build();
    final Job jobWithNewVersion = client.createJob(NAMESPACE_NAME, JOB_NAME, jobMetaWithRunId);
    assertThat(jobWithNewVersion.getId()).isEqualTo(JOB_ID);
    assertThat(jobWithNewVersion.getType()).isEqualTo(JOB_TYPE);
    assertThat(jobWithNewVersion.getName()).isEqualTo(JOB_NAME);
    assertThat(jobWithNewVersion.getCreatedAt()).isAfter(EPOCH);
    assertThat(jobWithNewVersion.getUpdatedAt()).isAfter(EPOCH);
    assertThat(jobWithNewVersion.getInputs()).isEqualTo(inputs);
    assertThat(jobWithNewVersion.getOutputs()).isEqualTo(outputs);
    assertThat(jobWithNewVersion.getLocation()).isEqualTo(Optional.of(JOB_LOCATION));
    assertThat(jobWithNewVersion.getContext()).isEqualTo(modifiedJobContext);
    assertThat(jobWithNewVersion.getDescription()).isEqualTo(Optional.of(JOB_DESCRIPTION));
    assertThat(jobWithNewVersion.getLatestRun().get().getId()).isEqualTo(runStarted.getId());

    // (9) Complete a run
    final Instant endedAt = newTimestamp().truncatedTo(ChronoUnit.MICROS);
    final Run runCompleted = client.markRunAsCompleted(run.getId(), endedAt);
    assertThat(runCompleted.getId()).isEqualTo(run.getId());
    assertThat(runCompleted.getCreatedAt()).isAfter(EPOCH);
    assertThat(runCompleted.getUpdatedAt()).isAfter(EPOCH);
    assertThat(runCompleted.getNominalStartTime()).isEmpty();
    assertThat(runCompleted.getNominalEndTime()).isEmpty();
    assertThat(runCompleted.getState()).isEqualTo(RunState.COMPLETED);
    assertThat(runCompleted.getStartedAt().get()).isEqualTo(startedAt);
    assertThat(runCompleted.getEndedAt().get()).isEqualTo(endedAt);
    assertThat(runCompleted.getDurationMs()).isNotEmpty();
    assertThat(runCompleted.getArgs()).isEmpty();

    final Job completedJob = client.getJob(NAMESPACE_NAME, JOB_NAME);
    assertThat(completedJob.getOutputs()).isEqualTo(outputs);
  }

  @Test
  public void testApp_testLazyInputDataset() {
    String jobName = newJobName().getValue();

    // Create namespace for job
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // Create source for input / output db tables
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(STREAM_SOURCE_TYPE)
            .connectionUrl(STREAM_CONNECTION_URL)
            .description(STREAM_SOURCE_DESCRIPTION)
            .build();

    client.createSource(STREAM_SOURCE_NAME, sourceMeta);

    // Create job
    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();

    final Job job = client.createJob(NAMESPACE_NAME, jobName, jobMeta);

    // Create a run
    final RunMeta runMeta = RunMeta.builder().build();
    final Run run = client.createRun(NAMESPACE_NAME, jobName, runMeta);
    assertThat(run.getId()).isNotNull();
    assertThat(run.getCreatedAt()).isAfter(EPOCH);
    assertThat(run.getUpdatedAt()).isAfter(EPOCH);
    assertThat(run.getNominalStartTime()).isEmpty();
    assertThat(run.getNominalEndTime()).isEmpty();
    assertThat(run.getState()).isEqualTo(RunState.NEW);
    assertThat(run.getStartedAt()).isEmpty();
    assertThat(run.getEndedAt()).isEmpty();
    assertThat(run.getDurationMs()).isEmpty();
    assertThat(run.getArgs()).isEmpty();

    // Create datasets
    final ImmutableSet<DatasetId> inputs = newDatasetIdsWith(NAMESPACE_NAME, 2);
    for (final DatasetId input : inputs) {
      final DbTableMeta dbTableMeta =
          DbTableMeta.builder()
              .physicalName(input.getName())
              .sourceName(STREAM_SOURCE_NAME)
              .description(newDescription())
              .build();

      client.createDataset(input.getNamespace(), input.getName(), dbTableMeta);
    }

    final ImmutableSet<DatasetId> outputs = newDatasetIdsWith(NAMESPACE_NAME, 2);
    for (final DatasetId output : outputs) {
      final DbTableMeta dbTableMeta =
          DbTableMeta.builder()
              .physicalName(output.getName())
              .sourceName(STREAM_SOURCE_NAME)
              .runId(run.getId()) // with run ID in output dataset
              .description(newDescription())
              .build();

      client.createDataset(output.getNamespace(), output.getName(), dbTableMeta);
    }

    // Update job
    final JobMeta jobUpdateMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(inputs)
            .outputs(outputs)
            .runId(run.getId())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, jobName, jobUpdateMeta);

    // Assure datasets are associated
    Job finalJob = client.getJob(NAMESPACE_NAME, jobName);
    assertThat(finalJob.getInputs()).hasSize(2);
  }

  @Test
  public void testApp_listTags() {
    final Set<Tag> tags = client.listTags();
    assertThat(tags).containsExactlyInAnyOrder(PII, SENSITIVE);
  }

  @Test
  public void testApp_listRunOrder() {
    // Create namespace for runs
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // Create job
    String jobName = newJobName().getValue();
    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    final Job job = client.createJob(NAMESPACE_NAME, jobName, jobMeta);

    // create some runs to test ordering mechanics
    final RunMeta runMeta = RunMeta.builder().build();
    final Run run0 = client.createRun(NAMESPACE_NAME, jobName, runMeta);
    final Run run1 = client.createRunAndStart(NAMESPACE_NAME, jobName, runMeta);
    client.markRunAsCompleted(run1.getId());
    final Run run2 = client.createRunAndStart(NAMESPACE_NAME, jobName, runMeta);

    // assert that runs are in the correct order
    List<Run> runs = client.listRuns(NAMESPACE_NAME, jobName);
    assertThat(runs.get(0).getId()).isEqualTo(run2.getId());
    assertThat(runs.get(1).getId()).isEqualTo(run1.getId());
    assertThat(runs.get(2).getId()).isEqualTo(run0.getId());
  }

  @Test
  public void testApp_search() {
    createNamespace(NAMESPACE_NAME);
    createSource("my-source");

    final String datasetName = "public.mytable";
    final List<Field> fields =
        ImmutableList.of(
            Field.builder().name("a").type("INTEGER").build(),
            Field.builder().name("b").type("TIMESTAMP").build());
    final DbTableMeta dbTableMeta =
        DbTableMeta.builder()
            .physicalName(datasetName)
            .sourceName("my-source")
            .fields(fields)
            .build();

    client.createDataset(NAMESPACE_NAME, datasetName, dbTableMeta);

    final String query = "mytable";
    final SearchResults searchResults = client.search(query);
    assertThat(searchResults.getTotalCount()).isEqualTo(1);

    final SearchResult result = searchResults.getResults().get(0);
    assertThat(result.getType()).isEqualTo(SearchResult.ResultType.DATASET);
    assertThat(result.getName()).isEqualTo(datasetName);
  }

  @Test
  public void testApp_getJob() throws SQLException {
    Jdbi jdbi =
        Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());
    createNamespace(NAMESPACE_NAME);

    // Create job
    String jobName = newJobName().getValue();
    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    final Job originalJob = client.createJob(NAMESPACE_NAME, jobName, jobMeta);

    String targetJobName = newJobName().getValue();
    final JobMeta targetJobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    final Job targetJob = client.createJob(NAMESPACE_NAME, targetJobName, targetJobMeta);

    JobDao jobDao = jdbi.onDemand(JobDao.class);
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Optional<NamespaceRow> namespaceRow = namespaceDao.findNamespaceByName(NAMESPACE_NAME);
    Optional<JobRow> originalJobRow = jobDao.findJobByNameAsRow(NAMESPACE_NAME, jobName);
    Optional<JobRow> targetJobRow = jobDao.findJobByNameAsRow(NAMESPACE_NAME, targetJobName);
    PGobject inputs = new PGobject();
    inputs.setType("json");
    inputs.setValue("[]");
    originalJobRow.ifPresent(
        j -> {
          jobDao.upsertJob(
              j.getUuid(),
              JobType.valueOf(JOB_TYPE.name()),
              Instant.now(),
              namespaceRow.get().getUuid(),
              NAMESPACE_NAME,
              jobName,
              JOB_DESCRIPTION,
              j.getJobContextUuid().orElse(null),
              JOB_LOCATION.toString(),
              targetJobRow.get().getUuid(),
              inputs);
        });

    Job job = client.getJob(NAMESPACE_NAME, jobName);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("namespace", NAMESPACE_NAME)
        .hasFieldOrPropertyWithValue("name", targetJobName);
  }

  @Test
  public void testApp_getJobWithFQNFromParent() throws SQLException {
    Jdbi jdbi =
        Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin());
    createNamespace(NAMESPACE_NAME);

    // Create job
    String jobName = newJobName().getValue();
    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    final Job originalJob = client.createJob(NAMESPACE_NAME, jobName, jobMeta);

    String parentJobName = newJobName().getValue();
    final JobMeta parentJobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    final Job parentJob = client.createJob(NAMESPACE_NAME, parentJobName, parentJobMeta);

    JobDao jobDao = jdbi.onDemand(JobDao.class);
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Optional<NamespaceRow> namespaceRow = namespaceDao.findNamespaceByName(NAMESPACE_NAME);
    if (namespaceRow.isEmpty()) {
      throw new AssertionError("Couldn't find expected namespace row");
    }
    Optional<JobRow> originalJobRow = jobDao.findJobByNameAsRow(NAMESPACE_NAME, jobName);
    if (originalJobRow.isEmpty()) {
      throw new AssertionError("Couldn't find job row we just inserted");
    }
    Optional<JobRow> parentJobRow = jobDao.findJobByNameAsRow(NAMESPACE_NAME, parentJobName);
    if (parentJobRow.isEmpty()) {
      throw new AssertionError("Couldn't find parent job we just inserted");
    }
    PGobject inputs = new PGobject();
    inputs.setType("json");
    inputs.setValue("[]");
    JobRow jobRow = originalJobRow.get();
    JobRow targetJobRow =
        jobDao.upsertJob(
            UUID.randomUUID(),
            parentJobRow.get().getUuid(),
            JobType.valueOf(jobRow.getType()),
            Instant.now(),
            namespaceRow.get().getUuid(),
            namespaceRow.get().getName(),
            jobRow.getName(),
            jobRow.getDescription().orElse(null),
            jobRow.getJobContextUuid().orElse(null),
            jobRow.getLocation(),
            null,
            inputs);
    // symlink the old job to point to the new one that has a parent uuid
    jobDao.upsertJob(
        jobRow.getUuid(),
        JobType.valueOf(JOB_TYPE.name()),
        Instant.now(),
        namespaceRow.get().getUuid(),
        NAMESPACE_NAME,
        jobName,
        JOB_DESCRIPTION,
        jobRow.getJobContextUuid().orElse(null),
        JOB_LOCATION.toString(),
        targetJobRow.getUuid(),
        inputs);

    Job job = client.getJob(NAMESPACE_NAME, jobName);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("namespace", NAMESPACE_NAME)
        .hasFieldOrPropertyWithValue("name", parentJobName + "." + jobName);
  }
}
