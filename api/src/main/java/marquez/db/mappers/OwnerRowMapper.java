/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.OwnerRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class OwnerRowMapper implements RowMapper<OwnerRow> {
  @Override
  public OwnerRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new OwnerRow(
        uuidOrNull(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        stringOrThrow(results, Columns.NAME));
  }
}
