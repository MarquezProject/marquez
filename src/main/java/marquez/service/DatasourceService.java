package marquez.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.MarquezException;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.db.DatasourceDao;
import marquez.db.models.DatasourceRow;
import marquez.service.mappers.DatasourceMapper;
import marquez.service.mappers.DatasourceRowMapper;
import marquez.service.models.Datasource;

@Slf4j
public class DatasourceService {
  private final DatasourceDao datasourceDao;

  public DatasourceService(@NonNull final DatasourceDao datasourceDao) {
    this.datasourceDao = datasourceDao;
  }

  public Datasource create(@NonNull ConnectionUrl connectionUrl, @NonNull DatasourceName name)
      throws MarquezException {
    DatasourceRow datasourceRow = DatasourceRowMapper.map(connectionUrl, name);
    datasourceDao.insert(datasourceRow);
    final Optional<DatasourceRow> datasourceRowIfFound =
        datasourceDao.findBy(datasourceRow.getUuid());
    try {
      return datasourceRowIfFound.map(DatasourceMapper::map).orElseThrow(MarquezException::new);
    } catch (MarquezException e) {
      log.error(e.getMessage());
      throw new MarquezException();
    }
  }

  public List<Datasource> getAll(@NonNull Integer limit, @NonNull Integer offset) {
    List<DatasourceRow> datasources = datasourceDao.findAll(limit, offset);
    return Collections.unmodifiableList(DatasourceMapper.map(datasources));
  }
}
