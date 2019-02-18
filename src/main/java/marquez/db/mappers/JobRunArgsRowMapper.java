package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.RunArgs;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobRunArgsRowMapper implements RowMapper<RunArgs> {
  @Override
  public RunArgs map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new RunArgs(
        results.getString(Columns.CHECKSUM),
        results.getString(Columns.RUN_ARGS),
        results.getTimestamp(Columns.CREATED_AT));
  }
}
