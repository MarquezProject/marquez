/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newConnectionUrlFor;
import static marquez.common.models.CommonModelGenerator.newContext;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDbSourceType;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newFieldName;
import static marquez.common.models.CommonModelGenerator.newFieldType;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.CommonModelGenerator.newSchemaLocation;
import static marquez.common.models.CommonModelGenerator.newSourceName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {
  protected static final String CONFIG_FILE = "config.test.yml";
  protected static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

  @Container protected static final PostgresContainer POSTGRES = createMarquezPostgres();

  // TAGS
  protected static final Tag PII = new Tag("PII", "Personally identifiable information");
  protected static final Tag SENSITIVE = new Tag("SENSITIVE", "Contains sensitive information");

  // NAMESPACE
  protected static String NAMESPACE_NAME;
  protected static String OWNER_NAME;
  protected static String NAMESPACE_DESCRIPTION;

  // SOURCE
  protected static String SOURCE_TYPE;
  protected static String SOURCE_NAME;
  protected static URI CONNECTION_URL;
  protected static String SOURCE_DESCRIPTION;

  // DB TABLE DATASET
  protected static String DB_TABLE_SOURCE_TYPE;
  protected static String DB_TABLE_SOURCE_NAME;
  protected static URI DB_TABLE_CONNECTION_URL;
  protected static String DB_TABLE_SOURCE_DESCRIPTION;
  protected static DatasetId DB_TABLE_ID;
  protected static String DB_TABLE_NAME;
  protected static String DB_TABLE_PHYSICAL_NAME;
  protected static String DB_TABLE_DESCRIPTION;
  protected static ImmutableList<Field> DB_TABLE_FIELDS;
  protected static Set<String> DB_TABLE_TAGS;
  protected static DbTableMeta DB_TABLE_META;

  // STREAM DATASET
  protected static String STREAM_SOURCE_TYPE;
  protected static String STREAM_SOURCE_NAME;
  protected static URI STREAM_CONNECTION_URL;
  protected static String STREAM_SOURCE_DESCRIPTION;
  protected static DatasetId STREAM_ID;
  protected static String STREAM_NAME;
  protected static String STREAM_PHYSICAL_NAME;
  protected static URL STREAM_SCHEMA_LOCATION;
  protected static String STREAM_DESCRIPTION;
  protected static StreamMeta STREAM_META;
  // JOB
  protected static String JOB_NAME;
  protected static JobId JOB_ID;
  protected static JobType JOB_TYPE;
  protected static URL JOB_LOCATION;
  protected static ImmutableMap<String, String> JOB_CONTEXT;
  protected static String JOB_DESCRIPTION;
  protected static JobMeta JOB_META;

  public static DropwizardAppExtension<MarquezConfig> APP;
  protected final HttpClient http2 = HttpClient.newBuilder().version(Version.HTTP_2).build();

  protected URL baseUrl;
  protected MarquezClient client;

  @BeforeAll
  protected static void setupAll() throws Exception {
    NAMESPACE_NAME = newNamespaceName().getValue();
    OWNER_NAME = newOwnerName().getValue();
    NAMESPACE_DESCRIPTION = newDescription();

    SOURCE_TYPE = newDbSourceType().getValue();
    SOURCE_NAME = newSourceName().getValue();
    CONNECTION_URL = newConnectionUrl();
    SOURCE_DESCRIPTION = newDescription();

    DB_TABLE_SOURCE_TYPE = SourceType.of("POSTGRESQL").getValue();
    DB_TABLE_SOURCE_NAME = newSourceName().getValue();
    DB_TABLE_CONNECTION_URL = newConnectionUrlFor(SourceType.of("POSTGRESQL"));
    DB_TABLE_SOURCE_DESCRIPTION = newDescription();
    DB_TABLE_ID = newDatasetIdWith(NAMESPACE_NAME);
    DB_TABLE_NAME = DB_TABLE_ID.getName();
    DB_TABLE_PHYSICAL_NAME = DB_TABLE_NAME;
    DB_TABLE_DESCRIPTION = newDescription();
    DB_TABLE_FIELDS = ImmutableList.of(newFieldWith(SENSITIVE.getName()), newField());
    DB_TABLE_TAGS = ImmutableSet.of(PII.getName());
    DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    STREAM_SOURCE_TYPE = SourceType.of("KAFKA").getValue();
    STREAM_SOURCE_NAME = newSourceName().getValue();
    STREAM_CONNECTION_URL = newConnectionUrlFor(SourceType.of("KAFKA"));
    STREAM_SOURCE_DESCRIPTION = newDescription();
    STREAM_ID = newDatasetIdWith(NAMESPACE_NAME);
    STREAM_NAME = STREAM_ID.getName();
    STREAM_PHYSICAL_NAME = STREAM_NAME;
    STREAM_SCHEMA_LOCATION = newSchemaLocation();
    STREAM_DESCRIPTION = newDescription();
    STREAM_META =
        StreamMeta.builder()
            .physicalName(STREAM_PHYSICAL_NAME)
            .sourceName(STREAM_SOURCE_NAME)
            .schemaLocation(STREAM_SCHEMA_LOCATION)
            .description(STREAM_DESCRIPTION)
            .build();

    JOB_NAME = newJobName().getValue();
    JOB_ID = new JobId(NAMESPACE_NAME, JOB_NAME);
    JOB_TYPE = JobType.BATCH;
    JOB_LOCATION = newLocation();
    JOB_CONTEXT = newContext();
    JOB_DESCRIPTION = newDescription();
    JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(ImmutableSet.of())
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();

    APP =
        new DropwizardAppExtension<>(
            MarquezApp.class,
            CONFIG_FILE_PATH,
            ConfigOverride.config("db.url", POSTGRES.getJdbcUrl()),
            ConfigOverride.config("db.user", POSTGRES.getUsername()),
            ConfigOverride.config("db.password", POSTGRES.getPassword()));

    APP.before();
  }

  @BeforeEach
  protected void setupApp() {
    baseUrl = Utils.toUrl("http://localhost:" + APP.getLocalPort());
    client = MarquezClient.builder().baseUrl(baseUrl).build();
  }

  @AfterAll
  protected static void cleanUp() {
    APP.after();
  }

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

  protected CompletableFuture<HttpResponse<String>> fetchLineage(String nodeId) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/api/v1/lineage?nodeId=" + nodeId))
            .header("Content-Type", "application/json")
            .GET()
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
    return new Field(newFieldName().getValue(), newFieldType(), tags, newDescription());
  }
}
