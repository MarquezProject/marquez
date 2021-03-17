package marquez;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.ModelGenerator.newConnectionUrl;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDbSourceType;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newFieldName;
import static marquez.common.models.ModelGenerator.newFieldType;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newSchemaLocation;
import static marquez.common.models.ModelGenerator.newSourceName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import marquez.client.MarquezClient;
import marquez.client.Utils;
import marquez.client.models.DatasetId;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.JobId;
import marquez.client.models.JobMeta;
import marquez.client.models.JobType;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.SourceMeta;
import marquez.client.models.StreamMeta;
import marquez.client.models.Tag;
import marquez.common.models.SourceType;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.ClassRule;

public abstract class BaseIntegrationTest {
  protected static final String CONFIG_FILE = "config.test.yml";
  protected static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);
  protected static final PostgresContainer POSTGRES = createMarquezPostgres();
  protected final HttpClient http2 = HttpClient.newBuilder().version(Version.HTTP_2).build();

  static {
    POSTGRES.start();
  }

  protected final URL baseUrl = Utils.toUrl("http://localhost:" + APP.getLocalPort());
  protected final MarquezClient client = MarquezClient.builder().baseUrl(baseUrl).build();
  // TAGS
  protected static final Tag PII = new Tag("PII", "Personally identifiable information");
  protected static final Tag SENSITIVE = new Tag("SENSITIVE", "Contains sensitive information");

  // NAMESPACE
  protected static final String NAMESPACE_NAME = newNamespaceName().getValue();
  protected static final String OWNER_NAME = newOwnerName().getValue();
  protected static final String NAMESPACE_DESCRIPTION = newDescription();

  // SOURCE
  protected static final String SOURCE_TYPE = newDbSourceType().getValue();
  protected static final String SOURCE_NAME = newSourceName().getValue();
  protected static final URI CONNECTION_URL = newConnectionUrl();
  protected static final String SOURCE_DESCRIPTION = newDescription();

  // DB TABLE DATASET
  protected static final String DB_TABLE_SOURCE_TYPE = SourceType.of("POSTGRESQL").getValue();
  protected static final String DB_TABLE_SOURCE_NAME = newSourceName().getValue();
  protected static final URI DB_TABLE_CONNECTION_URL =
      newConnectionUrlFor(SourceType.of("POSTGRESQL"));
  protected static final String DB_TABLE_SOURCE_DESCRIPTION = newDescription();
  protected static final DatasetId DB_TABLE_ID = newDatasetIdWith(NAMESPACE_NAME);
  protected static final String DB_TABLE_NAME = DB_TABLE_ID.getName();
  protected static final String DB_TABLE_PHYSICAL_NAME = DB_TABLE_NAME;
  protected static final String DB_TABLE_DESCRIPTION = newDescription();
  protected static final ImmutableList<Field> DB_TABLE_FIELDS =
      ImmutableList.of(newFieldWith(SENSITIVE.getName()), newField());
  protected static final Set<String> DB_TABLE_TAGS = ImmutableSet.of(PII.getName());
  protected static final DbTableMeta DB_TABLE_META =
      DbTableMeta.builder()
          .physicalName(DB_TABLE_PHYSICAL_NAME)
          .sourceName(DB_TABLE_SOURCE_NAME)
          .fields(DB_TABLE_FIELDS)
          .tags(DB_TABLE_TAGS)
          .description(DB_TABLE_DESCRIPTION)
          .build();

  // STREAM DATASET
  protected static final String STREAM_SOURCE_TYPE = SourceType.of("KAFKA").getValue();
  protected static final String STREAM_SOURCE_NAME = newSourceName().getValue();
  protected static final URI STREAM_CONNECTION_URL = newConnectionUrlFor(SourceType.of("KAFKA"));
  protected static final String STREAM_SOURCE_DESCRIPTION = newDescription();
  protected static final DatasetId STREAM_ID = newDatasetIdWith(NAMESPACE_NAME);
  protected static final String STREAM_NAME = STREAM_ID.getName();
  protected static final String STREAM_PHYSICAL_NAME = STREAM_NAME;
  protected static final URL STREAM_SCHEMA_LOCATION = newSchemaLocation();
  protected static final String STREAM_DESCRIPTION = newDescription();
  protected static final StreamMeta STREAM_META =
      StreamMeta.builder()
          .physicalName(STREAM_PHYSICAL_NAME)
          .sourceName(STREAM_SOURCE_NAME)
          .schemaLocation(STREAM_SCHEMA_LOCATION)
          .description(STREAM_DESCRIPTION)
          .build();
  // JOB
  protected static final String JOB_NAME = newJobName().getValue();
  protected static final JobId JOB_ID = new JobId(NAMESPACE_NAME, JOB_NAME);
  protected static final JobType JOB_TYPE = JobType.BATCH;
  protected static final URL JOB_LOCATION = newLocation();
  protected static final ImmutableMap<String, String> JOB_CONTEXT = newContext();
  protected static final String JOB_DESCRIPTION = newDescription();
  final JobMeta JOB_META =
      JobMeta.builder()
          .type(JOB_TYPE)
          .inputs(ImmutableSet.of())
          .outputs(ImmutableSet.of())
          .location(JOB_LOCATION)
          .context(JOB_CONTEXT)
          .description(JOB_DESCRIPTION)
          .build();

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class,
          CONFIG_FILE_PATH,
          ConfigOverride.config("db.url", POSTGRES.getJdbcUrl()),
          ConfigOverride.config("db.user", POSTGRES.getUsername()),
          ConfigOverride.config("db.password", POSTGRES.getPassword()));

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  protected static PostgresContainer createMarquezPostgres() {
    return PostgresContainer.create("marquez");
  }

  protected CompletableFuture<HttpResponse<String>> sendLineage(String body) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/lineage"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(body))
            .build();

    return http2.sendAsync(request, BodyHandlers.ofString());
  }

  protected void createSource(String sourceName) {
    final SourceMeta sourceMeta =
        SourceMeta.builder()
            .type(DB_TABLE_SOURCE_TYPE)
            .connectionUrl(DB_TABLE_CONNECTION_URL)
            .description(DB_TABLE_SOURCE_DESCRIPTION)
            .build();
    client.createSource(sourceName, sourceMeta);
  }

  protected void createNamespace(String namespaceName) {
    // (1) Create namespace for db table
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(NAMESPACE_DESCRIPTION).build();

    client.createNamespace(namespaceName, namespaceMeta);
  }

  protected static ImmutableSet<DatasetId> newDatasetIdsWith(
      final String namespaceName, final int limit) {
    return java.util.stream.Stream.generate(() -> newDatasetIdWith(namespaceName))
        .limit(limit)
        .collect(toImmutableSet());
  }

  protected static DatasetId newDatasetIdWith(final String namespaceName) {
    return new DatasetId(namespaceName, newDatasetName().getValue());
  }

  protected static Field newField() {
    return newFieldWith(ImmutableSet.of());
  }

  protected static Field newFieldWith(final String tag) {
    return newFieldWith(ImmutableSet.of(tag));
  }

  protected static Field newFieldWith(final ImmutableSet<String> tags) {
    return new Field(newFieldName().getValue(), newFieldType().name(), tags, newDescription());
  }
}
