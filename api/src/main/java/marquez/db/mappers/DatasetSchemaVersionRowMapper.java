/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetSchemaVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetSchemaVersionRowMapper implements RowMapper<DatasetSchemaVersionRow> {
  @Override
  public DatasetSchemaVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new DatasetSchemaVersionRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        uuidOrThrow(results, Columns.DATASET_UUID),
        timestampOrThrow(results, Columns.CREATED_AT));
  }
}
