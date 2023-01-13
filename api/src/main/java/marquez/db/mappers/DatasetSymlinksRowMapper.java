/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.booleanOrDefault;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetSymlinkRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class DatasetSymlinksRowMapper implements RowMapper<DatasetSymlinkRow> {

  @Override
  public DatasetSymlinkRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new DatasetSymlinkRow(
        uuidOrThrow(results, Columns.DATASET_UUID),
        stringOrThrow(results, Columns.NAME),
        uuidOrThrow(results, Columns.NAMESPACE_UUID),
        stringOrNull(results, Columns.TYPE),
        booleanOrDefault(results, Columns.IS_PRIMARY, false),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT));
  }
}
