/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetVersionId;
import marquez.db.Columns;
import marquez.db.models.ExtendedRunRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class ExtendedRunRowMapper implements RowMapper<ExtendedRunRow> {
  @Override
  public ExtendedRunRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());

    return new ExtendedRunRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        uuidOrNull(results, Columns.JOB_UUID),
        uuidOrNull(results, Columns.JOB_VERSION_UUID),
        uuidOrNull(results, Columns.PARENT_RUN_UUID),
        uuidOrThrow(results, Columns.RUN_ARGS_UUID),
        columnNames.contains(Columns.INPUT_VERSIONS)
            ? toDatasetVersion(results, Columns.INPUT_VERSIONS)
            : ImmutableList.of(),
        columnNames.contains(Columns.OUTPUT_VERSIONS)
            ? toDatasetVersion(results, Columns.OUTPUT_VERSIONS)
            : ImmutableList.of(),
        timestampOrNull(results, Columns.NOMINAL_START_TIME),
        timestampOrNull(results, Columns.NOMINAL_END_TIME),
        stringOrNull(results, Columns.CURRENT_RUN_STATE),
        columnNames.contains(Columns.STARTED_AT)
            ? timestampOrNull(results, Columns.STARTED_AT)
            : null,
        uuidOrNull(results, Columns.START_RUN_STATE_UUID),
        columnNames.contains(Columns.ENDED_AT) ? timestampOrNull(results, Columns.ENDED_AT) : null,
        uuidOrNull(results, Columns.END_RUN_STATE_UUID),
        columnNames.contains(Columns.ARGS) ? stringOrThrow(results, Columns.ARGS) : "",
        stringOrThrow(results, Columns.NAMESPACE_NAME),
        stringOrThrow(results, Columns.JOB_NAME));
  }

  private List<DatasetVersionId> toDatasetVersion(ResultSet rs, String column) throws SQLException {
    String dsString = rs.getString(column);
    if (dsString == null) {
      return Collections.emptyList();
    }
    return Utils.fromJson(dsString, new TypeReference<List<DatasetVersionId>>() {});
  }
}
