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
        hasDescription ? newDescription().getValue() : null);
  }

  public static DatasetsResponse newDatasetsResponse() {
    return newDatasetsResponseWith(newDatasetResponses(4));
  }

  public static DatasetsResponse newDatasetsResponseWith(final List<DatasetResponse> responses) {
    return new DatasetsResponse(responses);
  }

  public static List<DatasetResponse> newDatasetResponses(final Integer limit) {
    return Stream.generate(() -> newDatasetResponse()).limit(limit).collect(toList());
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

  public static NamespaceRequest newNamespaceRequest() {
    return newNamespaceRequest(true);
  }

  public static NamespaceRequest newNamespaceRequest(final Boolean hasDescription) {
    return new NamespaceRequest(
        newOwnerName().getValue(), hasDescription ? newDescription().getValue() : null);
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
