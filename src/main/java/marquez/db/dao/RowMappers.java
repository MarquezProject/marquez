package marquez.db.dao;

import marquez.api.Job;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

final class RowMappers {
  private RowMappers() {}

  static final class JobRow implements RowMapper<Job> {
    @Override
    public Job map(final ResultSet rs, final StatementContext ctx) throws SQLException {
      // TODO
      throw new UnsupportedOperationException();
    }
  }
}
