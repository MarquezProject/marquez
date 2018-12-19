package marquez.service.mappers;

import static marquez.common.models.Description.NO_VALUE;

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
        .urn(dbTableVersion.toDatasetUrn(namespace).getValue())
        .description(
            dbTableVersion.getDescription().map((desc) -> desc.getValue()).orElse(NO_VALUE))
        .build();
  }
}
