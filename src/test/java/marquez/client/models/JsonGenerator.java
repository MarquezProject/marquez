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
import marquez.client.Utils;

public final class JsonGenerator {
  private JsonGenerator() {}

  private static final ObjectMapper MAPPER = Utils.newObjectMapper();

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
        .put("updatedAt", ISO_INSTANT.format(namespace.getUpdatedAt()))
        .put("ownerName", namespace.getOwnerName())
        .put("description", namespace.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final SourceMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("type", meta.getType().toString())
        .put("connectionUrl", meta.getConnectionUrl())
        .put("description", meta.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Source source) {
    return MAPPER
        .createObjectNode()
        .put("type", source.getType().toString())
        .put("name", source.getName())
        .put("createdAt", ISO_INSTANT.format(source.getCreatedAt()))
        .put("updatedAt", ISO_INSTANT.format(source.getUpdatedAt()))
        .put("connectionUrl", source.getConnectionUrl())
        .put("description", source.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final DatasetMeta meta) {
    switch (meta.getType()) {
      case STREAM:
        return newJsonFor((StreamMeta) meta);
      default:
        return newJsonFor((DbTableMeta) meta);
    }
  }

  public static String newJsonFor(final DbTableMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("type", meta.getType().toString())
        .put("physicalName", meta.getPhysicalName())
        .put("sourceName", meta.getSourceName())
        .put("description", meta.getDescription().orElse(null))
        .put("runId", meta.getRunId().orElse(null))
        .toString();
  }

  public static String newJsonFor(final StreamMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("type", meta.getType().toString())
        .put("physicalName", meta.getPhysicalName())
        .put("sourceName", meta.getSourceName())
        .put("schemaLocation", meta.getSchemaLocation())
        .put("description", meta.getDescription().orElse(null))
        .put("runId", meta.getRunId().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Dataset dataset) {
    switch (dataset.getType()) {
      case STREAM:
        return newJsonFor((Stream) dataset);
      default:
        return newJsonFor((DbTable) dataset);
    }
  }

  public static String newJsonFor(final DbTable dataset) {
    return MAPPER
        .createObjectNode()
        .put("type", dataset.getType().toString())
        .put("name", dataset.getName())
        .put("physicalName", dataset.getPhysicalName())
        .put("createdAt", ISO_INSTANT.format(dataset.getCreatedAt()))
        .put("updatedAt", ISO_INSTANT.format(dataset.getUpdatedAt()))
        .put("sourceName", dataset.getSourceName())
        .put("description", dataset.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Stream dataset) {
    return MAPPER
        .createObjectNode()
        .put("type", dataset.getType().toString())
        .put("name", dataset.getName())
        .put("physicalName", dataset.getPhysicalName())
        .put("createdAt", ISO_INSTANT.format(dataset.getCreatedAt()))
        .put("updatedAt", ISO_INSTANT.format(dataset.getUpdatedAt()))
        .put("sourceName", dataset.getSourceName())
        .put("schemaLocation", dataset.getSchemaLocation())
        .put("description", dataset.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final JobMeta meta) {
    final ArrayNode inputs = MAPPER.valueToTree(meta.getInputs());
    final ArrayNode outputs = MAPPER.valueToTree(meta.getOutputs());
    final ObjectNode obj = MAPPER.createObjectNode();
    obj.put("type", meta.getType().toString());
    obj.putArray("inputs").addAll(inputs);
    obj.putArray("outputs").addAll(outputs);
    obj.put("location", meta.getLocation());
    obj.put("description", meta.getDescription().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final Job job) {
    final ArrayNode inputs = MAPPER.valueToTree(job.getInputs());
    final ArrayNode outputs = MAPPER.valueToTree(job.getOutputs());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("type", job.getType().toString())
            .put("name", job.getName())
            .put("createdAt", ISO_INSTANT.format(job.getCreatedAt()))
            .put("updatedAt", ISO_INSTANT.format(job.getUpdatedAt()));
    obj.putArray("inputs").addAll(inputs);
    obj.putArray("outputs").addAll(outputs);
    obj.put("location", job.getLocation());
    obj.put("description", job.getDescription().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final RunMeta meta) {
    final ObjectNode obj = MAPPER.createObjectNode();
    obj.put("nominalStartTime", meta.getNominalStartTime().map(ISO_INSTANT::format).orElse(null));
    obj.put("nominalEndTime", meta.getNominalEndTime().map(ISO_INSTANT::format).orElse(null));

    final ObjectNode runArgs = MAPPER.createObjectNode();
    meta.getArgs().forEach((k, v) -> runArgs.put(k, v));
    obj.set("runArgs", runArgs);
    return obj.toString();
  }

  public static String newJsonFor(final Run run) {
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("runId", run.getId())
            .put("createdAt", ISO_INSTANT.format(run.getCreatedAt()))
            .put("updatedAt", ISO_INSTANT.format(run.getUpdatedAt()));
    obj.put("nominalStartTime", run.getNominalStartTime().map(ISO_INSTANT::format).orElse(null));
    obj.put("nominalEndTime", run.getNominalEndTime().map(ISO_INSTANT::format).orElse(null));

    final ObjectNode runArgs = MAPPER.createObjectNode();
    run.getArgs().forEach((k, v) -> runArgs.put(k, v));
    obj.set("runArgs", runArgs);
    obj.put("runState", run.getState().toString());
    return obj.toString();
  }
}
