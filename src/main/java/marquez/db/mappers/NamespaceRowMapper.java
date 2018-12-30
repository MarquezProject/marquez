package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.NamespaceRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class NamespaceRowMapper implements RowMapper<NamespaceRow> {
  @Override
  public NamespaceRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return NamespaceRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .updatedAt(results.getDate("updated_at").toInstant())
        .namespace(results.getString("namespace"))
        .description(results.getString("description"))
        .currentOwnership(results.getString("current_ownership"))
        .build();
  }
}
