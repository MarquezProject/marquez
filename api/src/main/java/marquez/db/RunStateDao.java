/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.common.models.RunState;
import marquez.db.mappers.RunStateRowMapper;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.RunStateRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(RunStateRowMapper.class)
public interface RunStateDao extends BaseDao {
  @SqlQuery(
      "INSERT INTO run_states (uuid, transitioned_at, run_uuid, state)"
          + "VALUES (:uuid, :now, :runUuid, :runStateType) RETURNING *")
  RunStateRow upsert(UUID uuid, Instant now, UUID runUuid, RunState runStateType);

  @Transaction
  default void updateRunStateFor(UUID runUuid, RunState runState, Instant transitionedAt) {
    RunDao runDao = createRunDao();
    RunStateRow runStateRow = upsert(UUID.randomUUID(), transitionedAt, runUuid, runState);
    runDao.updateRunState(runUuid, transitionedAt, runState);
    if (runState.isDone()) {
      runDao.updateEndState(runUuid, transitionedAt, runStateRow.getUuid());
      List<ExtendedDatasetVersionRow> outputs =
          createDatasetVersionDao().findOutputDatasetVersionsFor(runUuid);
      List<UUID> outputUuids =
          outputs.stream().map(DatasetVersionRow::getDatasetUuid).collect(Collectors.toList());
      if (!outputUuids.isEmpty()) {
        createDatasetDao().updateLastModifiedAt(outputUuids, transitionedAt);
      }
    } else if (runState.isStarting()) {
      runDao.updateStartState(runUuid, transitionedAt, runStateRow.getUuid());
    }
  }
}
