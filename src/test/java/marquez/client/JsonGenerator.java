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

package marquez.client;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetMeta;
import marquez.client.models.Datasets;
import marquez.client.models.Datasource;
import marquez.client.models.DatasourceMeta;
import marquez.client.models.Datasources;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.JobRun;
import marquez.client.models.JobRunMeta;
import marquez.client.models.JobRuns;
import marquez.client.models.Jobs;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.Namespaces;

public final class JsonGenerator {
  private JsonGenerator() {}

  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new Jdk8Module());

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

  public static String newJsonFor(final Namespaces namespaces) {
    final ArrayNode array = MAPPER.createArrayNode();
    namespaces.getNamespaces().forEach((namespace) -> array.addPOJO(newJsonFor(namespace)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("namespaces", array);
    return responseAsJson.toString();
  }

  public static String newJsonFor(final JobMeta meta) {
    final ArrayNode array0 = MAPPER.valueToTree(meta.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(meta.getOutputDatasetUrns());
    final ObjectNode obj = MAPPER.createObjectNode();
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
            .put("name", job.getName())
            .put("createdAt", ISO_INSTANT.format(job.getCreatedAt()))
            .put("updatedAt", ISO_INSTANT.format(job.getUpdatedAt()));
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", job.getLocation());
    obj.put("description", job.getDescription().orElse(null));
    return obj.toString();
  }

  public static String newJsonFor(final Jobs jobs) {
    final ArrayNode array = MAPPER.createArrayNode();
    jobs.getJobs().forEach((job) -> array.addPOJO(newJsonFor(job)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("jobs", array);
    return responseAsJson.toString();
  }

  public static String newJsonFor(final JobRunMeta meta) {
    return MAPPER
        .createObjectNode()
        .put("nominalStartTime", ISO_INSTANT.format(meta.getNominalStartTime().orElse(null)))
        .put("nominalEndTime", ISO_INSTANT.format(meta.getNominalEndTime().orElse(null)))
        .put("runArgs", meta.getRunArgs().orElse(null))
        .toString();
  }

  public static String newJsonFor(final JobRun jobRun) {
    return MAPPER
        .createObjectNode()
        .put("runId", jobRun.getRunId())
        .put("nominalStartTime", ISO_INSTANT.format(jobRun.getNominalStartTime().orElse(null)))
        .put("nominalEndTime", ISO_INSTANT.format(jobRun.getNominalEndTime().orElse(null)))
        .put("runArgs", jobRun.getRunArgs().orElse(null))
        .put("runState", jobRun.getRunState().toString())
        .toString();
  }

  public static String newJsonFor(final JobRuns jobRuns) {
    final ArrayNode array = MAPPER.createArrayNode();
    jobRuns.getRuns().forEach((jobRun) -> array.addPOJO(newJsonFor(jobRun)));
    return array.toString();
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

  public static String newJsonFor(final Datasources datasources) {
    final ArrayNode array = MAPPER.createArrayNode();
    datasources.getDatasources().forEach((datasource) -> array.addPOJO(newJsonFor(datasource)));
    return MAPPER.createObjectNode().set("datasources", array).toString();
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

  public static String newJsonFor(final Datasets datasets) {
    final ArrayNode array = MAPPER.createArrayNode();
    datasets.getDatasets().forEach((dataset) -> array.addPOJO(newJsonFor(dataset)));
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("datasets", array);
    return responseAsJson.toString();
  }
}
