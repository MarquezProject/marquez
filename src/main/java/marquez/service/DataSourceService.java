package marquez.service;

import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DataSourceConnectionUrl;
import marquez.common.models.DataSourceName;
import marquez.db.DataSourceDao;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.DataSource;

@Slf4j
public class DataSourceService {
  private final DataSourceDao dataSourceDao;

  public DataSourceService(@NonNull final DataSourceDao dataSourceDao) {
    this.dataSourceDao = dataSourceDao;
  }

  public DataSource create(
      @NonNull DataSourceConnectionUrl connectionUrl, @NonNull DataSourceName name)
      throws UnexpectedException {
    return null;
  }

  public List<DataSource> list() {

    return null;
  }
}
