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
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DatasetRow.builder()
        .uuid(uuidOrNull(results, Columns.ROW_UUID))
        .createdAt(timestampOrThrow(results, Columns.CREATED_AT))
        .updatedAt(timestampOrNull(results, Columns.UPDATED_AT))
        .namespaceUuid(uuidOrNull(results, Columns.NAMESPACE_UUID))
        .datasourceUuid(uuidOrNull(results, Columns.DATASOURCE_UUID))
        .urn(stringOrThrow(results, Columns.URN))
        .name(stringOrThrow(results, Columns.NAME))
        .description(stringOrNull(results, Columns.DESCRIPTION))
        .currentVersionUuid(uuidOrNull(results, Columns.CURRENT_VERSION_UUID))
        .build();
  }
}
