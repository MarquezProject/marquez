package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Db;
import marquez.common.models.Schema;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableInfoRowMapper implements RowMapper<DbTableInfoRow> {
  @Override
  public DbTableInfoRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final Db db = Db.of(results.getString("name"));
    final Schema schema = Schema.of(results.getString("schema"));

    return DbTableInfoRow.builder().uuid(uuid).createdAt(createdAt).db(db).schema(schema).build();
  }
}
