package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTable;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper {
  private DbTableVersionMapper() {}

  public static DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        ConnectionUrl.of(request.getConnectionUrl()),
        DbSchemaName.of(request.getSchema()),
        DbTable.of(request.getTable()),
        request.getDescription().map(Description::of).orElse(NO_DESCRIPTION));
  }
}
