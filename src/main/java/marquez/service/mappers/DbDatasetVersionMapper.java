package marquez.service.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.CreateDbDatasetVersionRequest;
import marquez.common.Mapper;
import marquez.service.models.DbDatasetVersion;

public final class DbDatasetVersionMapper
    implements Mapper<CreateDbDatasetVersionRequest, DbDatasetVersion> {
  @Override
  public DbDatasetVersion map(CreateDbDatasetVersionRequest request) {
    requireNonNull(request, "request must not be null");
    return new DbDatasetVersion(
        request.getConnectionUrl(),
        request.getSchema(),
        request.getTable(),
        request.getDescription().orElse(null));
  }
}
