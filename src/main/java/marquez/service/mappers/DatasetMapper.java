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
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.service.models.Dataset;

public final class DatasetMapper {
  private DatasetMapper() {}

  public static Dataset map(@NonNull DatasourceUrn datasourceUrn, @NonNull DatasetRow row) {
    return Dataset.builder()
        .name(DatasetName.of(row.getName()))
        .createdAt(row.getCreatedAt())
        .urn(DatasetUrn.of(row.getUrn()))
        .datasourceUrn(datasourceUrn)
        .description(Description.of(row.getDescription()))
        .build();
  }

  public static Dataset map(@NonNull DatasetRowExtended rowExtended) {
    return Dataset.builder()
        .name(DatasetName.of(rowExtended.getName()))
        .createdAt(rowExtended.getCreatedAt())
        .urn(DatasetUrn.of(rowExtended.getUrn()))
        .datasourceUrn(DatasourceUrn.of(rowExtended.getDatasourceUrn()))
        .description(Description.of(rowExtended.getDescription()))
        .build();
  }

  public static List<Dataset> map(@NonNull List<DatasetRowExtended> rowsExtended) {
    return unmodifiableList(
        rowsExtended.stream().map(rowExtended -> map(rowExtended)).collect(toList()));
  }
}
