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
import java.net.URL;
import marquez.client.Utils;

public final class JsonGenerator {
  private JsonGenerator() {}

  private static final ObjectMapper MAPPER = Utils.newObjectMapper();

  private static final String DB_TABLE = "DB_TABLE";
  private static final String STREAM = "STREAM";

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
        .put("type", meta.getType())
        .put("connectionUrl", meta.getConnectionUrl().toString())
        .put("description", meta.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final Source source) {
    return MAPPER
        .createObjectNode()
        .put("type", source.getType())
        .put("name", source.getName())
        .put("createdAt", ISO_INSTANT.format(source.getCreatedAt()))
        .put("updatedAt", ISO_INSTANT.format(source.getUpdatedAt()))
        .put("connectionUrl", source.getConnectionUrl().toString())
        .put("description", source.getDescription().orElse(null))
        .toString();
  }

  public static String newJsonFor(final DatasetMeta meta) {
    if (meta instanceof DbTableMeta) {
      return newJsonFor((DbTableMeta) meta);
    } else if (meta instanceof StreamMeta) {
      return newJsonFor((StreamMeta) meta);
    }

    throw new IllegalArgumentException();
  }

  private static String newJsonFor(final DbTableMeta meta) {
    final ArrayNode fields = MAPPER.valueToTree(meta.getFields());
    final ArrayNode tags = MAPPER.valueToTree(meta.getTags());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("type", DB_TABLE)
            .put("physicalName", meta.getPhysicalName())
            .put("sourceName", meta.getSourceName());
    obj.putArray("fields").addAll(fields);
    obj.putArray("tags").addAll(tags);
    obj.put("description", meta.getDescription().orElse(null));
    obj.put("runId", meta.getRunId().orElse(null));

    return obj.toString();
  }

