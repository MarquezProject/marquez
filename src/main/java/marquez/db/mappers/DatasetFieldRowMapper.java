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
import static marquez.db.Columns.uuidOrThrow;

import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.MapperUtils;
import marquez.db.models.DatasetFieldRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetFieldRowMapper implements RowMapper<DatasetFieldRow> {
  @Override
  public DatasetFieldRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());

    return new DatasetFieldRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        stringOrThrow(results, Columns.TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrThrow(results, Columns.DATASET_UUID),
        stringOrThrow(results, Columns.NAME),
        columnNames.contains(Columns.TAG_UUIDS)
            ? uuidArrayOrThrow(results, Columns.TAG_UUIDS)
            : ImmutableList.of(),
        stringOrNull(results, Columns.DESCRIPTION));
  }
}
