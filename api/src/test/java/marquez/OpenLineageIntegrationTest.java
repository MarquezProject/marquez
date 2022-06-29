/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetVersion;
import marquez.client.models.Job;
import marquez.client.models.JobId;
import marquez.client.models.Run;
import marquez.common.Utils;
import marquez.db.LineageTestUtils;
import marquez.service.models.LineageEvent;
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
public class OpenLineageIntegrationTest extends BaseIntegrationTest {

  public static String EVENT_REQUIRED = "open_lineage/event_required_only.json";
  public static String EVENT_SIMPLE = "open_lineage/event_simple.json";
  public static String EVENT_FULL = "open_lineage/event_full.json";
  public static String EVENT_UNICODE = "open_lineage/event_unicode.json";
  public static String EVENT_LARGE = "open_lineage/event_large.json";
  public static String NULL_NOMINAL_END_TIME = "open_lineage/null_nominal_end_time.json";
  public static String EVENT_NAMESPACE_NAMING = "open_lineage/event_namespace_naming.json";

  public static List<String> data() {
    return Arrays.asList(
        EVENT_FULL,
        EVENT_SIMPLE,
        EVENT_REQUIRED,
        EVENT_UNICODE,
        // FIXME: A very large event fails the test.
        // EVENT_LARGE,
        NULL_NOMINAL_END_TIME,
        EVENT_NAMESPACE_NAMING);
  }

  @AfterEach
  public void tearDown() {
    Jdbi.create(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .withHandle(
            handle -> {
              handle.execute("DELETE FROM lineage_events");
              handle.execute("DELETE FROM runs_input_mapping");
              handle.execute("DELETE FROM dataset_versions_field_mapping");
              handle.execute("DELETE FROM stream_versions");
              handle.execute("DELETE FROM dataset_versions");
              handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
              handle.execute("DELETE FROM run_states");
              handle.execute("DELETE FROM runs");
              handle.execute("DELETE FROM run_args");
              handle.execute("DELETE FROM job_versions_io_mapping");
              handle.execute("DELETE FROM job_versions");
              handle.execute("DELETE FROM jobs");
              return null;
            });
  }

  @Test
  public void testSendOpenLineageBadArgument() throws IOException {
    // Namespaces can't have emojis, so this will get rejected
    String badNamespace =
        "sqlserver://myhost:3342;user=auser;password=\uD83D\uDE02\uD83D\uDE02\uD83D\uDE02;database=TheDatabase";
    LineageEvent event =
        new LineageEvent(
            "COMPLETE",
            Instant.now().atZone(ZoneId.systemDefault()),
            new LineageEvent.Run(UUID.randomUUID().toString(), null),
            new LineageEvent.Job("namespace", "job_name", null),
            List.of(new LineageEvent.Dataset(badNamespace, "the_table", null)),
            Collections.emptyList(),
            "the_producer");

    final CompletableFuture<Integer> resp =
        this.sendLineage(Utils.toJson(event))
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });

    // Ensure the event was correctly rejected and a proper response code returned.
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
  public void testGetLineageForNonExistantDataset() {
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
        createAirflowRunEvent(ol, startOfHour, endOfHour, airflowParentRunId, task1Name, dagName);

    RunEvent airflowTask2 =
        createAirflowRunEvent(ol, startOfHour, endOfHour, airflowParentRunId, task2Name, dagName);

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
    RunEvent airflowTask1 =
        createAirflowRunEvent(ol, startOfHour, endOfHour, airflowParentRunId, task1Name, dagName);

    RunEvent airflowTask2 =
        createAirflowRunEvent(ol, startOfHour, endOfHour, airflowParentRunId, task2Name, dagName);

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
    UUID parentRunUuid = Utils.toNameBasedUuid(dagName, airflowParentRunId);
    assertThat(runsList.get(0)).hasFieldOrPropertyWithValue("id", parentRunUuid.toString());

    List<Run> taskRunsList = client.listRuns(NAMESPACE_NAME, dagName + "." + task1Name);
    assertThat(taskRunsList).hasSize(1);
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
        createAirflowRunEvent(ol, startOfHour, endOfHour, airflowParentRunId, task1Name, dagName);

    RunEvent sparkTask =
        createRunEvent(
            ol,
            startOfHour,
            endOfHour,
            airflowTask1.getRun().getRunId().toString(),
            sparkTaskName,
            dagName + "." + task1Name,
            Optional.empty());

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
      String taskName,
      String dagName) {
    RunFacet airflowVersionFacet = ol.newRunFacet();
    airflowVersionFacet
        .getAdditionalProperties()
        .putAll(ImmutableMap.of("airflowVersion", "2.1.0", "openlineageAirflowVersion", "0.10"));

    return createRunEvent(
        ol,
        startOfHour,
        endOfHour,
        airflowParentRunId,
        taskName,
        dagName,
        Optional.of(airflowVersionFacet));
  }

  @NotNull
  private RunEvent createRunEvent(
      OpenLineage ol,
      ZonedDateTime startOfHour,
      ZonedDateTime endOfHour,
      String airflowParentRunId,
      String taskName,
      String dagName,
      Optional<RunFacet> airflowVersionFacet) {
    // The Java SDK requires parent run ids to be a UUID, but the python SDK doesn't. In order to
    // emulate requests coming in from older versions of the Airflow library, we log this as just
    // a plain old RunFact, but using the "parent" key name. To Marquez, this will look just the
    // same as a python client using the official ParentRunFacet.
    RunFacet parentRunFacet = ol.newRunFacet();
    parentRunFacet
        .getAdditionalProperties()
        .putAll(
            ImmutableMap.of(
                "run",
                ImmutableMap.of("runId", airflowParentRunId),
                "job",
                ImmutableMap.of("namespace", NAMESPACE_NAME, "name", dagName + "." + taskName)));
    RunFacetsBuilder runFacetBuilder =
        ol.newRunFacetsBuilder()
            .nominalTime(ol.newNominalTimeRunFacet(startOfHour, endOfHour))
            .put("parent", parentRunFacet);
    airflowVersionFacet.ifPresent(facet -> runFacetBuilder.put("airflow_version", facet));
    return ol.newRunEventBuilder()
        .eventType(EventType.COMPLETE)
        .eventTime(Instant.now().atZone(LineageTestUtils.LOCAL_ZONE))
        .run(ol.newRun(UUID.randomUUID(), runFacetBuilder.build()))
        .job(
            ol.newJob(
                NAMESPACE_NAME,
                dagName + "." + taskName,
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

  private void validateDatasetFacets(JsonNode json) {
    final String namespace = json.path("namespace").asText();
    final String output = json.path("name").asText();
    final JsonNode expectedFacets = json.path("facets");

    final Dataset dataset = client.getDataset(namespace, output);
    if (!expectedFacets.isMissingNode()) {
      assertThat(dataset.getNamespace()).isEqualTo(namespace);
      assertThat(dataset.getName()).isEqualTo(output);
      final JsonNode facetsForDataset =
          Utils.getMapper().convertValue(dataset.getFacets(), JsonNode.class);
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
          Utils.getMapper().convertValue(latestDatasetVersion.getFacets(), JsonNode.class);
      assertThat(facetsForDatasetVersion).isEqualTo(expectedFacets);
    } else {
      assertThat(latestDatasetVersion.getFacets()).isEmpty();
    }
  }
}
