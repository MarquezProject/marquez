/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.TRANSFORMATION_DESCRIPTION;
import static marquez.db.Columns.TRANSFORMATION_TYPE;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.db.Columns;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.InputFieldNodeData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.jdbc.PgArray;

@Slf4j
public class ColumnLineageNodeDataMapper implements RowMapper<ColumnLineageNodeData> {

  private static final ObjectMapper MAPPER = Utils.getMapper();

  @Override
  public ColumnLineageNodeData map(ResultSet results, StatementContext ctx) throws SQLException {
    return new ColumnLineageNodeData(
        stringOrThrow(results, Columns.NAMESPACE_NAME),
        stringOrThrow(results, Columns.DATASET_NAME),
        uuidOrThrow(results, Columns.DATASET_VERSION_UUID),
        stringOrThrow(results, Columns.FIELD_NAME),
        stringOrThrow(results, Columns.TYPE),
        stringOrNull(results, TRANSFORMATION_DESCRIPTION),
        stringOrNull(results, TRANSFORMATION_TYPE),
        toInputFields(results, "inputFields"));
  }

  public static ImmutableList<InputFieldNodeData> toInputFields(ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return ImmutableList.of();
    }

    PgArray pgArray = (PgArray) results.getObject(column);
    Object[] deserializedArray = (Object[]) pgArray.getArray();

    return ImmutableList.copyOf(
        Arrays.asList(deserializedArray).stream()
            .map(o -> (String[]) o)
            .map(arr -> new InputFieldNodeData(arr[0], arr[1], UUID.fromString(arr[2]), arr[3]))
            .collect(Collectors.toList()));
  }
}
