/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NamespaceRowMapper implements RowMapper<NamespaceRow> {
  @Override
  public NamespaceRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new NamespaceRow(
        uuidOrNull(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        stringOrThrow(results, Columns.NAME),
        stringOrNull(results, Columns.DESCRIPTION),
        stringOrThrow(results, Columns.CURRENT_OWNER_NAME));
  }
}
