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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.ExtendedRunRowMapper;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.RunRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface RunDao extends SqlObject {
  @CreateSqlObject
  JobVersionDao createJobVersionDao();

  @Transaction
  default void insert(RunRow row) {
    getHandle()
        .createUpdate(
            "INSERT INTO runs (uuid, created_at, updated_at, job_version_uuid, run_args_uuid, nominal_start_time, nominal_end_time) "
                + "VALUES (:uuid, :createdAt, :updatedAt, :jobVersionUuid, :runArgsUuid, :nominalStartTime, :nominalEndTime)")
        .bindBean(row)
        .execute();

    createJobVersionDao().update(row.getJobVersionUuid(), row.getCreatedAt(), row.getUuid());
  }

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM runs WHERE uuid = :rowUuid)")
  boolean exists(UUID rowUuid);

  @SqlUpdate(
      "UPDATE runs "
          + "SET updated_at = :updatedAt, "
          + "    current_run_state = :currentRunState "
          + "WHERE uuid = :rowUuid")
  void update(UUID rowUuid, Instant updatedAt, String currentRunState);

  @SqlQuery(
      "SELECT r.*, ra.args "
          + "FROM runs AS r "
          + "INNER JOIN run_args AS ra"
          + "  ON (ra.uuid = r.run_args_uuid) "
          + "WHERE r.uuid = :rowUuid")
  @RegisterRowMapper(ExtendedRunRowMapper.class)
  Optional<ExtendedRunRow> findBy(UUID rowUuid);

  @SqlQuery(
      "SELECT r.*, ra.args "
          + "FROM runs AS r "
          + "INNER JOIN run_args AS ra"
          + "  ON (ra.uuid = r.run_args_uuid)"
          + "WHERE r.job_version_uuid = :jobVersionUuid "
          + "ORDER BY r.created_at "
          + "LIMIT :limit OFFSET :offset")
  @RegisterRowMapper(ExtendedRunRowMapper.class)
  List<ExtendedRunRow> findAll(UUID jobVersionUuid, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM runs")
  int count();
}
