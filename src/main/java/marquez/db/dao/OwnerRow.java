package marquez.db.dao;

import marquez.api.Owner;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OwnerRow implements RowMapper<Owner> {
  @Override
  public Owner map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Owner(rs.getString("name"));
  }
}
