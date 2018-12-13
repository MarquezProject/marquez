package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Urn;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final Instant updatedAt = results.getDate("updated_at").toInstant();
    final UUID namespaceUuid = UUID.fromString(results.getString("namespace_uuid"));
    final UUID dataSourceUuid = UUID.fromString(results.getString("data_source_uuid"));
    final Urn urn = new Urn(results.getString("urn"));
    final UUID currentVersionUuid = UUID.fromString(results.getString("current_version_uuid"));
    final String description = results.getString("description");

    return DatasetRow.builder()
        .uuid(uuid)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .namespaceUuid(namespaceUuid)
        .dataSourceUuid(dataSourceUuid)
        .urn(urn)
        .currentVersionUuid(currentVersionUuid)
        .description(description)
        .build();
  }
}
