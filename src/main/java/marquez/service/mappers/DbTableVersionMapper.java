package marquez.service.mappers;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.Mapper;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper implements Mapper<DbTableVersionRequest, DbTableVersion> {
  @Override
  public DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        request.getConnectionUrl(),
        request.getSchema(),
        request.getTable(),
        request.getDescription().orElse(null));
  }
}
