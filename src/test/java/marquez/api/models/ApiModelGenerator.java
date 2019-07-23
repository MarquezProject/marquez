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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import marquez.common.models.DatasetUrn;

public final class ApiModelGenerator {
  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new Jdk8Module())
          .registerModule(new JavaTimeModule())
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  private static final Supplier<String> nullString = () -> null;

  private ApiModelGenerator() {}

  public static String asJson(final DatasetRequest datasetRequest) throws JsonProcessingException {
    final Map<String, Object> datasetRequestMap = new LinkedHashMap<>();
    datasetRequestMap.put("name", datasetRequest.getName());
    datasetRequestMap.put("datasourceUrn", datasetRequest.getDatasourceUrn());
    datasetRequestMap.put("description", datasetRequest.getDescription().orElseGet(nullString));

    return MAPPER.writeValueAsString(datasetRequestMap);
  }

  public static DatasetRequest newDatasetRequest() {
    return newDatasetRequest(true);
  }

  public static DatasetRequest newDatasetRequest(final Boolean hasDescription) {
    return new DatasetRequest(
        newDatasetName().getValue(),
        newDatasourceUrn().getValue(),
        hasDescription ? newDescription().getValue() : null);
  }

  public static List<DatasetResponse> newDatasetResponses(final Integer limit) {
    return Stream.generate(() -> newDatasetResponse()).limit(limit).collect(toList());
  }

  public static String asJson(final DatasetResponse datasetResponse) throws JsonProcessingException {
    final Map<String, Object> datasetResponseMap = new LinkedHashMap<>();
    datasetResponseMap.put("name", datasetResponse.getName());
    datasetResponseMap.put("createdAt", datasetResponse.getCreatedAt());
    datasetResponseMap.put("urn", datasetResponse.getUrn());
    datasetResponseMap.put("datasourceUrn", datasetResponse.getDatasourceUrn());
    datasetResponseMap.put("description", datasetResponse.getDescription().orElseGet(nullString));

    return MAPPER.writeValueAsString(datasetResponseMap);
  }

  public static DatasetResponse newDatasetResponse() {
    return newDatasetResponse(true);
  }

  public static DatasetResponse newDatasetResponse(final Boolean hasDescription) {
    return new DatasetResponse(
        newDatasetName().getValue(),
        newDatasetUrn().getValue(),
        newIsoTimestamp(),
        newDatasourceUrn().getValue(),
        hasDescription ? newDescription().getValue() : null);
  }

  public static String asJson(final NamespaceRequest namespaceRequest) throws JsonProcessingException {
    final Map<String, Object> namespaceRequestMap = new LinkedHashMap<>();
    namespaceRequestMap.put("ownerName", namespaceRequest.getOwnerName());
    namespaceRequestMap.put("description", namespaceRequest.getDescription().orElseGet(nullString));

    return MAPPER.writeValueAsString(namespaceRequestMap);
  }

  public static NamespaceRequest newNamespaceRequest() {
    return newNamespaceRequest(true);
  }

  public static NamespaceRequest newNamespaceRequest(final Boolean hasDescription) {
    return new NamespaceRequest(
        newOwnerName().getValue(), hasDescription ? newDescription().getValue() : null);
  }

  public static List<NamespaceResponse> newNamespaceResponses(final Integer limit) {
    return Stream.generate(() -> newNamespaceResponse(true)).limit(limit).collect(toList());
  }

  public static String asJson(final NamespaceResponse namespaceResponse) throws JsonProcessingException {
    final Map<String, Object> namespaceResponseMap = new LinkedHashMap<>();
    namespaceResponseMap.put("name", namespaceResponse.getName());
    namespaceResponseMap.put("createdAt", namespaceResponse.getCreatedAt());
    namespaceResponseMap.put("ownerName", namespaceResponse.getOwnerName());
    namespaceResponseMap.put("description", namespaceResponse.getDescription().orElseGet(nullString));

    return MAPPER.writeValueAsString(namespaceResponseMap);
  }

  public static NamespaceResponse newNamespaceResponse() {
    return newNamespaceResponse(true);
  }

  public static NamespaceResponse newNamespaceResponse(final Boolean hasDescription) {
    return new NamespaceResponse(
        newDatasetName().getValue(),
        newIsoTimestamp(),
        newOwnerName().getValue(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static String asJson(final JobRequest jobRequest) throws JsonProcessingException {
    final Map<String, Object> jobRequestMap = new LinkedHashMap<>();
    jobRequestMap.put("inputDatasetUrns", jobRequest.getInputDatasetUrns());
    jobRequestMap.put("outputDatasetUrns", jobRequest.getOutputDatasetUrns());
    jobRequestMap.put("location", jobRequest.getLocation());
    jobRequestMap.put("description", jobRequest.getDescription().orElseGet(nullString));

    return MAPPER.writeValueAsString(jobRequestMap);
  }

  public static JobRequest newJobRequest() {
    return newJobRequest(true);
  }

  public static JobRequest newJobRequest(final Boolean hasDescription) {
    return new JobRequest(
        newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toList()),
        newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toList()),
        newLocation().toString(),
        hasDescription ? newDescription().getValue() : null);
  }

  public static String newIsoTimestamp() {
    return ISO_INSTANT.format(Instant.now());
  }
}
