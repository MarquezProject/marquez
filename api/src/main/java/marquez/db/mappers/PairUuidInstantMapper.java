/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class PairUuidInstantMapper implements RowMapper<Pair<UUID, Instant>> {
  @Override
  public Pair<UUID, Instant> map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return Pair.of(
        uuidOrThrow(results, Columns.ROW_UUID), timestampOrNull(results, Columns.CREATED_AT));
  }
}
