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

import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.api.models.SearchResult;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/** */
public final class JobSearchResultMapper implements RowMapper<SearchResult> {
  @Override
  public SearchResult map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return SearchResult.newJobResult(
        JobName.of(stringOrThrow(results, Columns.NAME)),
        timestampOrThrow(results, Columns.UPDATED_AT),
        NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)));
  }
}
