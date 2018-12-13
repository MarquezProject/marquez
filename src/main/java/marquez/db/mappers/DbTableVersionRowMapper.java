package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableVersionRowMapper implements RowMapper<DbTableVersionRow> {
  @Override
  public DbTableVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final UUID datasetUuid = UUID.fromString(results.getString("dataset_uuid"));
    final UUID dbUuid = UUID.fromString(results.getString("db_table_info_uuid"));
    final String description = results.getString("description");

    return DbTableVersionRow.builder()
        .uuid(uuid)
        .createdAt(createdAt)
        .datasetUuid(datasetUuid)
        .dbTableInfoUuid(dbUuid)
        .description(description)
        .build();
  }
}
