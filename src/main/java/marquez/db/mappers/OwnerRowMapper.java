package marquez.db.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.OwnerRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class OwnerRowMapper implements RowMapper<OwnerRow> {
  @Override
  public OwnerRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return OwnerRow.builder()
        .uuid(UUID.fromString(results.getString("uuid")))
        .createdAt(results.getDate("created_at").toInstant())
        .owner(results.getString("owner"))
        .build();
  }
}
