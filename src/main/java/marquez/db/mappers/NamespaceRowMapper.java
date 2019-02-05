package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class NamespaceRowMapper implements RowMapper<Namespace> {
  @Override
  public Namespace map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Namespace(
        UUID.fromString(rs.getString("guid")),
        rs.getTimestamp("created_at"),
        rs.getString("name"),
        rs.getString("current_ownership"),
        rs.getString("description"));
  }
}
