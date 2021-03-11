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

package marquez.db;

import java.time.Instant;
import java.util.UUID;
import marquez.db.mappers.RunArgsRowMapper;
import marquez.db.models.RunArgsRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(RunArgsRowMapper.class)
public interface RunArgsDao {
  @SqlQuery(
      "INSERT INTO run_args ( "
          + "uuid, "
          + "created_at, "
          + "args, "
          + "checksum "
          + ") VALUES ( "
          + ":uuid, "
          + ":now, "
          + ":args, "
          + ":checksum "
          + ") ON CONFLICT(checksum) DO "
          + "UPDATE SET "
          + "args = :args "
          + "RETURNING *")
  RunArgsRow upsert(UUID uuid, Instant now, String args, String checksum);
}
