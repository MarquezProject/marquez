/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.dropwizard.util.Resources;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineage.RunEvent;
import io.openlineage.client.OpenLineage.RunEvent.EventType;
import io.openlineage.client.OpenLineage.RunFacet;
import io.openlineage.client.OpenLineage.RunFacetsBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import marquez.api.JdbiUtils;
import marquez.client.MarquezClient;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetVersion;
import marquez.client.models.Job;
import marquez.client.models.JobId;
import marquez.client.models.JobVersion;
import marquez.client.models.LineageEvent;
import marquez.client.models.Run;
import marquez.common.Utils;
import marquez.db.LineageTestUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;

@org.junit.jupiter.api.Tag("IntegrationTests")
@Slf4j
public class OpenLineageIntegrationTest extends BaseIntegrationTest {

  public static String EVENT_REQUIRED = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";
  public static String NULL_NOMINAL_END_TIME = "open_lineage/null_nominal_end_time.json";
  public static String EVENT_NAMESPACE_NAMING = "open_lineage/event_namespace_naming.json";
  public static String EVENT_DATASET_EVENT = "open_lineage/event_dataset_event.json";
  public static String EVENT_JOB_EVENT = "open_lineage/event_job_event.json";
  public static String EVENT_WITHOUT_SCHEMA_URL = "open_lineage/event_without_schema_url.json";

  public static String RUN_EVENT_SCHEMA_URL =
      "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/RunEvent";

  public static List<String> data() {
    return Arrays.asList(
        EVENT_FULL,
        EVENT_SIMPLE,
        EVENT_WITHOUT_SCHEMA_URL,
        EVENT_REQUIRED,
        EVENT_UNICODE,
        // FIXME: A very large event fails the test.
        // EVENT_LARGE,
        NULL_NOMINAL_END_TIME,
        EVENT_NAMESPACE_NAMING);
  }

