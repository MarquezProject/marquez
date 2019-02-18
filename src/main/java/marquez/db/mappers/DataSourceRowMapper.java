package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DataSourceRowMapper implements RowMapper<DataSourceRow> {
  @Override
  public DataSourceRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DataSourceRow.builder()
        .uuid(results.getObject(Columns.ROW_UUID, UUID.class))
        .createdAt(results.getTimestamp(Columns.CREATED_AT).toInstant())
        .name(results.getString(Columns.NAME))
        .connectionUrl(results.getString(Columns.CONNECTION_URL))
        .build();
  }
}
