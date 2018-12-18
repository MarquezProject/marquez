package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.Mapper;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper implements Mapper<DbTableVersionRequest, DbTableVersion> {
  @Override
  public DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        ConnectionUrl.of(request.getConnectionUrl()),
        DbSchema.of(request.getSchema()),
        DbTable.of(request.getTable()),
        request.getDescription().map(Description::of).orElse(NO_DESCRIPTION));
  }
}