  @AfterEach
  public void tearDown() {
    JdbiUtils.cleanDatabase(
        Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()));
  }

  @Test
  public void testSendOpenLineageBadArgument() throws IOException {
    // Namespaces can't have emojis, so this will get rejected
    String badNamespace =
        "sqlserver://myhost:3342;user=auser;password=\uD83D\uDE02\uD83D\uDE02\uD83D\uDE02;database=TheDatabase";
    marquez.service.models.LineageEvent event =
        marquez.service.models.LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new marquez.service.models.LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new marquez.service.models.LineageEvent.Job("namespace", "job_name", null))
            .inputs(
                List.of(
                    new marquez.service.models.LineageEvent.Dataset(
                        badNamespace, "the_table", null)))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    final CompletableFuture<Integer> resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(400);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        // input dataset has null name
        "{\"eventTime\": \"2021-11-03T10:53:52.427343\", \"eventType\": \"COMPLETE\", "
            + "\"inputs\": [{\"facets\": {}, \"name\": null, \"namespace\": \"testing_namespace_1\"}], "
            + "\"job\": {\"facets\": {}, \"name\": \"testing_name_1\", \"namespace\": \"testing_namespace_1\"}, "
            + "\"outputs\": [], \"producer\": \"me\", \"run\": {\"facets\": {}, \"runId\": \"dae0d60a-6010-4c37-980e-c5270f5a6be4\"}}",

        // output dataset schema has invalid fields (actual production issue :) ).
        "{\"eventTime\": \"2021-11-03T10:53:52.427343\", \"eventType\": \"COMPLETE\", \"inputs\": [{\"facets\": {}, \"name\": \"OPEN_LINEAGE_DEMO.DEMO.SOURCE_TABLE_1\", \"namespace\": \"testing_namespace_1\"}], "
            + "\"job\": {\"facets\": {}, \"name\": \"testing_name_1\", \"namespace\": \"testing_namespace_1\"}, "
            + "\"outputs\": [{\"facets\": {\"schema\": {\"_producer\": \"https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client\", \"_schemaURL\": \"https://openlineage.io/spec/facets/1-0-0/DataQualityAssertionsDatasetFacet.json\", "
            + "                                 \"fields\": [{\"assertion\": \"a\", \"success\": true}]}}, \"name\": \"OPEN_LINEAGE_DEMO.DEMO.SOURCE_TABLE_1\", \"namespace\": \"testing_namespace_1\"}], "
            + "\"producer\": \"me\", \"run\": {\"facets\": {}, \"runId\": \"dae0d60a-6010-4c37-980e-c5270f5a6be4\"}}",

        // job has a null name
        "{\"eventTime\": \"2021-11-03T10:53:52.427343\", \"eventType\": \"COMPLETE\", \"inputs\": [{\"facets\": {}, \"name\": \"OPEN_LINEAGE_DEMO.DEMO.SOURCE_TABLE_1\", \"namespace\": \"testing_namespace_1\"}], "
            + "\"job\": {\"facets\": {}, \"name\": null, \"namespace\": \"testing_namespace_1\"}, "
            + "\"outputs\": [], \"producer\": \"me\", \"run\": {\"facets\": {}, \"runId\": \"dae0d60a-6010-4c37-980e-c5270f5a6be4\"}}",

        // run has a null id
        "{\"eventTime\": \"2021-11-03T10:53:52.427343\", \"eventType\": \"COMPLETE\", \"inputs\": [{\"facets\": {}, \"name\": \"OPEN_LINEAGE_DEMO.DEMO.SOURCE_TABLE_1\", \"namespace\": \"testing_namespace_1\"}], "
            + "\"job\": {\"facets\": {}, \"name\": \"testing_name_1\", \"namespace\": \"testing_namespace_1\"}, "
            + "\"outputs\": [], \"producer\": \"me\", \"run\": {\"facets\": {}, \"runId\": null}}",

        // parent run facet has an empty {} run section
        "{\"eventTime\": \"2021-11-03T10:53:52.427343\", \"eventType\": \"COMPLETE\", \"inputs\": [{\"facets\": {}, \"name\": \"OPEN_LINEAGE_DEMO.DEMO.SOURCE_TABLE_1\", \"namespace\": \"testing_namespace_1\"}], "
            + "\"job\": {\"facets\": {}, \"name\": \"testing_name_1\", \"namespace\": \"testing_namespace_1\"}, "
            + "\"outputs\": [], \"producer\": \"me\", \"run\": {\"facets\": { \"parent\": "
            + "{ \"_producer\": \"me\", \"_schemaURL\": \"https://me\", \"run\": {}, \"job\": { \"namespace\": \"my-scheduler-namespace\", \"name\": \"myjob.mytask\"} }},"
            + "\"runId\": \"dae0d60a-6010-4c37-980e-c5270f5a6be4\"}}",
      })
  public void testSendOpenLineageEventFailsValidation(String eventBody) throws IOException {
    final CompletableFuture<Integer> resp =
        this.sendLineage(eventBody)
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, err) -> {
                  if (err != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    assertThat(resp.join()).isEqualTo(422);
  }

  @Test
  public void testSendOpenLineageEventFailsJsonProcessing() throws IOException {
    String eventWithIncorrectEventTimeFormat =
        "{\"eventTime\": \"2021-11-03\", \"eventType\": \"START\", \"inputs\": [], \"job\": {\"facets\": {}, \"name\": \"job\", \"namespace\": \"openlineage\"}, \"outputs\": [], \"run\": {\"facets\": {}, \"runId\": \"123e4567-e89b-12d3-a456-426614174000\"}}";

    final CompletableFuture<String> resp =
        this.sendLineage(eventWithIncorrectEventTimeFormat)
            .thenApply(HttpResponse::body)
            .whenComplete(
                (val, err) -> {
                  if (err != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    assertThat(resp.join())
        .contains(
            "Cannot deserialize value of type `java.time.ZonedDateTime` from String \\\"2021-11-03\\\"");
  }

  @Test
  public void testGetLineageForNonExistentDataset() {
    CompletableFuture<Integer> response =
        this.fetchLineage("dataset:Imadethisup:andthistoo")
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    assertThat(response.join()).isEqualTo(404);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegration()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String task2Name = "task2";
    String dagName = "the_dag";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task2Name,
            NAMESPACE_NAME);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationWithParentRunFacet()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String task2Name = "task2";
    String dagName = "the_dag";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    // the older airflow integration reported parentRun instead of parent. We support this as an
    // alias for compatibility
    RunFacet parent = airflowTask1.getRun().getFacets().getAdditionalProperties().remove("parent");
    airflowTask1.getRun().getFacets().getAdditionalProperties().put("parentRun", parent);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task2Name,
            NAMESPACE_NAME);
    parent = airflowTask2.getRun().getFacets().getAdditionalProperties().remove("parent");
    airflowTask2.getRun().getFacets().getAdditionalProperties().put("parentRun", parent);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationWithParentAndParentRunFacet()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String task2Name = "task2";
    String dagName = "the_dag";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    // the older airflow integration reported parentRun instead of parent. The new integration
    // reports both. They are the same in the airflow integration, but this test verifies we handle
    // the "parentRun" field first.
    // It would be preferable to prioritize the "parent" field, but it seems Jackson prefers the
    // alias first.
    RunFacet parent = airflowTask1.getRun().getFacets().getAdditionalProperties().get("parent");
    RunFacet newParent = ol.newRunFacet();
    Map<String, Object> runFacetProps = newParent.getAdditionalProperties();
    runFacetProps.put("run", parent.getAdditionalProperties().get("run"));
    runFacetProps.put(
        "job", ImmutableMap.of("name", "a_new_dag", "namespace", "incorrect_namespace"));
    airflowTask1.getRun().getFacets().getAdditionalProperties().put("parentRun", parent);
    airflowTask1.getRun().getFacets().getAdditionalProperties().put("parent", newParent);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task2Name,
            NAMESPACE_NAME);
    parent = airflowTask2.getRun().getFacets().getAdditionalProperties().get("parent");
    newParent = ol.newRunFacet();
    runFacetProps = newParent.getAdditionalProperties();
    runFacetProps.put("run", parent.getAdditionalProperties().get("run"));
    runFacetProps.put(
        "job", ImmutableMap.of("name", "a_new_dag", "namespace", "incorrect_namespace"));
    airflowTask2.getRun().getFacets().getAdditionalProperties().put("parentRun", parent);
    airflowTask2.getRun().getFacets().getAdditionalProperties().put("parent", newParent);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationWithParentOnStartEventOnly()
      throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String dagName = "the_dag";
    RunEvent event1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);
    ObjectMapper mapper = Utils.newObjectMapper();
    JsonNode eventOneJson = mapper.valueToTree(event1);
    ((ObjectNode) eventOneJson).set("eventType", new TextNode("START"));

    event1.getRun().getFacets().getAdditionalProperties().remove("parent");
    CompletableFuture.allOf(
            sendLineage(mapper.writeValueAsString(eventOneJson))
                .thenCompose(
                    r -> {
                      try {
                        return sendLineage(mapper.writeValueAsString(event1));
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                    }))
        .get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList)
        .isNotEmpty()
        .hasSize(1)
        .first()
        .extracting("startedAt", as(InstanceOfAssertFactories.OPTIONAL))
        .get()
        .isNotNull();
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowMissingParentForExistingJob()
      throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String dagName = "the_dag";
    RunEvent event1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);
    ObjectMapper mapper = Utils.newObjectMapper();

    RunEvent event2 =
        createAirflowRunEvent(
            ol,
            endOfHour,
            endOfHour.plusHours(1),
            null,
            null,
            dagName + "." + task1Name,
            NAMESPACE_NAME);
    CompletableFuture.allOf(
            sendLineage(mapper.writeValueAsString(event1))
                .thenCompose(
                    r -> {
                      try {
                        return sendLineage(mapper.writeValueAsString(event2));
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                    }))
        .get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(runsList)
        .isNotEmpty()
        .hasSize(2)
        .extracting(Run::getId)
        .containsExactlyInAnyOrder(
            event1.getRun().getRunId().toString(), event2.getRun().getRunId().toString());
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowAddParentForExistingJob()
      throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String dagName = "the_dag";
    RunEvent event1 =
        createAirflowRunEvent(
            ol, startOfHour, endOfHour, null, null, dagName + "." + task1Name, NAMESPACE_NAME);
    ObjectMapper mapper = Utils.newObjectMapper();

    RunEvent event2 =
        createAirflowRunEvent(
            ol,
            endOfHour,
            endOfHour.plusHours(1),
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);
    CompletableFuture.allOf(
            sendLineage(mapper.writeValueAsString(event1))
                .thenCompose(
                    r -> {
                      try {
                        return sendLineage(mapper.writeValueAsString(event2));
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                    }))
        .get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName)
        .hasFieldOrPropertyWithValue("simpleName", task1Name);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(runsList)
        .isNotEmpty()
        .hasSize(2)
        .extracting(Run::getId)
        .containsExactlyInAnyOrder(
            event1.getRun().getRunId().toString(), event2.getRun().getRunId().toString());
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowHandlesParentForEventsOutOfOrder()
      throws ExecutionException, InterruptedException, TimeoutException, JsonProcessingException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String dagName = "the_dag";
    ObjectMapper mapper = Utils.newObjectMapper();
    RunEvent event =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    // first event is the COMPLETE event and is missing the parent facet
    JsonNode event1 = mapper.valueToTree(event);
    ((ObjectNode) event1.get("run").get("facets")).remove("parent");

    // the second event is the start
    JsonNode event2 =
        ((ObjectNode) mapper.valueToTree(event)).set("eventType", new TextNode("START"));

    CompletableFuture.allOf(
            sendLineage(mapper.writeValueAsString(event1))
                .thenCompose(
                    r -> {
                      try {
                        return sendLineage(mapper.writeValueAsString(event2));
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                    }))
        .get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(runsList)
        .isNotEmpty()
        .hasSize(1)
        .extracting(Run::getId)
        .containsExactlyInAnyOrder(event1.get("run").get("runId").asText());
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationWithDagNameWithDot()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String task2Name = "task2";
    String dagName = "the.dag";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task2Name,
            NAMESPACE_NAME);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationWithTaskGroup()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task_group.task1";
    String task2Name = "task_group.task2";
    String dagName = "dag_with_task_group";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task2Name,
            NAMESPACE_NAME);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyOldAirflowIntegration()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);

    // The old airflow integration used the Dag's Airflow run_id (its scheduled or manual execution
    // time) as the runid in the ParentRunFacet. The newer integration calculates a legitimate UUID
    // for the run id so we can record a run of a distinct job. We emulate that calculation in
    // marquez.
    String airflowParentRunId = "scheduled__2022-04-25T00:20:00+00:00";
    String task1Name = "task1";
    String task2Name = "task2";
    String dagName = "the_dag";

    // the old integration also used the fully qualified task name as the parent job name
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName + "." + task1Name,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName + "." + task2Name,
            dagName + "." + task2Name,
            NAMESPACE_NAME);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("simpleName", task1Name)
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
    UUID parentRunUuid = Utils.toNameBasedUuid(NAMESPACE_NAME, dagName, airflowParentRunId);
    assertThat(runsList.get(0)).hasFieldOrPropertyWithValue("id", parentRunUuid.toString());

    List<Run> taskRunsList = client.listRuns(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(taskRunsList).hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchyAirflowIntegrationConflictingRunUuid()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "task1";
    String dagName = "reused_dag_name";

    // two dag runs with different namespaces - should result in two distinct jobs
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    String secondNamespace = "another_namespace";
    RunEvent airflowTask2 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            secondNamespace);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, airflowTask2);
    future.get(5, TimeUnit.SECONDS);

    Job job = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(job)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job parentJob = client.getJob(secondNamespace, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(secondNamespace, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(secondNamespace, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  public void testOpenLineageJobHierarchySparkAndAirflow()
      throws ExecutionException, InterruptedException, TimeoutException {
    OpenLineage ol = new OpenLineage(URI.create("http://openlineage.test.com/"));
    ZonedDateTime startOfHour =
        Instant.now()
            .atZone(LineageTestUtils.LOCAL_ZONE)
            .with(ChronoField.MINUTE_OF_HOUR, 0)
            .with(ChronoField.SECOND_OF_MINUTE, 0);
    ZonedDateTime endOfHour = startOfHour.plusHours(1);
    String airflowParentRunId = UUID.randomUUID().toString();
    String task1Name = "startSparkJob";
    String sparkTaskName = "theSparkJob";
    String dagName = "the_dag";
    RunEvent airflowTask1 =
        createAirflowRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowParentRunId,
            dagName,
            dagName + "." + task1Name,
            NAMESPACE_NAME);

    RunEvent sparkTask =
        createRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowTask1.getRun().getRunId().toString(),
            dagName + "." + task1Name,
            dagName + "." + task1Name + "." + sparkTaskName,
            Optional.empty(),
            NAMESPACE_NAME);

    CompletableFuture<Integer> future = sendAllEvents(airflowTask1, sparkTask);
    future.get(5, TimeUnit.SECONDS);

    Job airflowTask = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(airflowTask)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name))
        .hasFieldOrPropertyWithValue("simpleName", task1Name)
        .hasFieldOrPropertyWithValue("parentJobName", dagName);

    Job sparkJob = client.getJob(NAMESPACE_NAME, dagName + "." + task1Name + "." + sparkTaskName);
    assertThat(sparkJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue(
            "id", new JobId(NAMESPACE_NAME, dagName + "." + task1Name + "." + sparkTaskName))
        .hasFieldOrPropertyWithValue("simpleName", sparkTaskName)
        .hasFieldOrPropertyWithValue("parentJobName", dagName + "." + task1Name);

    Job parentJob = client.getJob(NAMESPACE_NAME, dagName);
    assertThat(parentJob)
        .isNotNull()
        .hasFieldOrPropertyWithValue("id", new JobId(NAMESPACE_NAME, dagName))
        .hasFieldOrPropertyWithValue("parentJobName", null);
    List<Run> runsList = client.listRuns(NAMESPACE_NAME, dagName);
    assertThat(runsList).isNotEmpty().hasSize(1);
  }

  @Test
  @SneakyThrows
  public void testSendEventAndGetItBack() {
    marquez.service.models.LineageEvent.Run run =
        new marquez.service.models.LineageEvent.Run(
            UUID.randomUUID().toString(),
            marquez.service.models.LineageEvent.RunFacet.builder().build());
    marquez.service.models.LineageEvent.Job job =
        marquez.service.models.LineageEvent.Job.builder()
            .namespace(NAMESPACE_NAME)
            .name(JOB_NAME)
            .build();
    marquez.service.models.LineageEvent.Dataset dataset =
        marquez.service.models.LineageEvent.Dataset.builder()
            .namespace(NAMESPACE_NAME)
            .name(DB_TABLE_NAME)
            .build();

    // We're losing zone info on write, so I have to UTC it here to compare later
    ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));

    final marquez.service.models.LineageEvent lineageEvent =
        marquez.service.models.LineageEvent.builder()
            .producer("testSendEventAndGetItBack")
            .eventType("COMPLETE")
            .run(run)
            .job(job)
            .eventTime(time)
            .inputs(Collections.emptyList())
            .outputs(Collections.singletonList(dataset))
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL))
            .build();

    final CompletableFuture<Integer> resp = sendEvent(lineageEvent);
    assertThat(resp.join()).isEqualTo(201);

    List<LineageEvent> events = client.listLineageEvents();

    assertThat(events.size()).isEqualTo(1);

    ObjectMapper mapper = Utils.getMapper();
    JsonNode prev = mapper.valueToTree(events.get(0));
    assertThat(prev).isEqualTo(mapper.valueToTree(lineageEvent));
  }

  @Test
  @SneakyThrows
  public void testFindEventIsSortedByTime() {
    marquez.service.models.LineageEvent.Run run =
        new marquez.service.models.LineageEvent.Run(
            UUID.randomUUID().toString(),
            marquez.service.models.LineageEvent.RunFacet.builder().build());
    marquez.service.models.LineageEvent.Job job =
        marquez.service.models.LineageEvent.Job.builder()
            .namespace(NAMESPACE_NAME)
            .name(JOB_NAME)
            .build();

    ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
    marquez.service.models.LineageEvent.Dataset dataset =
        marquez.service.models.LineageEvent.Dataset.builder()
            .namespace(NAMESPACE_NAME)
            .name(DB_TABLE_NAME)
            .build();

    marquez.service.models.LineageEvent.LineageEventBuilder builder =
        marquez.service.models.LineageEvent.builder()
            .producer("testFindEventIsSortedByTime")
            .run(run)
            .job(job)
            .inputs(Collections.emptyList())
            .outputs(Collections.singletonList(dataset))
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL));

    marquez.service.models.LineageEvent firstEvent =
        builder.eventTime(time).eventType("START").schemaURL(new URI(RUN_EVENT_SCHEMA_URL)).build();

    CompletableFuture<Integer> resp = sendEvent(firstEvent);
    assertThat(resp.join()).isEqualTo(201);

    marquez.service.models.LineageEvent secondEvent =
        builder
            .eventTime(time.plusSeconds(10))
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL))
            .eventType("COMPLETE")
            .build();

    resp = sendEvent(secondEvent);
    assertThat(resp.join()).isEqualTo(201);

    List<LineageEvent> rawEvents = client.listLineageEvents();

    assertThat(rawEvents.size()).isEqualTo(2);
    ObjectMapper mapper = Utils.getMapper();
    assertThat((JsonNode) mapper.valueToTree(firstEvent))
        .isEqualTo(mapper.valueToTree(rawEvents.get(1)));
    assertThat((JsonNode) mapper.valueToTree(secondEvent))
        .isEqualTo(mapper.valueToTree(rawEvents.get(0)));
  }

  @Test
  @SneakyThrows
  public void testFindEventIsSortedByTimeAsc() {
    marquez.service.models.LineageEvent.Run run =
        new marquez.service.models.LineageEvent.Run(
            UUID.randomUUID().toString(),
            marquez.service.models.LineageEvent.RunFacet.builder().build());
    marquez.service.models.LineageEvent.Job job =
        marquez.service.models.LineageEvent.Job.builder()
            .namespace(NAMESPACE_NAME)
            .name(JOB_NAME)
            .build();

    ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
    marquez.service.models.LineageEvent.Dataset dataset =
        marquez.service.models.LineageEvent.Dataset.builder()
            .namespace(NAMESPACE_NAME)
            .name(DB_TABLE_NAME)
            .build();

    marquez.service.models.LineageEvent.LineageEventBuilder builder =
        marquez.service.models.LineageEvent.builder()
            .producer("testFindEventIsSortedByTime")
            .run(run)
            .job(job)
            .inputs(Collections.emptyList())
            .outputs(Collections.singletonList(dataset))
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL));

    marquez.service.models.LineageEvent firstEvent =
        builder.eventTime(time).eventType("START").schemaURL(new URI(RUN_EVENT_SCHEMA_URL)).build();

    CompletableFuture<Integer> resp = sendEvent(firstEvent);
    assertThat(resp.join()).isEqualTo(201);

    marquez.service.models.LineageEvent secondEvent =
        builder
            .eventTime(time.plusSeconds(10))
            .eventType("COMPLETE")
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL))
            .build();

    resp = sendEvent(secondEvent);
    assertThat(resp.join()).isEqualTo(201);

    List<LineageEvent> rawEvents = client.listLineageEvents(MarquezClient.SortDirection.ASC, 10);

    assertThat(rawEvents.size()).isEqualTo(2);
    ObjectMapper mapper = Utils.getMapper();
    assertThat((JsonNode) mapper.valueToTree(firstEvent))
        .isEqualTo(mapper.valueToTree(rawEvents.get(0)));
    assertThat((JsonNode) mapper.valueToTree(secondEvent))
        .isEqualTo(mapper.valueToTree(rawEvents.get(1)));
  }

  @Test
  @SneakyThrows
  public void testFindEventBeforeAfterTime() {
    marquez.service.models.LineageEvent.Run run =
        new marquez.service.models.LineageEvent.Run(
            UUID.randomUUID().toString(),
            marquez.service.models.LineageEvent.RunFacet.builder().build());
    marquez.service.models.LineageEvent.Job job =
        marquez.service.models.LineageEvent.Job.builder()
            .namespace(NAMESPACE_NAME)
            .name(JOB_NAME)
            .build();

    ZonedDateTime after = ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    ZonedDateTime before = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

    marquez.service.models.LineageEvent.Dataset dataset =
        marquez.service.models.LineageEvent.Dataset.builder()
            .namespace(NAMESPACE_NAME)
            .name(DB_TABLE_NAME)
            .build();

    marquez.service.models.LineageEvent.LineageEventBuilder builder =
        marquez.service.models.LineageEvent.builder()
            .producer("testFindEventIsSortedByTime")
            .run(run)
            .job(job)
            .inputs(Collections.emptyList())
            .outputs(Collections.singletonList(dataset))
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL));

    marquez.service.models.LineageEvent firstEvent =
        builder.eventTime(after.minus(1, ChronoUnit.YEARS)).eventType("START").build();

    CompletableFuture<Integer> resp = sendEvent(firstEvent);
    assertThat(resp.join()).isEqualTo(201);

    marquez.service.models.LineageEvent secondEvent =
        builder
            .eventTime(after.plusSeconds(10))
            .eventType("COMPLETE")
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL))
            .build();

    resp = sendEvent(secondEvent);
    assertThat(resp.join()).isEqualTo(201);

    marquez.service.models.LineageEvent thirdEvent =
        builder
            .eventTime(before.plusSeconds(10))
            .eventType("COMPLETE")
            .schemaURL(new URI(RUN_EVENT_SCHEMA_URL))
            .build();

    List<LineageEvent> rawEvents =
        client.listLineageEvents(MarquezClient.SortDirection.ASC, before, after, 10);

    assertThat(rawEvents.size()).isEqualTo(1);
    ObjectMapper mapper = Utils.getMapper();
    assertThat((JsonNode) mapper.valueToTree(secondEvent))
        .isEqualTo(mapper.valueToTree(rawEvents.get(0)));
  }

  @Test
  public void testSendAndDeleteParentRunRelationshipFacet() {
    marquez.service.models.LineageEvent.Run run =
        new marquez.service.models.LineageEvent.Run(
            UUID.randomUUID().toString(),
            marquez.service.models.LineageEvent.RunFacet.builder()
                .parent(
                    marquez.service.models.LineageEvent.ParentRunFacet.builder()
                        .run(
                            marquez.service.models.LineageEvent.RunLink.builder()
                                .runId(UUID.randomUUID().toString())
                                .build())
                        .job(
                            marquez.service.models.LineageEvent.JobLink.builder()
                                .name("parent")
                                .namespace(NAMESPACE_NAME)
                                .build())
                        ._producer(PRODUCER_URL)
                        ._schemaURL(SCHEMA_URL)
                        .build())
                .build());
    marquez.service.models.LineageEvent.Job job =
        marquez.service.models.LineageEvent.Job.builder()
            .namespace(NAMESPACE_NAME)
            .name(JOB_NAME)
            .build();

    marquez.service.models.LineageEvent event =
        marquez.service.models.LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
            .producer(PRODUCER_URL.toString())
            .run(run)
            .job(job)
            .inputs(Collections.emptyList())
            .outputs(Collections.emptyList())
            .build();

    CompletableFuture<Integer> resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(201);

    List<Job> jobs = client.listJobs(NAMESPACE_NAME);

    String marquezJobName = String.format("parent.%s", JOB_NAME);

    assertThat(jobs.size()).isEqualTo(2);
    assertThat(jobs)
        .anySatisfy(returnedJob -> assertThat(returnedJob.getName()).isEqualTo("parent"))
        .anySatisfy(returnedJob -> assertThat(returnedJob.getName()).isEqualTo(marquezJobName));

    client.deleteJob(NAMESPACE_NAME, marquezJobName);

    jobs = client.listJobs(NAMESPACE_NAME);
    assertThat(jobs.size()).isEqualTo(1);
    assertThat(jobs)
        .anySatisfy(returnedJob -> assertThat(returnedJob.getName()).isEqualTo("parent"))
        .noneSatisfy(returnedJob -> assertThat(returnedJob.getName()).isEqualTo(marquezJobName));
  }

  private CompletableFuture<Integer> sendEvent(marquez.service.models.LineageEvent event) {
    return this.sendLineage(Utils.toJson(event))
        .thenApply(HttpResponse::statusCode)
        .whenComplete(
            (val, error) -> {
              if (error != null) {
                Assertions.fail("Could not complete request");
              }
            });
  }

  private CompletableFuture<Integer> sendAllEvents(RunEvent... events) {
    return Arrays.stream(events)
        .reduce(
            CompletableFuture.completedFuture(201),
            (prev, event) ->
                prev.thenCompose(
                    result -> {
                      String body;
                      try {
                        body = Utils.getMapper().writeValueAsString(event);
                      } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                      }
                      return this.sendLineage(body)
                          .thenApply(HttpResponse::statusCode)
                          .whenComplete(
                              (val, error) -> {
                                if (error != null) {
                                  Assertions.fail("Could not complete request");
                                }
                                assertEquals(201, val, "Error code received from server");
                              });
                    }),
            (a, b) -> a.thenCompose((res) -> b));
  }

  @NotNull
  private RunEvent createAirflowRunEvent(
      OpenLineage ol,
      ZonedDateTime startOfHour,
      ZonedDateTime endOfHour,
      String airflowParentRunId,
      String dagName,
      String taskName,
      String namespace) {
    RunFacet airflowVersionFacet = ol.newRunFacet();
    airflowVersionFacet
        .getAdditionalProperties()
        .putAll(ImmutableMap.of("airflowVersion", "2.1.0", "openlineageAirflowVersion", "0.10"));

    return createRunEvent(
        ol,
        startOfHour,
        endOfHour,
        airflowParentRunId,
        dagName,
        taskName,
        Optional.of(airflowVersionFacet),
        namespace);
  }

  @NotNull
  private RunEvent createRunEvent(
      OpenLineage ol,
      ZonedDateTime startOfHour,
      ZonedDateTime endOfHour,
      String airflowParentRunId,
      String dagName,
      String taskName,
      Optional<RunFacet> airflowVersionFacet,
      String namespace) {
    // The Java SDK requires parent run ids to be a UUID, but the python SDK doesn't. In order to
    // emulate requests coming in from older versions of the Airflow library, we log this as just
    // a plain old RunFact, but using the "parent" key name. To Marquez, this will look just the
    // same as a python client using the official ParentRunFacet.
    RunFacet parentRunFacet = ol.newRunFacet();
    RunFacetsBuilder runFacetBuilder =
        ol.newRunFacetsBuilder().nominalTime(ol.newNominalTimeRunFacet(startOfHour, endOfHour));
    if (airflowParentRunId != null) {
      parentRunFacet
          .getAdditionalProperties()
          .putAll(
              ImmutableMap.of(
                  "run",
                  ImmutableMap.of("runId", airflowParentRunId),
                  "job",
                  ImmutableMap.of("namespace", namespace, "name", dagName)));
      runFacetBuilder.put("parent", parentRunFacet);
    }
    airflowVersionFacet.ifPresent(facet -> runFacetBuilder.put("airflow_version", facet));
    return ol.newRunEventBuilder()
        .eventType(EventType.COMPLETE)
        .eventTime(Instant.now().atZone(LineageTestUtils.LOCAL_ZONE))
        .run(ol.newRun(UUID.randomUUID(), runFacetBuilder.build()))
        .job(
            ol.newJob(
                namespace,
                taskName,
                ol.newJobFacetsBuilder()
                    .documentation(ol.newDocumentationJobFacet("the job docs"))
                    .sql(ol.newSQLJobFacet("SELECT * FROM the_table"))
                    .build()))
        .inputs(Collections.emptyList())
        .outputs(Collections.emptyList())
        .build();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testSendOpenLineage(String pathToOpenLineageEvent) throws IOException {
    // (1) Get OpenLineage event.
    final String openLineageEventAsString =
        Resources.toString(Resources.getResource(pathToOpenLineageEvent), Charset.defaultCharset());

    // (2) Send OpenLineage event.
    final CompletableFuture<Integer> resp =
        this.sendLineage(openLineageEventAsString)
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });

    // Ensure the event was received.
    assertThat(resp.join()).isEqualTo(201);

    // (3) Convert the OpenLineage event to Json.
    final JsonNode openLineageEventAsJson =
        Utils.fromJson(openLineageEventAsString, new TypeReference<JsonNode>() {});

    // (4) Verify the input and output dataset facets associated with the OpenLineage event.
    final JsonNode inputsAsJson = openLineageEventAsJson.path("inputs");
    inputsAsJson.forEach(this::validateDatasetFacets);
    inputsAsJson.forEach(this::validateDatasetVersionFacets);

    final JsonNode outputsAsJson = openLineageEventAsJson.path("outputs");
    outputsAsJson.forEach(this::validateDatasetFacets);
    outputsAsJson.forEach(this::validateDatasetVersionFacets);

    // (5) Verify the job facets associated with the OpenLineage event.
    final JsonNode jobAsJson = openLineageEventAsJson.path("job");
    final String jobNamespace = jobAsJson.path("namespace").asText();
    final String jobName = jobAsJson.path("name").asText();
    final JsonNode jobFacetsAsJson = jobAsJson.path("facets");

    final Job job = client.getJob(jobNamespace, jobName);
    LoggerFactory.getLogger(getClass()).info("Got job from server {}", job);
    if (!jobFacetsAsJson.isMissingNode()) {
      final JsonNode facetsForRunAsJson =
          Utils.getMapper().convertValue(job.getFacets(), JsonNode.class);
      assertThat(facetsForRunAsJson).isEqualTo(jobFacetsAsJson);
    } else {
      assertThat(job.getFacets()).isEmpty();
    }

    // (6) Verify the run facets associated with the OpenLineage event.
    final JsonNode runAsJson = openLineageEventAsJson.path("run");
    final String runId = runAsJson.path("runId").asText();
    final JsonNode runFacetsAsJson = runAsJson.path("facets");

    final Run run = client.getRun(runId);
    if (!runFacetsAsJson.isMissingNode()) {
      final JsonNode facetsForRunAsJson =
          Utils.getMapper().convertValue(run.getFacets(), JsonNode.class);
      assertThat(facetsForRunAsJson).isEqualTo(runFacetsAsJson);
    } else {
      assertThat(run.getFacets()).isEmpty();
    }
  }

  @Test
  public void testSendDatasetEvent() throws IOException {
    final String openLineageEventAsString =
        Resources.toString(Resources.getResource(EVENT_DATASET_EVENT), Charset.defaultCharset());

    // (2) Send OpenLineage event.
    final CompletableFuture<Map<Integer, String>> resp =
        this.sendLineage(openLineageEventAsString)
            .thenApply(r -> Collections.singletonMap(r.statusCode(), r.body()))
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });

    // Ensure the event was received.
    Map<Integer, String> respMap = resp.join();

    assertThat(respMap.containsKey(201)).isTrue();

    // (3) Convert the OpenLineage event to Json.
    final JsonNode openLineageEventAsJson =
        Utils.fromJson(openLineageEventAsString, new TypeReference<JsonNode>() {});

    // (4) Verify dataset facet associated with the OpenLineage event.
    final JsonNode json = openLineageEventAsJson.path("dataset");

    final String namespace = json.path("namespace").asText();
    final String output = json.path("name").asText();
    final JsonNode expectedFacets = json.path("facets");

    final Dataset dataset = client.getDataset(namespace, output);
    assertThat(Utils.getMapper().convertValue(dataset.getFacets(), JsonNode.class))
        .isEqualTo(expectedFacets);

    List<DatasetVersion> datasetVersions = client.listDatasetVersions(namespace, output);
    assertThat(datasetVersions).isNotEmpty();

    DatasetVersion latestDatasetVersion = datasetVersions.get(0);
    assertThat(latestDatasetVersion.getNamespace()).isEqualTo(namespace);
    assertThat(latestDatasetVersion.getName()).isEqualTo(output);
    assertThat(Utils.getMapper().convertValue(latestDatasetVersion.getFacets(), JsonNode.class))
        .isEqualTo(expectedFacets);
  }

  @Test
  public void testSendJobEvent() throws IOException {
    final String openLineageEventAsString =
        Resources.toString(Resources.getResource(EVENT_JOB_EVENT), Charset.defaultCharset());
    final JsonNode openLineageEventAsJson =
        Utils.fromJson(openLineageEventAsString, new TypeReference<JsonNode>() {});

    // (1) Send OpenLineage event.
    final CompletableFuture<Map<Integer, String>> resp =
        this.sendLineage(openLineageEventAsString)
            .thenApply(r -> Collections.singletonMap(r.statusCode(), r.body()))
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });

    // Ensure the event was received.
    Map<Integer, String> respMap = resp.join();
    assertThat(respMap.containsKey(201)).isTrue();

    // (2) Verify the job facets associated with the OpenLineage event.
    final JsonNode jobAsJson = openLineageEventAsJson.path("job");
    final String jobNamespace = jobAsJson.path("namespace").asText();
    final String jobName = jobAsJson.path("name").asText();
    final JsonNode jobFacetsAsJson = jobAsJson.path("facets");

    final Job job = client.getJob(jobNamespace, jobName);
    LoggerFactory.getLogger(getClass()).info("Got job from server {}", job);
    if (!jobFacetsAsJson.isMissingNode()) {
      final JsonNode facetsForRunAsJson =
          Utils.getMapper().convertValue(job.getFacets(), JsonNode.class);
      assertThat(facetsForRunAsJson).isEqualTo(jobFacetsAsJson);
    } else {
      assertThat(job.getFacets()).isEmpty();
    }

    // (3) Verify input datasets are present + verify dataset facets in extra call
    final JsonNode inputsAsJson = openLineageEventAsJson.path("inputs");
    final String inputNamespace = inputsAsJson.get(0).path("namespace").asText();
    final String inputName = inputsAsJson.get(0).path("name").asText();

    assertThat(job.getInputs().stream().findAny().get())
        .hasFieldOrPropertyWithValue("namespace", inputNamespace)
        .hasFieldOrPropertyWithValue("name", inputName);
    assertThat(client.getDataset(inputNamespace, inputName))
        .hasFieldOrPropertyWithValue("description", Optional.of("input documentation"));

    // (4) Verify output datasets are present + verify dataset facets in extra call
    final JsonNode outputsAsJson = openLineageEventAsJson.path("outputs");
    final String outputNamespace = outputsAsJson.get(0).path("namespace").asText();
    final String outputName = outputsAsJson.get(0).path("name").asText();

    assertThat(job.getOutputs().stream().findAny().get())
        .hasFieldOrPropertyWithValue("namespace", outputNamespace)
        .hasFieldOrPropertyWithValue("name", outputName);

    assertThat(client.getDataset(outputNamespace, outputName))
        .hasFieldOrPropertyWithValue("description", Optional.of("output documentation"));

    // (5) Verify job version endpoint returns a job
    UUID version = client.listJobVersions(jobNamespace, jobName, 1, 0).get(0).getVersion();

    JobVersion jobVersion = client.getJobVersion(jobNamespace, jobName, version.toString());

    assertThat(jobVersion)
        .hasFieldOrPropertyWithValue("namespace", jobNamespace)
        .hasFieldOrPropertyWithValue("name", jobName);
    assertThat(jobVersion.getInputs()).isNotEmpty();

    // (6) verify list lineage endpoint responds correctly with no events returned
    assertThat(client.listLineageEvents()).hasSize(0);
  }

  private void validateDatasetFacets(JsonNode json) {
    final String namespace = json.path("namespace").asText();
    final String output = json.path("name").asText();
    final JsonNode expectedFacets = json.path("facets");

    final Dataset dataset = client.getDataset(namespace, output);
    if (!expectedFacets.isMissingNode()) {
      assertThat(dataset.getNamespace()).isEqualTo(namespace);
      assertThat(dataset.getName()).isEqualTo(output);
      final JsonNode facetsForDataset =
          Utils.getMapper()
              .convertValue(filterDataQualityFacets(dataset.getFacets()), JsonNode.class);
      assertThat(facetsForDataset).isEqualTo(expectedFacets);
    } else {
      assertThat(dataset.getFacets()).isEmpty();
    }
  }

  private void validateDatasetVersionFacets(JsonNode json) {
    final String namespace = json.path("namespace").asText();
    final String output = json.path("name").asText();
    final JsonNode expectedFacets = json.path("facets");

    List<DatasetVersion> datasetVersions = client.listDatasetVersions(namespace, output);
    assertThat(datasetVersions).isNotEmpty();

    DatasetVersion latestDatasetVersion = datasetVersions.get(0);
    if (!expectedFacets.isMissingNode()) {
      assertThat(latestDatasetVersion.getNamespace()).isEqualTo(namespace);
      assertThat(latestDatasetVersion.getName()).isEqualTo(output);
      final JsonNode facetsForDatasetVersion =
          Utils.getMapper()
              .convertValue(
                  filterDataQualityFacets(latestDatasetVersion.getFacets()), JsonNode.class);
      assertThat(facetsForDatasetVersion).isEqualTo(expectedFacets);
    } else {
      assertThat(latestDatasetVersion.getFacets()).isEmpty();
    }
  }

  // TODO: Filter data quality facets to ensure tests pass, but we'll want to revisit.
  private Map<String, Object> filterDataQualityFacets(@NonNull Map<String, Object> facets) {
    return Maps.filterKeys(
        facets,
        new Predicate<String>() {
          @Override
          public boolean apply(String key) {
            return !key.contains("dataQuality");
          }
        });
  }
}
