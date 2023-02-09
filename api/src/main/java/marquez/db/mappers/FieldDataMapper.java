/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.InputFieldData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class FieldDataMapper implements RowMapper<InputFieldData> {
  @Override
  public InputFieldData map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new InputFieldData(
        stringOrNull(results, Columns.NAMESPACE_NAME),
        stringOrNull(results, Columns.DATASET_NAME),
        stringOrNull(results, Columns.FIELD_NAME),
        uuidOrThrow(results, Columns.DATASET_UUID),
        uuidOrThrow(results, Columns.DATASET_FIELD_UUID),
        uuidOrThrow(results, Columns.DATASET_VERSION_UUID));
  }
}
