/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetVersionRowMapper implements RowMapper<DatasetVersionRow> {
  @Override
  public DatasetVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new DatasetVersionRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        uuidOrThrow(results, Columns.DATASET_UUID),
        uuidOrThrow(results, Columns.VERSION),
        stringOrNull(results, Columns.LIFECYCLE_STATE),
        uuidOrNull(results, Columns.RUN_UUID));
  }
}
