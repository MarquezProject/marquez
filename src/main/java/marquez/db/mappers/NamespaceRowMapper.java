package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NamespaceRowMapper implements RowMapper<Namespace> {
  @Override
  public Namespace map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Namespace(
        results.getObject(Columns.ROW_UUID, UUID.class),
        results.getTimestamp(Columns.CREATED_AT),
        results.getString(Columns.NAME),
        results.getString(Columns.CURRENT_OWNER_NAME),
        results.getString(Columns.DESCRIPTION));
  }
}
