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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import marquez.common.models.DatasetUrn;

public final class ApiModelGenerator {
  private ApiModelGenerator() {}

  public static DatasetRequest newDatasetRequest() {
    return newDatasetRequest(true);
  }

  public static DatasetRequest newDatasetRequest(final Boolean hasDescription) {
    return new DatasetRequest(
        newDatasetName().getValue(),
        newDatasourceUrn().getValue(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static List<DatasetResponse> newDatasetResponses(final Integer limit) {
    return Stream.generate(() -> newDatasetResponse()).limit(limit).collect(toImmutableList());
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
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static NamespaceRequest newNamespaceRequest() {
    return newNamespaceRequest(true);
  }

  public static NamespaceRequest newNamespaceRequest(final Boolean hasDescription) {
    return new NamespaceRequest(
        newOwnerName().getValue(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static List<NamespaceResponse> newNamespaceResponses(final Integer limit) {
    return Stream.generate(() -> newNamespaceResponse(true))
        .limit(limit)
        .collect(toImmutableList());
  }

  public static NamespaceResponse newNamespaceResponse() {
    return newNamespaceResponse(true);
  }

  public static NamespaceResponse newNamespaceResponse(final Boolean hasDescription) {
    return new NamespaceResponse(
        newNamespaceName().getValue(),
        newIsoTimestamp(),
        newOwnerName().getValue(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static JobRequest newJobRequest() {
    return newJobRequest(true);
  }

  public static JobRequest newJobRequest(final Boolean hasDescription) {
    return new JobRequest(
        newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
        newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
        newLocation().toString(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static List<JobResponse> newJobResponses(final Integer limit) {
    return Stream.generate(() -> newJobResponse(true)).limit(limit).collect(toImmutableList());
  }

  public static JobResponse newJobResponse() {
    return newJobResponse(true);
  }

  public static JobResponse newJobResponse(final Boolean hasDescription) {
    final String isoTimestamp = newIsoTimestamp();
    return new JobResponse(
        newJobName().getValue(),
        isoTimestamp,
        isoTimestamp,
        newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
        newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
        newLocation().toString(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static String newIsoTimestamp() {
    return ISO_INSTANT.format(Instant.now());
  }
}
