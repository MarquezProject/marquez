/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.booleanOrDefault;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new DatasetRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        stringOrThrow(results, Columns.TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrThrow(results, Columns.NAMESPACE_UUID),
        uuidOrThrow(results, Columns.SOURCE_UUID),
        stringOrThrow(results, Columns.NAME),
        stringOrThrow(results, Columns.PHYSICAL_NAME),
        timestampOrNull(results, Columns.LAST_MODIFIED_AT),
        stringOrNull(results, Columns.DESCRIPTION),
        uuidOrNull(results, Columns.CURRENT_VERSION_UUID),
        booleanOrDefault(results, Columns.IS_DELETED, false));
  }
}
