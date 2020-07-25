package marquez;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.Instant.EPOCH;
import static marquez.Generator.newTimestamp;
import static marquez.common.models.ModelGenerator.newConnectionUrl;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newFieldName;
import static marquez.common.models.ModelGenerator.newFieldType;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;
import static marquez.service.models.ModelGenerator.newSchemaLocation;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import marquez.client.models.DatasetId;
import marquez.client.models.DbTable;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.Job;
import marquez.client.models.JobId;
import marquez.client.models.JobMeta;
import marquez.client.models.JobType;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.Source;
import marquez.client.models.SourceMeta;
import marquez.client.models.Stream;
import marquez.client.models.StreamMeta;
import marquez.client.models.Tag;
import marquez.common.models.SourceType;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class MarquezAppIntegrationTest {
  private static final String CONFIG_FILE = "config.test.yml";
  private static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

  private static final PostgresContainer POSTGRES = PostgresContainer.create();

  static {
    POSTGRES.start();
  }

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class,
          CONFIG_FILE_PATH,
          ConfigOverride.config("db.url", POSTGRES.getJdbcUrl()),
          ConfigOverride.config("db.user", POSTGRES.getUsername()),
          ConfigOverride.config("db.password", POSTGRES.getPassword()));

  // TAGS
  private static final Tag PII = new Tag("PII", "Personally identifiable information");
  private static final Tag SENSITIVE = new Tag("SENSITIVE", "Contains sensitive information");

  // NAMESPACE
  private static final String NAMESPACE_NAME = newNamespaceName().getValue();
  private static final String OWNER_NAME = newOwnerName().getValue();
  private static final String NAMESPACE_DESCRIPTION = newDescription();

  // SOURCE
  private static final String SOURCE_TYPE = newSourceType().name();
  private static final String SOURCE_NAME = newSourceName().getValue();
  private static final URI CONNECTION_URL = newConnectionUrl();
  private static final String SOURCE_DESCRIPTION = newDescription();

  // DB TABLE DATASET
  private static final String DB_TABLE_SOURCE_TYPE = SourceType.POSTGRESQL.name();
  private static final String DB_TABLE_SOURCE_NAME = newSourceName().getValue();
  private static final URI DB_TABLE_CONNECTION_URL = newConnectionUrlFor(SourceType.POSTGRESQL);
  private static final String DB_TABLE_SOURCE_DESCRIPTION = newDescription();
  private static final DatasetId DB_TABLE_ID = newDatasetIdWith(NAMESPACE_NAME);
  private static final String DB_TABLE_NAME = DB_TABLE_ID.getName();
  private static final String DB_TABLE_PHYSICAL_NAME = DB_TABLE_NAME;
  private static final String DB_TABLE_DESCRIPTION = newDescription();
  private static final ImmutableList<Field> DB_TABLE_FIELDS =
      ImmutableList.of(newFieldWith(SENSITIVE.getName()), newField());
  private static final Set<String> DB_TABLE_TAGS = ImmutableSet.of(PII.getName());

  // STREAM DATASET
  private static final String STREAM_SOURCE_TYPE = SourceType.KAFKA.name();
  private static final String STREAM_SOURCE_NAME = newSourceName().getValue();
  private static final URI STREAM_CONNECTION_URL = newConnectionUrlFor(SourceType.KAFKA);
  private static final String STREAM_SOURCE_DESCRIPTION = newDescription();
  private static final DatasetId STREAM_ID = newDatasetIdWith(NAMESPACE_NAME);
  private static final String STREAM_NAME = STREAM_ID.getName();
  private static final String STREAM_PHYSICAL_NAME = STREAM_NAME;
  private static final URL STREAM_SCHEMA_LOCATION = newSchemaLocation();
  private static final String STREAM_DESCRIPTION = newDescription();

  // JOB
  private static final String JOB_NAME = newJobName().getValue();
  private static final JobId JOB_ID = new JobId(NAMESPACE_NAME, JOB_NAME);
  private static final JobType JOB_TYPE = JobType.BATCH;
  private static final URL JOB_LOCATION = newLocation();
  private static final ImmutableMap<String, String> JOB_CONTEXT = newContext();
  private static final String JOB_DESCRIPTION = newDescription();

  private final URL baseUrl = Utils.toUrl("http://localhost:" + APP.getLocalPort() + "/api/v1");
  private final MarquezClient client = MarquezClient.builder().baseUrl(baseUrl).build();

  @Test
  public void testApp_createNamespace() {
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    final Namespace namespace = client.createNamespace(NAMESPACE_NAME, namespaceMeta);
    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(namespace.getCreatedAt()).isAfter(EPOCH);
    assertThat(namespace.getUpdatedAt()).isAfter(EPOCH);
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getDescription()).isEqualTo(Optional.of(NAMESPACE_DESCRIPTION));

    assertThat(
            client.listNamespaces().stream()
                .filter(other -> other.getName().equals(NAMESPACE_NAME))
                .count())
        .isEqualTo(1);
  }

  @Test
  public void testApp_createSource() {
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(SOURCE_TYPE)
            .connectionUrl(CONNECTION_URL)
            .description(SOURCE_DESCRIPTION)
            .build();

    final Source source = client.createSource(SOURCE_NAME, sourceMeta);
    assertThat(source.getType()).isEqualTo(SOURCE_TYPE);
    assertThat(source.getName()).isEqualTo(SOURCE_NAME);
    assertThat(source.getCreatedAt()).isAfter(EPOCH);
    assertThat(source.getUpdatedAt()).isAfter(EPOCH);
    assertThat(source.getConnectionUrl()).isEqualTo(CONNECTION_URL);
    assertThat(source.getDescription()).isEqualTo(Optional.of(SOURCE_DESCRIPTION));

    assertThat(
            client.listSources().stream()
                .filter(other -> other.getName().equals(SOURCE_NAME))
                .count())
        .isEqualTo(1);
  }

  @Test
  public void testApp_createDbTable() {
    // (1) Create namespace for db table
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(NAMESPACE_NAME, namespaceMeta);

    // (2) Create source for db table
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(DB_TABLE_SOURCE_TYPE)
            .connectionUrl(DB_TABLE_CONNECTION_URL)
            .description(DB_TABLE_SOURCE_DESCRIPTION)
            .build();

    client.createSource(DB_TABLE_SOURCE_NAME, sourceMeta);

    // (3) Add db table to namespace and associate with source
    final DbTableMeta dbTableMeta =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    final DbTable dbTable =
        (DbTable) client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, dbTableMeta);
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
    final StreamMeta streamMeta =
        StreamMeta.builder()
            .physicalName(STREAM_PHYSICAL_NAME)
            .sourceName(STREAM_SOURCE_NAME)
            .schemaLocation(STREAM_SCHEMA_LOCATION)
            .description(STREAM_DESCRIPTION)
            .build();

    final Stream stream = (Stream) client.createDataset(NAMESPACE_NAME, STREAM_NAME, streamMeta);
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
    assertThat(job.getOutputs()).isEqualTo(outputs);
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
    final Instant startedAt = newTimestamp();
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

    // (8) Complete a run
    final Instant endedAt = newTimestamp();
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
  }

  @Test
  public void testApp_listTags() {
    final Set<Tag> tags = client.listTags();
    assertThat(tags).containsExactlyInAnyOrder(PII, SENSITIVE);
  }

  private static ImmutableSet<DatasetId> newDatasetIdsWith(
      final String namespaceName, final int limit) {
    return java.util.stream.Stream.generate(() -> newDatasetIdWith(namespaceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  private static DatasetId newDatasetIdWith(final String namespaceName) {
    return new DatasetId(namespaceName, newDatasetName().getValue());
  }

  private static Field newField() {
    return newFieldWith(ImmutableSet.of());
  }

  private static Field newFieldWith(final String tag) {
    return newFieldWith(ImmutableSet.of(tag));
  }

  private static Field newFieldWith(final ImmutableSet<String> tags) {
    return new Field(newFieldName().getValue(), newFieldType().name(), tags, newDescription());
  }
}
