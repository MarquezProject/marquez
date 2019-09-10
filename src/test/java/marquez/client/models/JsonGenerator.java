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

package marquez.client.models;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marquez.client.utils.JsonUtils;

public final class JsonGenerator {
  private JsonGenerator() {}

  private static final ObjectMapper MAPPER = JsonUtils.newObjectMapper();

  public static String newJsonFor(final NamespaceMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("ownerName", meta.getOwnerName())
        .put("description", meta.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Namespace namespace) {
    return MAPPER
        .createObjectNode()
        .put("name", namespace.getName())
        .put("createdAt", ISO_INSTANT.format(namespace.getCreatedAt()))
        .put("ownerName", namespace.getOwnerName())
        .put("description", namespace.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final JobMeta meta) {
    final ArrayNode array0 = MAPPER.valueToTree(meta.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(meta.getOutputDatasetUrns());
    final ObjectNode obj = MAPPER.createObjectNode();
    obj.put("type", meta.getType().toString());
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", meta.getLocation()).put("description", meta.getDescription().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final Job job) {
    final ArrayNode array0 = MAPPER.valueToTree(job.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(job.getOutputDatasetUrns());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("type", job.getType().toString())
            .put("name", job.getName())
            .put("createdAt", ISO_INSTANT.format(job.getCreatedAt()))
            .put("updatedAt", ISO_INSTANT.format(job.getUpdatedAt()));
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", job.getLocation());
    obj.put("description", job.getDescription().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final JobRunMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("nominalStartTime", ISO_INSTANT.format(meta.getNominalStartTime().orElse(null)))
        .put("nominalEndTime", ISO_INSTANT.format(meta.getNominalEndTime().orElse(null)))
        .put("runArgs", meta.getRunArgs().orElse(null))
        .toString();
  }

  public static String newJsonFor(final JobRun run) {
    return MAPPER
        .createObjectNode()
        .put("runId", run.getRunId())
        .put("nominalStartTime", ISO_INSTANT.format(run.getNominalStartTime().orElse(null)))
        .put("nominalEndTime", ISO_INSTANT.format(run.getNominalEndTime().orElse(null)))
        .put("runArgs", run.getRunArgs().orElse(null))
        .put("runState", run.getRunState().toString())
        .toString();
  }

  public static String newJsonFor(final DatasourceMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("name", meta.getName())
        .put("connectionUrl", meta.getConnectionUrl())
        .toString();
  }

  public static String newJsonFor(final Datasource datasource) {
    return MAPPER
        .createObjectNode()
        .put("name", datasource.getName())
        .put("createdAt", ISO_INSTANT.format(datasource.getCreatedAt()))
        .put("urn", datasource.getUrn())
        .put("connectionUrl", datasource.getConnectionUrl())
        .toString();
  }

  public static String newJsonFor(final DatasetMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("name", meta.getName())
        .put("datasourceUrn", meta.getDatasourceUrn())
        .put("description", meta.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Dataset dataset) {
    return MAPPER
        .createObjectNode()
        .put("name", dataset.getName())
        .put("createdAt", ISO_INSTANT.format(dataset.getCreatedAt()))
        .put("urn", dataset.getUrn())
        .put("datasourceUrn", dataset.getDatasourceUrn())
        .put("description", dataset.getDescription().orElse(null))
        .toString();
  }
}
