package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.JobRun;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobRunRowMapper implements RowMapper<JobRun> {
  @Override
  public JobRun map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobRun(
        results.getObject(Columns.ROW_UUID, UUID.class),
        results.getInt(Columns.CURRENT_RUN_STATE),
        results.getObject(Columns.JOB_VERSION_UUID, UUID.class),
        results.getString(Columns.RUN_ARGS_CHECKSUM),
        results.getString(Columns.RUN_ARGS),
        results.getTimestamp(Columns.NOMINAL_START_TIME),
        results.getTimestamp(Columns.NOMINAL_END_TIME),
        results.getTimestamp(Columns.CREATED_AT));
  }
}
