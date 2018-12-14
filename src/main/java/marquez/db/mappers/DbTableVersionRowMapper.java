package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Description;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableVersionRowMapper implements RowMapper<DbTableVersionRow> {
  @Override
  public DbTableVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DbTableVersionRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .datasetUuid(UUID.fromString(results.getString("dataset_uuid")))
        .dbTableInfoUuid(UUID.fromString(results.getString("db_table_info_uuid")))
        .description(Description.of(results.getString("description")))
        .build();
  }
}
