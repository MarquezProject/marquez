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

import static java.util.stream.Collectors.toList;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newOwnerName;

import marquez.common.models.DatasetUrn;

public final class ApiModelGenerator {
  private ApiModelGenerator() {}

  public static DatasetRequest newDatasetRequest() {
    return newDatasetRequest(true);
  }

  public static DatasetRequest newDatasetRequest(boolean hasDescription) {
    return new DatasetRequest(
        newDatasetName().getValue(),
        newDatasourceUrn().getValue(),
        hasDescription ? newDescription().getValue() : null);
  }

  public static NamespaceRequest newNamespaceRequest() {
    return newNamespaceRequest(true);
  }

  public static NamespaceRequest newNamespaceRequest(boolean hasDescription) {
    return new NamespaceRequest(
        newOwnerName().getValue(), hasDescription ? newDescription().getValue() : null);
  }

  public static JobRequest newJobRequest() {
    return newJobRequest(true);
  }

  public static JobRequest newJobRequest(boolean hasDescription) {
    return new JobRequest(
        newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toList()),
        newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toList()),
        newLocation().toString(),
        hasDescription ? newDescription().getValue() : null);
  }
}
