/* SPDX-License-Identifier: Apache-2.0 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.util.Resources;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetVersion;
import marquez.client.models.Job;
import marquez.client.models.Run;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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

  @Test
  public void testSendOpenLineageBadArgument() throws IOException {
    // Namespaces can't have semi-colons, so this will get rejected
    String badNamespace =
        "sqlserver://myhost:3342;user=auser;password=XXXXXXXXXX;database=TheDatabase";
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
