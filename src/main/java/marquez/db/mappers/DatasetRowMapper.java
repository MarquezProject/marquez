package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DatasetRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .updatedAt(results.getDate("updated_at").toInstant())
        .namespaceUuid(UUID.fromString(results.getString("namespace_uuid")))
        .dataSourceUuid(UUID.fromString(results.getString("data_source_uuid")))
        .urn(results.getString("urn"))
        .description(results.getString("description"))
        .currentVersion(UUID.fromString(results.getString("current_version")))
        .build();
  }
}
