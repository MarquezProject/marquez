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
import marquez.db.models.ExtendedDatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class ExtendedDatasetRowMapper implements RowMapper<ExtendedDatasetRow> {
  @Override
  public ExtendedDatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new ExtendedDatasetRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        stringOrThrow(results, Columns.TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrThrow(results, Columns.NAMESPACE_UUID),
        uuidOrThrow(results, Columns.SOURCE_UUID),
        stringOrThrow(results, Columns.SOURCE_NAME),
        stringOrThrow(results, Columns.NAME),
        stringOrThrow(results, Columns.PHYSICAL_NAME),
        stringOrNull(results, Columns.DESCRIPTION),
        uuidOrNull(results, Columns.CURRENT_VERSION_UUID));
  }
}
