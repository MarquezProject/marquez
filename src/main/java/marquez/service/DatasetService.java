package marquez.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import marquez.common.models.Namespace;
import marquez.common.models.Urn;
import marquez.db.DatasetDao;
import marquez.db.models.DatasetRow;
import marquez.service.mappers.DatasetMapper;
import marquez.service.models.Dataset;
import marquez.service.models.DbTableVersion;

public class DatasetService {
  private final DatasetMapper datasetMapper = new DatasetMapper();
  private final DatasetDao datasetDao;

  public DatasetService(@NonNull final DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  public Dataset create(@NonNull Namespace namespace, @NonNull DbTableVersion dbTableVersion) {
    final DatasetRow row =
        datasetDao.insert(
            namespace,
            dbTableVersion.getDataSource(),
            dbTableVersion.getConnectionUrl(),
            dbTableVersion.getDb(),
            dbTableVersion.getSchema(),
            dbTableVersion.getTable(),
            dbTableVersion.getDescription().orElse(null));
    return datasetMapper.map(row);
  }

  public Optional<Dataset> get(@NonNull Namespace namespace, @NonNull Urn urn) {
    final Optional<DatasetRow> row = datasetDao.findBy(urn);
    return row.map(datasetMapper::map);
  }

  public List<Dataset> getAll(
      @NonNull Namespace namespace, @NonNull Integer limit, @NonNull Integer offset) {
    final List<DatasetRow> datasetRows = datasetDao.findAll(namespace, limit, offset);
    final List<Dataset> datasets = datasetMapper.map(datasetRows);
    return Collections.unmodifiableList(datasets);
  }
}
