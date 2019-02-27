package marquez.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSourceName;
import marquez.db.DataSourceDao;
import marquez.db.models.DataSourceRow;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.mappers.DataSourceMapper;
import marquez.service.models.DataSource;

@Slf4j
public class DataSourceService {
  private final DataSourceDao dataSourceDao;

  public DataSourceService(@NonNull final DataSourceDao dataSourceDao) {
    this.dataSourceDao = dataSourceDao;
  }

  public DataSource create(@NonNull ConnectionUrl connectionUrl, @NonNull DataSourceName name)
      throws UnexpectedException {
    dataSourceDao.insert(
        new DataSourceRow(
            UUID.randomUUID(), name.getValue(), connectionUrl.getDataSource().getValue(), null));
  }

  public List<DataSource> list(@NonNull Integer limit, @NonNull Integer offset) {
    List<DataSourceRow> dataSources = dataSourceDao.findAll(limit, offset);
    return Collections.unmodifiableList(DataSourceMapper.map(dataSources));
  }
}
