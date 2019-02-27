package marquez.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.db.DatasourceDao;
import marquez.db.models.DatasourceRow;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.mappers.DatasourceMapper;
import marquez.service.models.Datasource;

@Slf4j
public class DatasourceService {
  private final DatasourceDao dataSourceDao;

  public DatasourceService(@NonNull final DatasourceDao dataSourceDao) {
    this.dataSourceDao = dataSourceDao;
  }

  public Datasource create(@NonNull ConnectionUrl connectionUrl, @NonNull DatasourceName name)
      throws UnexpectedException {
    UUID datasourceUUID = UUID.randomUUID();
    dataSourceDao.insert(
        new DatasourceRow(datasourceUUID, name.getValue(), connectionUrl.getRawValue(), null));
    final Optional<DatasourceRow> datasourceRowIfFound = dataSourceDao.findBy(datasourceUUID);
    try {
      return datasourceRowIfFound.map(DatasourceMapper::map).orElseThrow(UnexpectedException::new);
    } catch (UnexpectedException e) {
      log.error(e.getMessage());
      throw new UnexpectedException();
    }
  }

  public List<Datasource> list(@NonNull Integer limit, @NonNull Integer offset) {
    List<DatasourceRow> dataSources = dataSourceDao.findAll(limit, offset);
    return Collections.unmodifiableList(DatasourceMapper.map(dataSources));
  }
}
