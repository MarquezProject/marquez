package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.service.models.JobRun;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRunRowMapper implements RowMapper<JobRun> {
  @Override
  public JobRun map(final ResultSet rs, final StatementContext ctx) throws SQLException {
    return new JobRun(
        UUID.fromString(rs.getString("guid")),
        rs.getInt("current_state"),
        UUID.fromString(rs.getString("job_version_guid")),
        rs.getString("job_run_args_hex_digest"),
        rs.getString("args_json"),
        rs.getTimestamp("nominal_start_time"),
        rs.getTimestamp("nominal_end_time"),
        rs.getTimestamp("created_at"));
  }
}
