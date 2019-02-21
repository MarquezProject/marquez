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
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionRowMapper {
  private DbTableVersionRowMapper() {}

  public static DbTableVersionRow map(
      @NonNull DatasetRow datasetRow,
      @NonNull DbTableInfoRow dbTableInfoRow,
      @NonNull DbTableVersion dbTableVersion) {
    return DbTableVersionRow.builder()
        .uuid(UUID.randomUUID())
        .datasetUuid(datasetRow.getUuid())
        .dbTableInfoUuid(dbTableInfoRow.getUuid())
        .dbTable(dbTableVersion.getDbTableName().getValue())
        .build();
  }
}
