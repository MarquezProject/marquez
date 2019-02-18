package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.JobRunState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobRunStateRowMapper implements RowMapper<JobRunState> {
  @Override
  public JobRunState map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobRunState(
        results.getObject(Columns.ROW_UUID, UUID.class),
        results.getTimestamp(Columns.TRANSITIONED_AT),
        results.getObject(Columns.JOB_RUN_UUID, UUID.class),
        JobRunState.State.fromInt(results.getInt(Columns.RUN_STATE)));
  }
}
