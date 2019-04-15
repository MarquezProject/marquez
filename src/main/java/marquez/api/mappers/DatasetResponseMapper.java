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

package marquez.api.mappers;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.Description;
import marquez.service.models.Dataset;

public final class DatasetResponseMapper {
  private DatasetResponseMapper() {}

  public static DatasetResponse map(@NonNull Dataset dataset) {
    final Description description = dataset.getDescription();
    return new DatasetResponse(
        dataset.getName().getValue(),
        dataset.getCreatedAt().toString(),
        dataset.getUrn().getValue(),
        (description == null) ? null : description.getValue());
  }

  public static List<DatasetResponse> map(@NonNull List<Dataset> datasets) {
    return unmodifiableList(datasets.stream().map(dataset -> map(dataset)).collect(toList()));
  }

  public static DatasetsResponse toDatasetsResponse(@NonNull List<Dataset> datasets) {
    return new DatasetsResponse(map(datasets));
  }
}
