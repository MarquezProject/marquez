package marquez.db.mappers;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import marquez.common.Urn;
import marquez.db.models.DatasetRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowMapper implements RowMapper<DatasetRow> {
  @Override
  public DatasetRow map(ResultSet results, StatementContext context) throws SQLException {
    requireNonNull(results, "results must not be null");

    final UUID uuid = UUID.fromString(results.getString("uuid"));
    final UUID namespaceUuid = UUID.fromString(results.getString("namespace_uuid"));
    final UUID dataSourceUuid = UUID.fromString(results.getString("data_source_uuid"));
    final UUID currentVersion = UUID.fromString(results.getString("current_version"));
    final Urn urn = new Urn(results.getString("urn"));
    final Instant createdAt = results.getDate("created_at").toInstant();
    final Instant updatedAt = results.getDate("updated_at").toInstant();
    final String description = results.getString("description");

    return new DatasetRow(
        uuid,
        namespaceUuid,
        dataSourceUuid,
        currentVersion,
        urn,
        createdAt,
        updatedAt,
        description);
  }
}
