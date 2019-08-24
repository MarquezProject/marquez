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

package marquez.api.models;

import static marquez.common.models.Description.NO_DESCRIPTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;

public final class JsonGenerator {
  private JsonGenerator() {}

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  public static String newJsonFor(final NamespaceRequest request) {
    return MAPPER
        .createObjectNode()
        .put("owner", request.getOwner())
        .put("description", request.getDescription().orElse(NO_DESCRIPTION.getValue()))
        .toString();
  }

  public static String newJsonFor(final NamespaceResponse response) {
    return MAPPER
        .createObjectNode()
        .put("name", response.getName())
        .put("createdAt", response.getCreatedAt())
        .put("owner", response.getOwner())
        .put("description", response.getDescription().orElse(NO_DESCRIPTION.getValue()))
        .toString();
  }

  public static String newJsonFor(final NamespacesResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response.getNamespaces().forEach((namespace) -> array.addPOJO(newJsonFor(namespace)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("namespaces", array);
    return responseAsJson.toString();
  }

  public static String newJsonFor(final JobRequest request) {
    final ArrayNode array0 = MAPPER.valueToTree(request.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(request.getOutputDatasetUrns());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("location", request.getLocation())
            .put("description", request.getDescription().orElse(NO_DESCRIPTION.getValue()));
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    return obj.toString();
  }

  public static String newJsonFor(final JobResponse response) {
    final ArrayNode array0 = MAPPER.valueToTree(response.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(response.getOutputDatasetUrns());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("name", response.getName())
            .put("createdAt", response.getCreatedAt())
            .put("updatedAt", response.getUpdatedAt());
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", response.getLocation());
    obj.put("description", response.getDescription().orElse(NO_DESCRIPTION.getValue()));
    return obj.toString();
  }

  public static String newJsonFor(final JobsResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response.getJobs().forEach((job) -> array.addPOJO(newJsonFor(job)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("jobs", array);
    return responseAsJson.toString();
  }

  public static String newJsonFor(final DatasetRequest request) {
    return MAPPER
        .createObjectNode()
        .put("name", request.getName())
        .put("datasourceUrn", request.getDatasourceUrn())
        .put("description", request.getDescription().orElse(NO_DESCRIPTION.getValue()))
        .toString();
  }

  public static String newJsonFor(final DatasetResponse response) {
    return MAPPER
        .createObjectNode()
        .put("name", response.getName())
        .put("createdAt", response.getCreatedAt())
        .put("urn", response.getUrn())
        .put("datasourceUrn", response.getDatasourceUrn())
        .put("description", response.getDescription().orElse(NO_DESCRIPTION.getValue()))
        .toString();
  }

  public static String newJsonFor(final DatasetsResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response.getDatasets().forEach((dataset) -> array.addPOJO(newJsonFor(dataset)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("datasets", array);
    return responseAsJson.toString();
  }

  public static String newJsonFor(final DatasourceRequest request) {
    return MAPPER
        .createObjectNode()
        .put("name", request.getName())
        .put("connectionUrl", request.getConnectionUrl())
        .toString();
  }

  public static String newJsonFor(final DatasourceResponse response) {
    return MAPPER
        .createObjectNode()
        .put("name", response.getName())
        .put("createdAt", response.getCreatedAt())
        .put("urn", response.getUrn())
        .put("connectionUrl", response.getConnectionUrl())
        .toString();
  }

  public static String newJsonFor(final DatasourcesResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response.getDatasources().forEach((datasource) -> array.addPOJO(newJsonFor(datasource)));
    return MAPPER.createObjectNode().set("datasources", array).toString();
  }
}
