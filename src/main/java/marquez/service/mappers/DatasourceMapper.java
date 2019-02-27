package marquez.service.mappers;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Datasource;

public class DatasourceMapper {
  private DatasourceMapper() {}

  public static Datasource map(@NonNull DatasourceRow dataSourceRow) {
    return new Datasource(
        dataSourceRow.getCreatedAt().get(),
        DatasourceName.fromString(dataSourceRow.getName()),
        ConnectionUrl.fromString(dataSourceRow.getConnectionUrl()));
  }

  public static List<Datasource> map(@NonNull List<DatasourceRow> datasourceRows) {
    return datasourceRows.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(
            datasourceRows.stream().map(row -> map(row)).collect(toList()));
  }
}
