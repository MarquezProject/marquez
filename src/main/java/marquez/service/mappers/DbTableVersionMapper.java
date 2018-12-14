package marquez.service.mappers;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.Mapper;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.Description;
import marquez.common.models.Schema;
import marquez.common.models.Table;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper implements Mapper<DbTableVersionRequest, DbTableVersion> {
  @Override
  public DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        ConnectionUrl.of(request.getConnectionUrl()),
        Schema.of(request.getSchema()),
        Table.of(request.getTable()),
        request.getDescription().map(Description::of).orElse(null));
  }
}
