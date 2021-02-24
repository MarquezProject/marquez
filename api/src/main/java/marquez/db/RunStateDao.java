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
import marquez.common.models.RunState;
import marquez.db.mappers.RunStateRowMapper;
import marquez.db.models.RunStateRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(RunStateRowMapper.class)
public interface RunStateDao extends MarquezDao {
  @SqlQuery("SELECT * FROM run_states WHERE uuid = :rowUuid")
  Optional<RunStateRow> findBy(UUID rowUuid);

  @SqlQuery("SELECT COUNT(*) FROM run_states")
  int count();

  @SqlQuery(
      "INSERT INTO run_states (uuid, transitioned_at, run_uuid, state)"
          + "VALUES (:uuid, :now, :runUuid, :runStateType) RETURNING *")
  RunStateRow upsert(UUID uuid, Instant now, UUID runUuid, RunState runStateType);

  @Transaction
  default void updateRunState(UUID runUuid, RunState runState, Instant transitionedAt) {
    RunDao runDao = createRunDao();
    RunStateRow runStateRow = upsert(UUID.randomUUID(), transitionedAt, runUuid, runState);
    runDao.updateRunState(runUuid, transitionedAt, runState);
    if (runState.isDone()) {
      runDao.updateEndState(runUuid, transitionedAt, runStateRow.getUuid());
    } else if (runState.isStarting()) {
      runDao.updateStartState(runUuid, transitionedAt, runStateRow.getUuid());
    }
    //    if (complete && outputVersionUuids != null && outputVersionUuids.size() > 0) {
    //      createDatasetDao().updateLastModifedAt(outputVersionUuids, updateAt);
    //    }
  }
}
