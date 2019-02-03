package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import marquez.service.models.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobRowMapper implements RowMapper<Job> {

  @Override
  public Job map(final ResultSet rs, final StatementContext ctx) throws SQLException {
    return new Job(
        UUID.fromString(rs.getString("guid")),
        rs.getString("name"),
        rs.getString("uri"),
        UUID.fromString(rs.getString("namespace_guid")),
        rs.getString("description"),
        (rs.getArray("input_dataset_urns") != null)
            ? Arrays.asList((String[]) rs.getArray("input_dataset_urns").getArray())
            : Collections.<String>emptyList(),
        (rs.getArray("output_dataset_urns") != null)
            ? Arrays.asList((String[]) rs.getArray("output_dataset_urns").getArray())
            : Collections.<String>emptyList(),
        rs.getTimestamp("created_at"));
  }
}
