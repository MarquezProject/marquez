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
import marquez.db.mappers.RunStateRowMapper;
import marquez.db.models.RunStateRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(RunStateRowMapper.class)
public interface RunStateDao extends SqlObject {
  @CreateSqlObject
  DatasetDao createDatasetDao();

  @CreateSqlObject
  RunDao createRunDao();

  @Transaction
  default void insertWith(RunStateRow row, List<UUID> datasetUuids, Instant lastModified) {
    insert(row);
    createDatasetDao().updateLastModifed(datasetUuids, lastModified);
  }

  @Transaction
  default void insert(RunStateRow row) {
    getHandle()
        .createUpdate(
            "INSERT INTO run_states (uuid, transitioned_at, run_uuid, state)"
                + "VALUES (:uuid, :transitionedAt, :runUuid, :state)")
        .bindBean(row)
        .execute();

    createRunDao().update(row.getRunUuid(), row.getTransitionedAt(), row.getState());
  }

  @SqlQuery("SELECT * FROM run_states WHERE uuid = :rowUuid")
  Optional<RunStateRow> findBy(UUID rowUuid);

  @SqlQuery(
      "SELECT * FROM run_states "
          + "WHERE run_uuid = :runUuid "
          + "ORDER BY transitioned_at DESC "
          + "LIMIT 1")
  Optional<RunStateRow> findLatest(UUID runUuid);

  @SqlQuery("SELECT COUNT(*) FROM run_states")
  int count();
}
