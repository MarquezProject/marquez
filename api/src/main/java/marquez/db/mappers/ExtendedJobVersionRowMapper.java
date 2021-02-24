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
import static marquez.db.Columns.uuidArrayOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.MapperUtils;
import marquez.db.models.ExtendedJobVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class ExtendedJobVersionRowMapper implements RowMapper<ExtendedJobVersionRow> {
  @Override
  public ExtendedJobVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());

    return new ExtendedJobVersionRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrThrow(results, Columns.JOB_UUID),
        uuidOrThrow(results, Columns.JOB_CONTEXT_UUID),
        columnNames.contains(Columns.INPUT_UUIDS)
            ? uuidArrayOrThrow(results, Columns.INPUT_UUIDS)
            : ImmutableList.<UUID>of(),
        columnNames.contains(Columns.OUTPUT_UUIDS)
            ? uuidArrayOrThrow(results, Columns.OUTPUT_UUIDS)
            : ImmutableList.<UUID>of(),
        stringOrNull(results, Columns.LOCATION),
        uuidOrThrow(results, Columns.VERSION),
        uuidOrNull(results, Columns.LATEST_RUN_UUID),
        columnNames.contains(Columns.CONTEXT) ? stringOrThrow(results, Columns.CONTEXT) : "",
        stringOrThrow(results, Columns.NAMESPACE_NAME),
        stringOrThrow(results, Columns.JOB_NAME),
        uuidOrThrow(results, Columns.NAMESPACE_UUID));
  }
}
