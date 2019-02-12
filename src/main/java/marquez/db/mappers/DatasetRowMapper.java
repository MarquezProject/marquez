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
        .uuid(UUID.fromString(results.getString("guid")))
        .createdAt(results.getTimestamp("created_at").toInstant())
        .updatedAt(
            results.getTimestamp("updated_at") == null
                ? null
                : results.getTimestamp("updated_at").toInstant())
        .namespaceUuid(UUID.fromString(results.getString("namespace_guid")))
        .dataSourceUuid(UUID.fromString(results.getString("datasource_uuid")))
        .urn(results.getString("urn"))
        .description(results.getString("description"))
        .currentVersion(
            results.getString("current_version_uuid") == null
                ? null
                : UUID.fromString(results.getString("current_version_uuid")))
        .build();
  }
}
