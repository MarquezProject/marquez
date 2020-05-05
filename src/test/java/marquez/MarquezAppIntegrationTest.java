/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.api.models.ModelGenerator.EMPTY_RUN_REQUEST;
import static marquez.api.models.ModelGenerator.newDbTableRequestWith;
import static marquez.api.models.ModelGenerator.newJobRequestV1With;
import static marquez.api.models.ModelGenerator.newJobRequestV2With;
import static marquez.api.models.ModelGenerator.newNamespaceRequest;
import static marquez.api.models.ModelGenerator.newSourceRequest;
import static marquez.api.models.ModelGenerator.newSourceRequestWith;
import static marquez.api.models.ModelGenerator.newStreamRequestWith;
import static marquez.common.models.ModelGenerator.newDatasetName;
import static marquez.common.models.ModelGenerator.newDatasetNames;
import static marquez.common.models.ModelGenerator.newFields;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newTags;
import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.RUNNING;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.api.models.DbTableRequest;
import marquez.api.models.DbTableResponse;
import marquez.api.models.JobResponse;
import marquez.api.models.JobsResponse;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.api.models.RunsResponse;
import marquez.api.models.SourceResponse;
import marquez.api.models.SourcesResponse;
import marquez.api.models.StreamResponse;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.service.models.DatasetId;
import marquez.service.models.JobId;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class MarquezAppIntegrationTest {
  private static final String CONFIG_FILE = "config.test.yml";
  private static final String CONFIG_FILE_PATH = ResourceHelpers.resourceFilePath(CONFIG_FILE);

  private static final MarquezDb DB = MarquezDb.create();

  static {
    DB.start();
  }

  @ClassRule
  public static final DropwizardAppRule<MarquezConfig> APP =
      new DropwizardAppRule<>(
          MarquezApp.class,
          CONFIG_FILE_PATH,
          ConfigOverride.config("db.url", DB.getJdbcUrl()),
          ConfigOverride.config("db.user", DB.getUsername()),
          ConfigOverride.config("db.password", DB.getPassword()));

  private static final String BASE_API_PATH = "/api/v1";
  private final URI baseUri = URI.create("http://localhost:" + APP.getLocalPort() + BASE_API_PATH);

  private static final int HTTP_200 = 200;
  private static final int HTTP_201 = 201;

  private static NamespaceName defaultNamespace = NamespaceName.of("default");

  @Test
  public void testApp_createNamespace() {
    NamespaceName newNamespaceName = newNamespaceName();
    final Response response =
        APP.client()
            .target(baseUri + "/namespaces/{namespace}")
            .resolveTemplate("namespace", newNamespaceName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newNamespaceRequest()));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);

    final NamespaceResponse ns =
        APP.client()
            .target(baseUri + "/namespaces/{namespace}")
            .resolveTemplate("namespace", newNamespaceName.getValue())
            .request(APPLICATION_JSON)
            .get(NamespaceResponse.class);

    assertThat(ns.getName()).isEqualTo(newNamespaceName.getValue());

    final NamespacesResponse nss =
        APP.client()
            .target(baseUri + "/namespaces")
            .request(APPLICATION_JSON)
            .get(NamespacesResponse.class);

    ImmutableList<NamespaceResponse> filtered =
        nss.getNamespaces().stream()
            .filter((n) -> n.getName().equals(newNamespaceName.getValue()))
            .collect(toImmutableList());
    assertThat(filtered.size()).isEqualTo(1);
  }

  @Test
  public void testApp_createSource() {
    SourceName newSourceName = newSourceName();
    final Response response =
        APP.client()
            .target(baseUri + "/sources/{source}")
            .resolveTemplate("source", newSourceName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newSourceRequest()));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);

    final SourceResponse source =
        APP.client()
            .target(baseUri + "/sources/{source}")
            .resolveTemplate("source", newSourceName.getValue())
            .request(APPLICATION_JSON)
            .get(SourceResponse.class);

    assertThat(source.getName()).isEqualTo(newSourceName.getValue());

    final SourcesResponse sources =
        APP.client()
            .target(baseUri + "/sources")
            .request(APPLICATION_JSON)
            .get(SourcesResponse.class);

    ImmutableList<SourceResponse> filtered =
        sources.getSources().stream()
            .filter((s) -> s.getName().equals(newSourceName.getValue()))
            .collect(toImmutableList());
    assertThat(filtered.size()).isEqualTo(1);
  }

  @Test
  public void testApp_createDbTable() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.POSTGRESQL)));

    final DatasetName datasetName = newDatasetName();

    final Response response =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);

    final DbTableResponse dbTable =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .get(DbTableResponse.class);
    assertThat(dbTable.getName()).isEqualTo(datasetName);
    assertThat(dbTable.getNamespace().getValue()).isEqualTo("default");

    final DatasetsResponse datasets =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets")
            .request(APPLICATION_JSON)
            .get(DatasetsResponse.class);
    assertThat(dbTable.getName()).isEqualTo(datasetName);
    ImmutableList<DatasetResponse> filtered =
        datasets.getDatasets().stream()
            .filter((d) -> d.getName().equals(datasetName))
            .collect(toImmutableList());
    assertThat(filtered.size()).isEqualTo(1);
  }

  @Test
  public void testApp_createDbTableThenAddFields() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.POSTGRESQL)));

    final DatasetName datasetName = newDatasetName();

    final DatasetRequest request0 = newDbTableRequestWith(datasetName, sourceName);
    final Response response0 =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(request0));

    assertThat(response0.getStatus()).isEqualTo(HTTP_200);

    final List<Field> original = request0.getFields();
    final List<Field> fields = Lists.newArrayList(Iterables.concat(original, newFields(2)));

    final DatasetRequest request1 =
        new DbTableRequest(
            datasetName.getValue(),
            sourceName.getValue(),
            fields,
            newTags(2),
            request0.getDescription().get(),
            null);
    final Response response1 =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(request1));

    assertThat(response1.getStatus()).isEqualTo(HTTP_200);
  }

  @Test
  public void testApp_createStream() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.KAFKA)));

    final DatasetName datasetName = newDatasetName();

    final Response response =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newStreamRequestWith(datasetName, sourceName)));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);

    final StreamResponse responseGet =
        APP.client()
            .target(baseUri + "/namespaces/default/datasets/{dataset}")
            .resolveTemplate("dataset", datasetName.getValue())
            .request(APPLICATION_JSON)
            .get(StreamResponse.class);

    assertThat(responseGet.getId())
        .isEqualTo(new DatasetId(NamespaceName.of("default"), datasetName));
    assertThat(responseGet.getName()).isEqualTo(datasetName);
    assertThat(responseGet.getSourceName()).isEqualTo(sourceName.getValue());
    assertThat(responseGet.getNamespace().getValue()).isEqualTo("default");
  }

  @Test
  public void testApp_listTags() {
    final Response response =
        APP.client().target(baseUri + "/tags").request(APPLICATION_JSON).get();

    assertThat(response.getStatus()).isEqualTo(HTTP_200);
  }

  @Test
  public void testApp_createJob() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.POSTGRESQL)));

    final List<DatasetName> inputs = newDatasetNames(2);
    final List<DatasetName> outputs = newDatasetNames(4);

    // old input definition: Input and output are defined by name only, ns is assumed the same as
    // the job
    {
      createDatasets(defaultNamespace, sourceName, inputs);

      createDatasets(defaultNamespace, sourceName, outputs);

      JobName jobName = newJobName();
      final Response responsePut =
          APP.client()
              .target(baseUri + "/namespaces/default/jobs/{job}")
              .resolveTemplate("job", jobName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newJobRequestV1With(inputs, outputs)));

      assertThat(responsePut.getStatus()).isEqualTo(HTTP_200);

      ImmutableList<DatasetId> inputIds =
          inputs.stream()
              .map((n) -> new DatasetId(NamespaceName.of("default"), n))
              .collect(toImmutableList());
      ImmutableList<DatasetId> outputIds =
          outputs.stream()
              .map((n) -> new DatasetId(NamespaceName.of("default"), n))
              .collect(toImmutableList());

      final JobResponse responseGet =
          APP.client()
              .target(baseUri + "/namespaces/default/jobs/{job}")
              .resolveTemplate("job", jobName.getValue())
              .request(APPLICATION_JSON)
              .get(JobResponse.class);
      assertThat(responseGet.getId()).isEqualTo(new JobId(NamespaceName.of("default"), jobName));
      assertThat(responseGet.getName()).isEqualTo(jobName);
      assertThat(responseGet.getInputs()).isEqualTo(inputs);
      assertThat(responseGet.getOutputs()).isEqualTo(outputs);
      assertThat(responseGet.getInputIds()).isEqualTo(inputIds);
      assertThat(responseGet.getOutputIds()).isEqualTo(outputIds);
      assertThat(responseGet.getNamespace().getValue()).isEqualTo("default");
    }

    // new input definition: Input and output are defined by DatasetId
    {
      NamespaceName newNamespaceName = newNamespaceName();
      final NamespaceResponse newNamespace =
          APP.client()
              .target(baseUri + "/namespaces/{namespace}")
              .resolveTemplate("namespace", newNamespaceName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newNamespaceRequest()), NamespaceResponse.class);

      final SourceName sourceName2 = newSourceName();

      APP.client()
          .target(baseUri + "/sources/{source}")
          .resolveTemplate("source", sourceName2.getValue())
          .request(APPLICATION_JSON)
          .put(Entity.json(newSourceRequestWith(SourceType.KAFKA)));

      createDatasets(newNamespaceName, sourceName2, inputs);
      createDatasets(newNamespaceName, sourceName2, outputs);

      ImmutableList<DatasetId> inputIds2 =
          inputs.stream()
              .map((n) -> new DatasetId(NamespaceName.of(newNamespace.getName()), n))
              .collect(toImmutableList());
      ImmutableList<DatasetId> outputIds2 =
          outputs.stream()
              .map((n) -> new DatasetId(NamespaceName.of(newNamespace.getName()), n))
              .collect(toImmutableList());

      JobName jobName2 = newJobName();
      final Response responsePut2 =
          APP.client()
              .target(baseUri + "/namespaces/default/jobs/{job}")
              .resolveTemplate("job", jobName2.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newJobRequestV2With(inputIds2, outputIds2)));

      assertThat(responsePut2.getStatus()).isEqualTo(HTTP_200);

      final JobResponse responseGet2 =
          APP.client()
              .target(baseUri + "/namespaces/default/jobs/{job}")
              .resolveTemplate("job", jobName2.getValue())
              .request(APPLICATION_JSON)
              .get(JobResponse.class);
      assertThat(responseGet2.getId()).isEqualTo(new JobId(NamespaceName.of("default"), jobName2));
      assertThat(responseGet2.getName()).isEqualTo(jobName2);
      assertThat(responseGet2.getInputIds()).isEqualTo(inputIds2);
      assertThat(responseGet2.getOutputIds()).isEqualTo(outputIds2);
      assertThat(responseGet2.getInputs()).isEqualTo(inputs);
      assertThat(responseGet2.getOutputs()).isEqualTo(outputs);
    }
  }

  private void createDatasets(
      NamespaceName namespace, final SourceName sourceName, final List<DatasetName> inputs) {
    inputs.forEach(
        datasetName -> {
          Response r =
              APP.client()
                  .target(baseUri + "/namespaces/{ns}/datasets/{dataset}")
                  .resolveTemplate("ns", namespace.getValue())
                  .resolveTemplate("dataset", datasetName.getValue())
                  .request(APPLICATION_JSON)
                  .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));
          assertThat(r.getStatus()).isEqualTo(HTTP_200);
        });
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testApp_createRunAndMarkAsComplete() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.POSTGRESQL)));

    final List<DatasetName> inputs = newDatasetNames(2);
    final List<DatasetName> outputs = newDatasetNames(4);

    createDatasets(defaultNamespace, sourceName, inputs);

    createDatasets(defaultNamespace, sourceName, outputs);

    final JobName jobName = newJobName();

    APP.client()
        .target(baseUri + "/namespaces/default/jobs/{job}")
        .resolveTemplate("job", jobName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newJobRequestV1With(inputs, outputs)));

    final Response response0 =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs/{job}/runs")
            .resolveTemplate("job", jobName.getValue())
            .request(APPLICATION_JSON)
            .post(Entity.json(EMPTY_RUN_REQUEST));

    assertThat(response0.getStatus()).isEqualTo(HTTP_201);

    final Map<String, Object> run = response0.readEntity(Map.class);
    final String runId = (String) run.get("runId");

    assertThat(response0.getHeaderString(LOCATION)).isEqualTo(baseUri + "/jobs/runs/" + runId);

    final Response response1 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/start")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));

    final Map<String, Object> runStarted = response1.readEntity(Map.class);

    assertThat(response1.getStatus()).isEqualTo(HTTP_200);
    assertThat((String) runStarted.get("runId")).isEqualTo(runId);
    assertThat((String) runStarted.get("runState")).isEqualTo(RUNNING.toString());
    assertThat((String) runStarted.get("startedAt")).isNotBlank();
    assertThat((String) runStarted.get("endedAt")).isBlank();
    assertThat((String) runStarted.get("duration")).isBlank();

    final Instant beforeModified = Instant.now();
    final Response response2 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/complete")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));

    final Map<String, Object> runCompleted = response2.readEntity(Map.class);
    assertThat(response2.getStatus()).isEqualTo(HTTP_200);
    assertThat((String) runCompleted.get("runId")).isEqualTo(runId);
    assertThat((String) runCompleted.get("runState")).isEqualTo(COMPLETED.toString());
    assertThat((String) runCompleted.get("startedAt")).isNotBlank();
    assertThat((String) runCompleted.get("endedAt")).isNotBlank();
    assertThat((Integer) runCompleted.get("duration")).isGreaterThan(0);

    outputs.forEach(
        datasetName -> {
          final Response response3 =
              APP.client()
                  .target(baseUri + "/namespaces/default/datasets/{dataset}")
                  .resolveTemplate("dataset", datasetName.getValue())
                  .request(APPLICATION_JSON)
                  .get();

          final Map<String, String> dataset = response3.readEntity(Map.class);

          assertThat(response3.getStatus()).isEqualTo(HTTP_200);
          assertThat(dataset.get("name")).isEqualTo(datasetName.getValue());

          final Instant lastModifiedAt = Instant.parse(dataset.get("lastModifiedAt"));
          assertThat(lastModifiedAt).isAfter(beforeModified);
        });

    final Response response3 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/fail")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));
    assertThat(response3.getStatus()).isEqualTo(HTTP_200);

    final RunsResponse response3Run =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs/{job}/runs")
            .resolveTemplate("job", jobName.getValue())
            .request(APPLICATION_JSON)
            .get(RunsResponse.class);
    assertThat(response3Run.getRuns().get(0).getState()).isEqualTo("FAILED");

    final Response response4 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/abort")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));
    assertThat(response4.getStatus()).isEqualTo(HTTP_200);

    final RunsResponse response4Run =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs/{job}/runs")
            .resolveTemplate("job", jobName.getValue())
            .request(APPLICATION_JSON)
            .get(RunsResponse.class);
    assertThat(response4Run.getRuns().get(0).getState()).isEqualTo("ABORTED");

    final JobsResponse responseJobs =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs")
            .resolveTemplate("job", jobName.getValue())
            .request(APPLICATION_JSON)
            .get(JobsResponse.class);
    int size =
        responseJobs.getJobs().stream()
            .filter((j) -> j.getName().equals(jobName))
            .collect(toImmutableList())
            .size();
    assertThat(size).isEqualTo(1);
  }
}
