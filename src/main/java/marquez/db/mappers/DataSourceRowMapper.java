package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DataSourceRowMapper implements RowMapper<DataSourceRow> {
  @Override
  public DataSourceRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final DataSource dataSource = DataSource.of(results.getString("data_source"));
    final ConnectionUrl connectionUrl = ConnectionUrl.of(results.getString("connection_url"));

    return DataSourceRow.builder()
        .uuid(uuid)
        .createdAt(createdAt)
        .dataSource(dataSource)
        .connectionUrl(connectionUrl)
        .build();
  }
}
