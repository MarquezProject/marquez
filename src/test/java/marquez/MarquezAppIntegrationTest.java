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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.api.models.ModelGenerator.EMPTY_RUN_REQUEST;
import static marquez.api.models.ModelGenerator.newDbTableRequestWith;
import static marquez.api.models.ModelGenerator.newJobRequestWith;
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
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DbTableRequest;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.JobName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.service.models.Run;
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

  @Test
  public void testApp_createNamespace() {
    final Response response =
        APP.client()
            .target(baseUri + "/namespaces/{namespace}")
            .resolveTemplate("namespace", newNamespaceName().getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newNamespaceRequest()));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);
  }

  @Test
  public void testApp_createSource() {
    final Response response =
        APP.client()
            .target(baseUri + "/sources/{source}")
            .resolveTemplate("source", newSourceName().getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newSourceRequest()));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);
  }

  @Test
  public void testApp_listTag() {
    final Response response =
        APP.client().target(baseUri + "/tags").request(APPLICATION_JSON).get();

    assertThat(response.getStatus()).isEqualTo(HTTP_200);
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

    inputs.forEach(
        datasetName -> {
          APP.client()
              .target(baseUri + "/namespaces/default/datasets/{dataset}")
              .resolveTemplate("dataset", datasetName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));
        });

    outputs.forEach(
        datasetName -> {
          APP.client()
              .target(baseUri + "/namespaces/default/datasets/{dataset}")
              .resolveTemplate("dataset", datasetName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));
        });

    final Response response =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs/{job}")
            .resolveTemplate("job", newJobName().getValue())
            .request(APPLICATION_JSON)
            .put(Entity.json(newJobRequestWith(inputs, outputs)));

    assertThat(response.getStatus()).isEqualTo(HTTP_200);
  }

  @Test
  public void testApp_createRunAndMarkAsComplete() {
    final SourceName sourceName = newSourceName();

    APP.client()
        .target(baseUri + "/sources/{source}")
        .resolveTemplate("source", sourceName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newSourceRequestWith(SourceType.POSTGRESQL)));

    final List<DatasetName> inputs = newDatasetNames(2);
    final List<DatasetName> outputs = newDatasetNames(4);

    inputs.forEach(
        datasetName -> {
          APP.client()
              .target(baseUri + "/namespaces/default/datasets/{dataset}")
              .resolveTemplate("dataset", datasetName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));
        });

    outputs.forEach(
        datasetName -> {
          APP.client()
              .target(baseUri + "/namespaces/default/datasets/{dataset}")
              .resolveTemplate("dataset", datasetName.getValue())
              .request(APPLICATION_JSON)
              .put(Entity.json(newDbTableRequestWith(datasetName, sourceName)));
        });

    final JobName jobName = newJobName();

    APP.client()
        .target(baseUri + "/namespaces/default/jobs/{job}")
        .resolveTemplate("job", jobName.getValue())
        .request(APPLICATION_JSON)
        .put(Entity.json(newJobRequestWith(inputs, outputs)));

    final Response response0 =
        APP.client()
            .target(baseUri + "/namespaces/default/jobs/{job}/runs")
            .resolveTemplate("job", jobName.getValue())
            .request(APPLICATION_JSON)
            .post(Entity.json(EMPTY_RUN_REQUEST));

    assertThat(response0.getStatus()).isEqualTo(HTTP_201);

    final Map<String, Object> run = response0.readEntity(Map.class);
    final String runId = (String) run.get("runId");

    final Response response1 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/start")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));

    final Map<String, Object> runStarted = response1.readEntity(Map.class);

    assertThat(response1.getStatus()).isEqualTo(HTTP_200);
    assertThat((String) runStarted.get("runId")).isEqualTo(runId);
    assertThat((String) runStarted.get("runState")).isEqualTo(Run.State.RUNNING.toString());

    final Response response2 =
        APP.client()
            .target(baseUri + "/jobs/runs/{id}/complete")
            .resolveTemplate("id", runId)
            .request(APPLICATION_JSON)
            .post(Entity.json(ImmutableMap.of()));

    final Map<String, Object> runCompleted = response2.readEntity(Map.class);

    assertThat(response2.getStatus()).isEqualTo(HTTP_200);
    assertThat((String) runCompleted.get("runId")).isEqualTo(runId);
    assertThat((String) runCompleted.get("runState")).isEqualTo(Run.State.COMPLETED.toString());
  }
}
