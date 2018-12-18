package marquez.service.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;

import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.Namespace;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.service.models.DbTableVersion;

public final class DatasetRowMapper {
  private DatasetRowMapper() {}

  public static DatasetRow map(
      @NonNull Namespace namespace,
      @NonNull DataSourceRow dataSourceRow,
      @NonNull DbTableVersion dbTableVersion) {
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .dataSourceUuid(dataSourceRow.getUuid())
        .urn(dbTableVersion.toDatasetUrn(namespace))
        .description(dbTableVersion.getDescription().orElse(NO_DESCRIPTION))
        .build();
  }
}
