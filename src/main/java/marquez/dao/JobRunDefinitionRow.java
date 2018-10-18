package marquez.dao;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import marquez.api.JobRunDefinition;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRunDefinitionRow implements RowMapper<JobRunDefinition> {
  @Override
  public JobRunDefinition map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new JobRunDefinition(
        UUID.fromString(rs.getString("guid")),
        rs.getString("name"),
        rs.getString("current_owner_name"),
        URI.create(rs.getString("uri")),
        UUID.fromString(rs.getString("job_version_guid")),
        rs.getString("run_args_json"),
        rs.getInt("nominal_start_time"),
        rs.getInt("nominal_end_time"));
  }
}
