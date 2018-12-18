package marquez.service.mappers;

import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.DbTableInfoRow;
import marquez.service.models.DbTableVersion;

public final class DbTableInfoRowMapper {
  private DbTableInfoRowMapper() {}

  public static DbTableInfoRow map(@NonNull DbTableVersion dbTableVersion) {
    return DbTableInfoRow.builder()
        .uuid(UUID.randomUUID())
        .db(dbTableVersion.getConnectionUrl().getDb().getValue())
        .dbSchema(dbTableVersion.getDbSchema().getValue())
        .build();
  }
}
