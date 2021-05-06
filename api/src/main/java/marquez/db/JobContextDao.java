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
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.JobContextRowMapper;
import marquez.db.models.JobContextRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(JobContextRowMapper.class)
public interface JobContextDao {
  @SqlQuery("SELECT * FROM job_contexts WHERE uuid = :uuid")
  Optional<JobContextRow> findContextByUuid(UUID uuid);

  @SqlQuery(
      "INSERT INTO job_contexts "
          + "(uuid, created_at, context, checksum) "
          + "VALUES "
          + "(:uuid, :now, :context, :checksum) "
          + "ON CONFLICT (checksum) DO "
          + "UPDATE SET "
          + "context = EXCLUDED.context "
          + "RETURNING *")
  JobContextRow upsert(UUID uuid, Instant now, String context, String checksum);
}
