package marquez.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import marquez.core.models.RunArgs;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class RunArgsRow implements RowMapper<RunArgs> {
  @Override
  public RunArgs map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new RunArgs(rs.getString("hex_digest"), rs.getString("args_json"));
  }
}
