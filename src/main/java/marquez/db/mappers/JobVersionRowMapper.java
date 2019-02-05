package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.service.models.JobVersion;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobVersionRowMapper implements RowMapper<JobVersion> {
  @Override
  public JobVersion map(ResultSet rs, StatementContext ctx) throws SQLException {
    String rawLastRunGuid = rs.getString("latest_run_guid");
    UUID latestJobRunGuid = null;
    if (rawLastRunGuid != null) {
      try {
        latestJobRunGuid = UUID.fromString(rs.getString("latest_run_guid"));
      } catch (SQLException | IllegalArgumentException e) {
        latestJobRunGuid = null;
      }
    }

    return new JobVersion(
        UUID.fromString(rs.getString("guid")),
        UUID.fromString(rs.getString("job_guid")),
        rs.getString("uri"),
        UUID.fromString(rs.getString("version")),
        latestJobRunGuid,
        rs.getTimestamp("created_at"),
        rs.getTimestamp("updated_at"));
  }
}
