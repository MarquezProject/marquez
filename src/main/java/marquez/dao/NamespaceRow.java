package marquez.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.core.models.Namespace;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceRow implements RowMapper<Namespace> {
  static final Logger logger = LoggerFactory.getLogger(NamespaceDAO.class);

  @Override
  public Namespace map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Namespace(
        UUID.fromString(rs.getString("guid")),
        rs.getTimestamp("created_at"),
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("current_ownership"));
  }
}
