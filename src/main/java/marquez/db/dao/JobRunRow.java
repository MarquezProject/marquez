package marquez.db.dao;

import marquez.api.JobRun;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class JobRunRow implements RowMapper<JobRun> {
  @Override
  public JobRun map(final ResultSet rs, final StatementContext ctx) throws SQLException {
    return new JobRun(
        UUID.fromString(rs.getString("guid")),
        rs.getTimestamp("created_at"),
        rs.getTimestamp("started_at"),
        rs.getTimestamp("ended_at"),
        UUID.fromString(rs.getString("job_run_definition_guid")),
        rs.getInt("current_state"));
  }
}
