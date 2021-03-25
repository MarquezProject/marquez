package marquez.db.mappers;

import static marquez.db.Columns.mapOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.urlOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.JobVersion;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public class JobVersionMapper implements RowMapper<JobVersion> {
  @Override
  public JobVersion map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobVersion(
        JobId.of(
            NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
            JobName.of(stringOrThrow(results, Columns.JOB_NAME))),
        JobName.of(stringOrThrow(results, Columns.JOB_NAME)),
        timestampOrThrow(results, Columns.CREATED_AT),
        Version.of(uuidOrThrow(results, Columns.ROW_UUID)),
        urlOrNull(results, Columns.LOCATION),
        mapOrNull(results, Columns.CONTEXT));
  }
}
