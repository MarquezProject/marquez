/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static java.time.temporal.ChronoUnit.MILLIS;
import static marquez.common.models.RunState.NEW;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.Columns;
import marquez.service.models.Run;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class RunMapper implements RowMapper<Run> {
  private final String columnPrefix;

  public RunMapper() {
    this("");
  }

  public RunMapper(String columnPrefix) {
    this.columnPrefix = columnPrefix;
  }

  @Override
  public Run map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());
    Optional<Instant> startedAt =
        Optional.ofNullable(timestampOrNull(results, columnPrefix + Columns.STARTED_AT));
    Optional<Long> durationMs =
        Optional.ofNullable(timestampOrNull(results, columnPrefix + Columns.ENDED_AT))
            .flatMap(endedAt -> startedAt.map(s -> s.until(endedAt, MILLIS)));
    return new Run(
        RunId.of(uuidOrThrow(results, columnPrefix + Columns.ROW_UUID)),
        timestampOrThrow(results, columnPrefix + Columns.CREATED_AT),
        timestampOrThrow(results, columnPrefix + Columns.UPDATED_AT),
        timestampOrNull(results, columnPrefix + Columns.NOMINAL_START_TIME),
        timestampOrNull(results, columnPrefix + Columns.NOMINAL_END_TIME),
        stringOrNull(results, columnPrefix + Columns.CURRENT_RUN_STATE) == null
            ? NEW
            : RunState.valueOf(stringOrNull(results, columnPrefix + Columns.CURRENT_RUN_STATE)),
        columnNames.contains(columnPrefix + Columns.STARTED_AT)
            ? timestampOrNull(results, columnPrefix + Columns.STARTED_AT)
            : null,
        columnNames.contains(Columns.ENDED_AT)
            ? timestampOrNull(results, columnPrefix + Columns.ENDED_AT)
            : null,
        durationMs.orElse(null),
        toArgs(results, columnPrefix + Columns.ARGS),
        stringOrThrow(results, columnPrefix + Columns.NAMESPACE_NAME),
        stringOrThrow(results, columnPrefix + Columns.JOB_NAME),
        uuidOrNull(results, columnPrefix + Columns.JOB_VERSION),
        stringOrNull(results, columnPrefix + Columns.LOCATION),
        columnNames.contains(columnPrefix + Columns.INPUT_VERSIONS)
            ? toDatasetVersion(results, columnPrefix + Columns.INPUT_VERSIONS)
            : ImmutableList.of(),
        columnNames.contains(columnPrefix + Columns.OUTPUT_VERSIONS)
            ? toDatasetVersion(results, columnPrefix + Columns.OUTPUT_VERSIONS)
            : ImmutableList.of(),
        JobMapper.toContext(results, columnPrefix + Columns.CONTEXT),
        toFacetsOrNull(results, columnPrefix + Columns.FACETS));
  }

  private List<DatasetVersionId> toDatasetVersion(ResultSet rs, String column) throws SQLException {
    String dsString = rs.getString(column);
    if (dsString == null) {
      return Collections.emptyList();
    }
    return Utils.fromJson(dsString, new TypeReference<List<DatasetVersionId>>() {});
  }

  private Map<String, String> toArgs(ResultSet results, String column) throws SQLException {
    String args = stringOrNull(results, column);
    if (args == null) {
      return null;
    }
    return Utils.fromJson(args, new TypeReference<Map<String, String>>() {});
  }
}
