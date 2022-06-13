/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.RunArgsRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class RunArgsRowMapper implements RowMapper<RunArgsRow> {
  @Override
  public RunArgsRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new RunArgsRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        stringOrThrow(results, Columns.ARGS),
        stringOrThrow(results, Columns.CHECKSUM));
  }
}
