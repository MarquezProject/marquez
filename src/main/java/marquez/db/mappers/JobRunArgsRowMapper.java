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
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.RunArgs;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobRunArgsRowMapper implements RowMapper<RunArgs> {
  @Override
  public RunArgs map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new RunArgs(
        results.getString(Columns.CHECKSUM),
        results.getString(Columns.RUN_ARGS),
        results.getTimestamp(Columns.CREATED_AT));
  }
}
