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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
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
        .uuid(results.getObject(Columns.ROW_UUID, UUID.class))
        .createdAt(results.getTimestamp(Columns.CREATED_AT).toInstant())
        .updatedAt(Columns.toInstantOrNull(results.getTimestamp(Columns.UPDATED_AT)))
        .namespaceUuid(results.getObject(Columns.NAMESPACE_UUID, UUID.class))
        .datasourceUuid(results.getObject(Columns.DATA_SOURCE_UUID, UUID.class))
        .urn(results.getString(Columns.URN))
        .description(results.getString(Columns.DESCRIPTION))
        .currentVersion(Columns.toUuidOrNull(results.getString(Columns.CURRENT_VERSION_UUID)))
        .build();
  }
}
