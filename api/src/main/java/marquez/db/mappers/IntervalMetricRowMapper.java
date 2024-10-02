/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.intOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.IntervalMetric;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class IntervalMetricRowMapper implements RowMapper<IntervalMetric> {
  @Override
  public IntervalMetric map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new IntervalMetric(
        timestampOrThrow(results, Columns.START_INTERVAL),
        timestampOrThrow(results, Columns.END_INTERVAL),
        intOrThrow(results, Columns.COUNT));
  }
}
