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

import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableVersionRowMapper implements RowMapper<DbTableVersionRow> {
  @Override
  public DbTableVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DbTableVersionRow.builder()
        .uuid(results.getObject(Columns.ROW_UUID, UUID.class))
        .createdAt(timestampOrThrow(results, Columns.CREATED_AT))
        .datasetUuid(results.getObject(Columns.DATASET_UUID, UUID.class))
        .dbTableInfoUuid(results.getObject(Columns.DB_TABLE_INFO_UUID, UUID.class))
        .dbTable(results.getString(Columns.DB_TABLE_NAME))
        .build();
  }
}
