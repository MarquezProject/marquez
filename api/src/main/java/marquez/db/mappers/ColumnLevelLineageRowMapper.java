/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.TRANSFORMATION_DESCRIPTION;
import static marquez.db.Columns.TRANSFORMATION_TYPE;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.ColumnLevelLineageRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ColumnLevelLineageRowMapper implements RowMapper<ColumnLevelLineageRow> {

  @Override
  public ColumnLevelLineageRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new ColumnLevelLineageRow(
        uuidOrThrow(results, Columns.OUTPUT_DATASET_VERSION_UUID),
        uuidOrThrow(results, Columns.OUTPUT_DATASET_FIELD_UUID),
        uuidOrThrow(results, Columns.INPUT_DATASET_VERSION_UUID),
        uuidOrThrow(results, Columns.INPUT_DATASET_FIELD_UUID),
        stringOrThrow(results, TRANSFORMATION_DESCRIPTION),
        stringOrThrow(results, TRANSFORMATION_TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT));
  }
}