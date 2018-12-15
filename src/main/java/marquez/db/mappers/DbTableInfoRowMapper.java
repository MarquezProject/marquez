package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Db;
import marquez.common.models.DbSchema;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableInfoRowMapper implements RowMapper<DbTableInfoRow> {
  @Override
  public DbTableInfoRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DbTableInfoRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .db(Db.of(results.getString("db")))
        .dbSchema(DbSchema.of(results.getString("db_schema")))
        .build();
  }
}
