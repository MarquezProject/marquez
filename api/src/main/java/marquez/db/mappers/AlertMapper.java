package marquez.db.mappers;

import static marquez.db.Columns.jsonOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.AlertRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class AlertMapper implements RowMapper<AlertRow> {
  @Override
  public AlertRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new AlertRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        stringOrThrow(results, Columns.ENTITY_TYPE),
        stringOrThrow(results, Columns.ENTITY_UUID),
        stringOrThrow(results, Columns.TYPE),
        jsonOrNull(results, Columns.CONFIG));
  }
}
