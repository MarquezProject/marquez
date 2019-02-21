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

import static marquez.common.models.Description.NO_DESCRIPTION;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper {
  private DbTableVersionMapper() {}

  public static DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        ConnectionUrl.fromString(request.getConnectionUrl()),
        DbSchemaName.fromString(request.getSchema()),
        DbTableName.fromString(request.getTable()),
        request.getDescription().map(Description::fromString).orElse(NO_DESCRIPTION));
  }
}
