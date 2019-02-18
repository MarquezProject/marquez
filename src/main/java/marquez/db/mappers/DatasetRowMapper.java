package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.Columns;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DatasetRow.builder()
        .uuid(results.getObject(Columns.ROW_UUID, UUID.class))
        .createdAt(results.getTimestamp(Columns.CREATED_AT).toInstant())
        .updatedAt(Columns.toInstantOrNull(results.getTimestamp(Columns.UPDATED_AT)))
        .namespaceUuid(results.getObject(Columns.NAMESPACE_UUID, UUID.class))
        .dataSourceUuid(results.getObject(Columns.DATA_SOURCE_UUID, UUID.class))
        .urn(results.getString(Columns.URN))
        .description(results.getString(Columns.DESCRIPTION))
        .currentVersion(Columns.toUuidOrNull(results.getString(Columns.CURRENT_VERSION_UUID)))
        .build();
  }
}
