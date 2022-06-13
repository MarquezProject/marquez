/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
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
  RunArgsRow upsertRunArgs(UUID uuid, Instant now, String args, String checksum);
}
