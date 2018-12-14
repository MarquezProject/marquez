package marquez.db.mappers;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import marquez.common.models.DataSource;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DataSourceRowMapper implements RowMapper<DataSourceRow> {
  @Override
  public DataSourceRow map(ResultSet results, StatementContext context) throws SQLException {
    requireNonNull(results, "results must not be null");

    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final DataSource dataSource = DataSource.of(results.getString("type"));
    final String connectionUrl = results.getString("connection_url");

    return DataSourceRow.builder()
        .uuid(uuid)
        .createdAt(createdAt)
        .dataSource(dataSource)
        .connectionUrl(connectionUrl)
        .build();
  }
}
