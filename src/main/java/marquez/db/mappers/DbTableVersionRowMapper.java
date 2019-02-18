package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DbTableVersionRowMapper implements RowMapper<DbTableVersionRow> {
  @Override
  public DbTableVersionRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DbTableVersionRow.builder()
        .uuid(results.getObject(Columns.ROW_UUID, UUID.class))
        .createdAt(results.getTimestamp(Columns.CREATED_AT).toInstant())
        .datasetUuid(results.getObject(Columns.DATASET_UUID, UUID.class))
        .dbTableInfoUuid(results.getObject(Columns.DB_TABLE_INFO_UUID, UUID.class))
        .dbTable(results.getString(Columns.DB_TABLE_NAME))
        .build();
  }
}
