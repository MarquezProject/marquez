package marquez.service.mappers;

import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.models.DbTableVersion;

public final class DbTableVersionRowMapper {
  private DbTableVersionRowMapper() {}

  public static DbTableVersionRow map(
      @NonNull DatasetRow datasetRow,
      @NonNull DbTableInfoRow dbTableInfoRow,
      @NonNull DbTableVersion dbTableVersion) {
    return DbTableVersionRow.builder()
        .uuid(UUID.randomUUID())
        .datasetUuid(datasetRow.getUuid())
        .dbTableInfoUuid(dbTableInfoRow.getUuid())
        .dbTable(dbTableVersion.getDbTable().getValue())
        .build();
  }
}
