package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.JobVersion;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobVersionRowMapper implements RowMapper<JobVersion> {
  @Override
  public JobVersion map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobVersion(
        results.getObject(Columns.ROW_UUID, UUID.class),
        results.getObject(Columns.JOB_UUID, UUID.class),
        results.getString(Columns.LOCATION),
        results.getObject(Columns.VERSION, UUID.class),
        Columns.toUuidOrNull(results.getString(Columns.LATEST_JOB_RUN_UUID)),
        results.getTimestamp(Columns.CREATED_AT),
        results.getTimestamp(Columns.UPDATED_AT));
  }
}
