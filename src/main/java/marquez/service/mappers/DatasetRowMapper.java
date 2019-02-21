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

import static marquez.common.models.Description.NO_VALUE;

import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.NamespaceName;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.service.models.DbTableVersion;

public final class DatasetRowMapper {
  private DatasetRowMapper() {}

  public static DatasetRow map(
      @NonNull NamespaceName namespaceName,
      @NonNull DataSourceRow dataSourceRow,
      @NonNull DbTableVersion dbTableVersion) {
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .dataSourceUuid(dataSourceRow.getUuid())
        .urn(dbTableVersion.toDatasetUrn(namespaceName).getValue())
        .description(
            dbTableVersion.getDescription().map((desc) -> desc.getValue()).orElse(NO_VALUE))
        .build();
  }
}
