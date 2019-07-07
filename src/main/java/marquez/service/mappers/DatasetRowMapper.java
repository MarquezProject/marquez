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

import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasourceRow;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Dataset;

public final class DatasetRowMapper {
  private DatasetRowMapper() {}

  public static DatasetRow map(
      @NonNull NamespaceRow namespaceRow,
      @NonNull DatasourceRow datasourceRow,
      @NonNull Dataset dataset) {
    final DatasourceName datasourceName = DatasourceName.of(datasourceRow.getName());
    final DatasetUrn datasetUrn = DatasetUrn.of(datasourceName, dataset.getName());
    final Description description = dataset.getDescription();
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .namespaceUuid(namespaceRow.getUuid())
        .datasourceUuid(datasourceRow.getUuid())
        .name(dataset.getName().getValue())
        .urn(datasetUrn.getValue())
        .description(description == null ? null : description.getValue())
        .build();
  }
}
