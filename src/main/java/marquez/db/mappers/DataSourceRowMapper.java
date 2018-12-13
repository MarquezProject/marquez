package marquez.db.mappers;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DataSourceType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DataSourceRowMapper implements RowMapper<DataSourceRow> {
  @Override
  public DataSourceRow map(ResultSet results, StatementContext context) throws SQLException {
    requireNonNull(results, "results must not be null");

    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final DataSourceType type = DataSourceType.valueOf(results.getString("type"));
    final String connectionUrl = results.getString("connection_url");

    return DataSourceRow.builder()
        .uuid(uuid)
        .createdAt(createdAt)
        .type(type)
        .connectionUrl(connectionUrl)
        .build();
  }
}
