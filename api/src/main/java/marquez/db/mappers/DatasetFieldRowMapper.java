/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidArrayOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetFieldRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetFieldRowMapper implements RowMapper<DatasetFieldRow> {
  @Override
  public DatasetFieldRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());

    return new DatasetFieldRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        stringOrNull(results, Columns.TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrThrow(results, Columns.DATASET_UUID),
        stringOrThrow(results, Columns.NAME),
        columnNames.contains(Columns.TAG_UUIDS)
            ? uuidArrayOrThrow(results, Columns.TAG_UUIDS)
            : ImmutableList.of(),
        stringOrNull(results, Columns.DESCRIPTION));
  }
}