  private static String newJsonFor(final StreamMeta meta) {
    final ArrayNode fields = MAPPER.valueToTree(meta.getFields());
    final ArrayNode tags = MAPPER.valueToTree(meta.getTags());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("type", STREAM)
            .put("physicalName", meta.getPhysicalName())
            .put("sourceName", meta.getSourceName())
            .put("schemaLocation", meta.getSchemaLocation().map(URL::toString).orElse(null));
    obj.putArray("fields").addAll(fields);
    obj.putArray("tags").addAll(tags);
    obj.put("description", meta.getDescription().orElse(null));
    obj.put("runId", meta.getRunId().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final Dataset dataset) {
    if (dataset instanceof DbTable) {
      return newJsonFor((DbTable) dataset);
    } else if (dataset instanceof Stream) {
      return newJsonFor((Stream) dataset);
    }

    throw new IllegalArgumentException();
  }

  private static String newJsonFor(final DbTable dbTable) {
    final ObjectNode id =
        MAPPER
            .createObjectNode()
            .put("namespace", dbTable.getId().getNamespace())
            .put("name", dbTable.getId().getName());
    final ArrayNode fields = MAPPER.valueToTree(dbTable.getFields());
    final ArrayNode tags = MAPPER.valueToTree(dbTable.getTags());

    final ObjectNode obj = MAPPER.createObjectNode();
    obj.set("id", id);
    obj.put("type", DB_TABLE);
    obj.put("name", dbTable.getName());
    obj.put("physicalName", dbTable.getPhysicalName());
    obj.put("createdAt", ISO_INSTANT.format(dbTable.getCreatedAt()));
    obj.put("updatedAt", ISO_INSTANT.format(dbTable.getUpdatedAt()));
    obj.put("namespace", dbTable.getNamespace());
    obj.put("sourceName", dbTable.getSourceName());
    obj.putArray("fields").addAll(fields);
    obj.putArray("tags").addAll(tags);
    obj.put("lastModifiedAt", dbTable.getLastModifiedAt().map(ISO_INSTANT::format).orElse(null));
    obj.put("description", dbTable.getDescription().orElse(null));

    return obj.toString();
  }

  private static String newJsonFor(final Stream stream) {
    final ObjectNode id =
        MAPPER
            .createObjectNode()
            .put("namespace", stream.getId().getNamespace())
            .put("name", stream.getId().getName());
    final ArrayNode fields = MAPPER.valueToTree(stream.getFields());
    final ArrayNode tags = MAPPER.valueToTree(stream.getTags());

    final ObjectNode obj = MAPPER.createObjectNode();
    obj.set("id", id);
    obj.put("type", STREAM);
    obj.put("name", stream.getName());
    obj.put("physicalName", stream.getPhysicalName());
    obj.put("createdAt", ISO_INSTANT.format(stream.getCreatedAt()));
    obj.put("updatedAt", ISO_INSTANT.format(stream.getUpdatedAt()));
    obj.put("namespace", stream.getNamespace());
    obj.put("sourceName", stream.getSourceName());
    obj.putArray("fields").addAll(fields);
    obj.putArray("tags").addAll(tags);
    obj.put("lastModifiedAt", stream.getLastModifiedAt().map(ISO_INSTANT::format).orElse(null));
    obj.put("schemaLocation", stream.getSchemaLocation().map(URL::toString).orElse(null));
    obj.put("description", stream.getDescription().orElse(null));

    return obj.toString();
  }

  public static String newJsonFor(final JobMeta meta) {
    final ArrayNode inputs = MAPPER.valueToTree(meta.getInputs());
    final ArrayNode outputs = MAPPER.valueToTree(meta.getOutputs());
    final ObjectNode obj = MAPPER.createObjectNode();
    final ObjectNode context = MAPPER.createObjectNode();
    meta.getContext().forEach(context::put);

    obj.put("type", meta.getType().toString());
    obj.putArray("inputs").addAll(inputs);
    obj.putArray("outputs").addAll(outputs);
    obj.put("location", meta.getLocation().map(URL::toString).orElse(null));
    obj.set("context", context);
    obj.put("description", meta.getDescription().orElse(null));
    obj.put("runId", meta.getRunId().orElse(null));

    return obj.toString();
  }

  public static String newJsonFor(final Job job) {
    final ObjectNode id =
        MAPPER
            .createObjectNode()
            .put("namespace", job.getId().getNamespace())
            .put("name", job.getId().getName());
    final ArrayNode inputs = MAPPER.valueToTree(job.getInputs());
    final ArrayNode outputs = MAPPER.valueToTree(job.getOutputs());
    final ObjectNode context = MAPPER.createObjectNode();
    job.getContext().forEach(context::put);

    final ObjectNode obj = MAPPER.createObjectNode();
    obj.set("id", id);
    obj.put("type", job.getType().toString());
    obj.put("name", job.getName());
    obj.put("createdAt", ISO_INSTANT.format(job.getCreatedAt()));
    obj.put("updatedAt", ISO_INSTANT.format(job.getUpdatedAt()));
    obj.put("namespace", job.getNamespace());
    obj.putArray("inputs").addAll(inputs);
    obj.putArray("outputs").addAll(outputs);
    obj.put("location", job.getLocation().map(URL::toString).orElse(null));
    obj.set("context", context);
    obj.put("description", job.getDescription().orElse(null));
    obj.set("latestRun", toObj(job.getLatestRun().orElse(null)));

    return obj.toString();
  }

  public static String newJsonFor(final RunMeta meta) {
    final ObjectNode obj = MAPPER.createObjectNode();
    obj.put("nominalStartTime", meta.getNominalStartTime().map(ISO_INSTANT::format).orElse(null));
    obj.put("nominalEndTime", meta.getNominalEndTime().map(ISO_INSTANT::format).orElse(null));

    final ObjectNode runArgs = MAPPER.createObjectNode();
    meta.getArgs().forEach(runArgs::put);
    obj.set("args", runArgs);

    return obj.toString();
  }

  public static String newJsonFor(final Run run) {
    return toObj(run).toString();
  }

  private static ObjectNode toObj(final Run run) {
    if (run == null) {
      return null;
    }

    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("id", run.getId())
            .put("createdAt", ISO_INSTANT.format(run.getCreatedAt()))
            .put("updatedAt", ISO_INSTANT.format(run.getUpdatedAt()));
    obj.put("nominalStartTime", run.getNominalStartTime().map(ISO_INSTANT::format).orElse(null));
    obj.put("nominalEndTime", run.getNominalEndTime().map(ISO_INSTANT::format).orElse(null));
    obj.put("state", run.getState().name());
    obj.put("startedAt", run.getStartedAt().map(ISO_INSTANT::format).orElse(null));
    obj.put("endedAt", run.getEndedAt().map(ISO_INSTANT::format).orElse(null));
    obj.put("durationMs", run.getDurationMs().orElse(null));

    final ObjectNode runArgs = MAPPER.createObjectNode();
    run.getArgs().forEach(runArgs::put);
    obj.set("args", runArgs);

    return obj;
  }
}
