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

package marquez.service.mappers;

import static java.util.stream.Collectors.toList;
import static marquez.common.models.Description.NO_DESCRIPTION;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper {
  private DatasetMapper() {}

  public static Dataset map(@NonNull DatasetRow datasetRow) {
    return new Dataset(
        DatasetUrn.fromString(datasetRow.getUrn()),
        datasetRow.getCreatedAt(),
        datasetRow.getDescription().map(Description::fromString).orElse(NO_DESCRIPTION));
  }

  public static List<Dataset> map(@NonNull List<DatasetRow> datasetRows) {
    return datasetRows.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(datasetRows.stream().map(row -> map(row)).collect(toList()));
  }
}
