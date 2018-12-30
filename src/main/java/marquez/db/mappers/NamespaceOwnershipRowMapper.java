package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.NamespaceOwnershipRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NamespaceOwnershipRowMapper implements RowMapper<NamespaceOwnershipRow> {
  @Override
  public NamespaceOwnershipRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return NamespaceOwnershipRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .startedAt(results.getDate("started_at").toInstant())
        .endedAt(results.getDate("ended_at").toInstant())
        .namespaceUuid(UUID.fromString(results.getString("namespace_uuid")))
        .ownerUuid(UUID.fromString(results.getString("owner_uuid")))
        .build();
  }
}
