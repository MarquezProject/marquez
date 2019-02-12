package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;

import lombok.NonNull;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionMapper {
  private DbTableVersionMapper() {}

  public static DbTableVersion map(@NonNull DbTableVersionRequest request) {
    return new DbTableVersion(
        ConnectionUrl.fromString(request.getConnectionUrl()),
        DbSchemaName.fromString(request.getSchema()),
        DbTableName.fromString(request.getTable()),
        request.getDescription().map(Description::fromString).orElse(NO_DESCRIPTION));
  }
}
