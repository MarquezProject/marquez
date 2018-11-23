package marquez.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSS-XX");
    Timestamp createdAt;
    try {
      createdAt = new Timestamp(dateFormat.parse(rs.getString("created_at")).getTime());
    } catch (ParseException e) {
      logger.error("failed to parse timestamp", e);
      createdAt = null;
    }

    return new Namespace(
        UUID.fromString(rs.getString("guid")),
        createdAt,
        rs.getString("name"),
        rs.getString("description"),
        rs.getString("current_ownership"));
  }
}
