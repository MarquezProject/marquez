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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.NonNull;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper {
  private DatasetMapper() {}

  public static Dataset map(@NonNull DatasetRow row) {
    return Dataset.builder()
        .name(DatasetName.fromString(row.getName()))
        .createdAt(row.getCreatedAt())
        .urn(DatasetUrn.fromString(row.getUrn()))
        .description(Description.fromString(row.getDescription()))
        .build();
  }

  public static List<Dataset> map(@NonNull List<DatasetRow> rows) {
    return unmodifiableList(rows.stream().map(row -> map(row)).collect(toList()));
  }
}
