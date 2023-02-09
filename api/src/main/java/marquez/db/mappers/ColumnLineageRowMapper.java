/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.TRANSFORMATION_DESCRIPTION;
import static marquez.db.Columns.TRANSFORMATION_TYPE;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.ColumnLineageRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ColumnLineageRowMapper implements RowMapper<ColumnLineageRow> {

  @Override
  public ColumnLineageRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new ColumnLineageRow(
        uuidOrThrow(results, Columns.OUTPUT_DATASET_VERSION_UUID),
        uuidOrThrow(results, Columns.OUTPUT_DATASET_FIELD_UUID),
        uuidOrThrow(results, Columns.INPUT_DATASET_VERSION_UUID),
        uuidOrThrow(results, Columns.INPUT_DATASET_FIELD_UUID),
        stringOrNull(results, TRANSFORMATION_DESCRIPTION),
        stringOrNull(results, TRANSFORMATION_TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT));
  }
}
