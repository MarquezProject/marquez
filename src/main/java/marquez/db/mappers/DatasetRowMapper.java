package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Description;
import marquez.common.models.Urn;
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
        .urn(Urn.of(results.getString("urn")))
        .currentVersionUuid(UUID.fromString(results.getString("current_version_uuid")))
        .description(Description.of(results.getString("description")))
        .build();
  }
}
