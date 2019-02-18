package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.Job;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class JobRowMapper implements RowMapper<Job> {
  @Override
  public Job map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Job(
        results.getObject(Columns.ROW_UUID, UUID.class),
        results.getString(Columns.NAME),
        results.getString(Columns.LOCATION),
        results.getObject(Columns.NAMESPACE_UUID, UUID.class),
        results.getString(Columns.DESCRIPTION),
        Columns.toList(results.getArray(Columns.INPUT_DATASET_URNS)),
        Columns.toList(results.getArray(Columns.OUTPUT_DATASET_URNS)),
        results.getTimestamp(Columns.CREATED_AT));
  }
}
