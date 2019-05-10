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

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetRowExtended;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowExtendedMapper implements RowMapper<DatasetRowExtended> {
  @Override
  public DatasetRowExtended map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DatasetRowExtended.builderExtended()
        .uuid(uuidOrNull(results, Columns.ROW_UUID))
        .createdAt(timestampOrThrow(results, Columns.CREATED_AT))
        .updatedAt(timestampOrThrow(results, Columns.UPDATED_AT))
        .namespaceUuid(uuidOrThrow(results, Columns.NAMESPACE_UUID))
        .datasourceUuid(uuidOrThrow(results, Columns.DATASOURCE_UUID))
        .name(stringOrThrow(results, Columns.NAME))
        .urn(stringOrThrow(results, Columns.URN))
        .datasourceUrn(stringOrThrow(results, Columns.DATASOURCE_URN))
        .description(stringOrNull(results, Columns.DESCRIPTION))
        .currentVersionUuid(uuidOrNull(results, Columns.CURRENT_VERSION_UUID))
        .build();
  }
}
