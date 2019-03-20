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
import marquez.api.models.DatasourceResponse;
import marquez.api.models.DatasourcesResponse;
import marquez.service.models.Datasource;

public final class DatasourceResponseMapper {
  private DatasourceResponseMapper() {}

  public static DatasourceResponse map(@NonNull Datasource datasource) {
    return new DatasourceResponse(
        datasource.getName().getValue(),
        datasource.getCreatedAt().toString(),
        datasource.getUrn().getValue(),
        datasource.getConnectionUrl().getRawValue());
  }

  public static List<DatasourceResponse> map(@NonNull List<Datasource> datasources) {
    return unmodifiableList(
        datasources.stream().map(datasource -> map(datasource)).collect(toList()));
  }

  public static DatasourcesResponse toDatasourcesResponse(@NonNull List<Datasource> datasources) {
    return new DatasourcesResponse(map(datasources));
  }
}
