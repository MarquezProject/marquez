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
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Datasource;

public final class DatasourceMapper {
  private DatasourceMapper() {}

  public static Datasource map(@NonNull DatasourceRow row) {
    return Datasource.builder()
        .name(DatasourceName.of(row.getName()))
        .createdAt(row.getCreatedAt())
        .urn(DatasourceUrn.of(row.getUrn()))
        .connectionUrl(ConnectionUrl.of(row.getConnectionUrl()))
        .build();
  }

  public static List<Datasource> map(@NonNull List<DatasourceRow> rows) {
    return unmodifiableList(rows.stream().map(row -> map(row)).collect(toList()));
  }
}
