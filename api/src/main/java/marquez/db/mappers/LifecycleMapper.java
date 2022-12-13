package marquez.db.mappers;

import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.service.LifecycleService.Lifecycle;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class LifecycleMapper implements RowMapper<Lifecycle.Event> {
  @Override
  public Lifecycle.Event map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Lifecycle.Event(
        Lifecycle.State.valueOf(stringOrThrow(results, "state")),
        stringOrThrow(results, "namespace_name"),
        timestampOrThrow(results, "transitioned_at"),
        stringOrThrow(results, "message"));
  }
}
