package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import marquez.service.models.RunArgs;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRunArgsRowMapper implements RowMapper<RunArgs> {
  @Override
  public RunArgs map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new RunArgs(
        rs.getString("hex_digest"), rs.getString("args_json"), rs.getTimestamp("created_at"));
  }
}
