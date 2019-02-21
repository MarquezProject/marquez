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

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.service.models.Dataset;

public final class DatasetResponseMapper {
  private DatasetResponseMapper() {}

  public static DatasetResponse map(@NonNull Dataset dataset) {
    return new DatasetResponse(
        dataset.getUrn().getValue(),
        dataset.getCreatedAt().toString(),
        dataset.getDescription().map(desc -> desc.getValue()).orElse(null));
  }

  public static List<DatasetResponse> map(@NonNull List<Dataset> datasets) {
    return datasets.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(
            datasets.stream().map(dataset -> map(dataset)).collect(toList()));
  }
}
