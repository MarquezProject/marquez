package marquez.service.mappers;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSourceName;
import marquez.db.models.DataSourceRow;
import marquez.service.models.DataSource;

public class DataSourceMapper {
  private DataSourceMapper() {}

  public static DataSource map(@NonNull DataSourceRow dataSourceRow) {
    return new DataSource(
        dataSourceRow.getCreatedAt().get(),
        DataSourceName.fromString(dataSourceRow.getName()),
        ConnectionUrl.fromString(dataSourceRow.getConnectionUrl()));
  }

  public static List<DataSource> map(@NonNull List<DataSourceRow> dataSourceRows) {
    return dataSourceRows.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(
            dataSourceRows.stream().map(row -> map(row)).collect(toList()));
  }
}
