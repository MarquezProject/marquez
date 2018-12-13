package marquez.db.mappers;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableInfoRowMapper implements RowMapper<DbTableInfoRow> {
  @Override
  public DbTableInfoRow map(ResultSet results, StatementContext context) throws SQLException {
    requireNonNull(results, "results must not be null");

    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final String db = results.getString("name");
    final String schema = results.getString("schema");

    return DbTableInfoRow.builder().uuid(uuid).createdAt(createdAt).db(db).schema(schema).build();
  }
}
