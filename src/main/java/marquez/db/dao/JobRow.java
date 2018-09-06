package marquez.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import marquez.api.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRow implements RowMapper<Job> {
  @Override
  public Job map(final ResultSet rs, final StatementContext ctx) throws SQLException {
    return new Job(
        UUID.fromString(rs.getString("guid")),
        rs.getString("name"),
        "",
        new Timestamp(new Date(0).getTime()),
        "",
        "");
  }
}
