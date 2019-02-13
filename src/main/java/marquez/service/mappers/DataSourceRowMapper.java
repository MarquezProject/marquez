package marquez.service.mappers;

import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DataSourceRow;
import marquez.service.models.DbTableVersion;

public final class DataSourceRowMapper {
  private DataSourceRowMapper() {}

  public static DataSourceRow map(@NonNull DbTableVersion dbTableVersion) {
    return DataSourceRow.builder()
        .uuid(UUID.randomUUID())
        .name(dbTableVersion.getConnectionUrl().getDataSource().getValue())
        .connectionUrl(dbTableVersion.getConnectionUrl().getRawValue())
        .build();
  }
}
