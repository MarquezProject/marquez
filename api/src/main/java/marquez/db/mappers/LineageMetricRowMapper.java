package marquez.db.mappers;

import static marquez.db.Columns.intOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class LineageMetricRowMapper implements RowMapper<LineageMetric> {
  @Override
  public LineageMetric map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new LineageMetric(
        timestampOrThrow(results, Columns.START_INTERVAL),
        timestampOrThrow(results, Columns.END_INTERVAL),
        intOrThrow(results, Columns.FAIL),
        intOrThrow(results, Columns.START),
        intOrThrow(results, Columns.COMPLETE),
        intOrThrow(results, Columns.ABORT));
  }
}
