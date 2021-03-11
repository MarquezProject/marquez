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
import static marquez.db.Columns.uriOrNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.Columns;
import marquez.service.models.Source;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public final class SourceMapper implements RowMapper<Source> {
  @Override
  public Source map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Source(
        SourceType.of(stringOrThrow(results, Columns.TYPE)),
        SourceName.of(stringOrThrow(results, Columns.NAME)),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uriOrNull(results, Columns.CONNECTION_URL),
        stringOrNull(results, Columns.DESCRIPTION));
  }
}
