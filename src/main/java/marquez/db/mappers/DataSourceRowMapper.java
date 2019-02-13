package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DataSourceRowMapper implements RowMapper<DataSourceRow> {
  @Override
  public DataSourceRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DataSourceRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .name(results.getString("name"))
        .connectionUrl(results.getString("connection_url"))
        .build();
  }
}
