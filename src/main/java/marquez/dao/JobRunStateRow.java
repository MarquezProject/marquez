package marquez.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.core.models.JobRunState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRunStateRow implements RowMapper<JobRunState> {
  @Override
  public JobRunState map(final ResultSet rs, final StatementContext ctx) throws SQLException {
    return new JobRunState(
        UUID.fromString(rs.getString("guid")),
        rs.getTimestamp("transitioned_at"),
        UUID.fromString(rs.getString("job_run_guid")),
        JobRunState.State.fromInt(rs.getInt("state")));
  }
}
