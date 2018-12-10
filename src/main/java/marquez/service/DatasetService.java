package marquez.service;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import marquez.common.Namespace;
import marquez.common.Urn;
import marquez.db.DatasetDao;
import marquez.db.models.DatasetRow;
import marquez.service.mappers.DatasetMapper;
import marquez.service.models.Dataset;
import marquez.service.models.DbDatasetVersion;

public class DatasetService {
  private final DatasetMapper datasetMapper = new DatasetMapper();
  private final DatasetDao datasetDao;

  public DatasetService(final DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  public Dataset create(Namespace namespace, DbDatasetVersion version) {
    return null;
  }

  public Dataset get(Namespace namespace, Urn urn) {
    return null;
  }

  public List<Dataset> getAll(Namespace namespace, Integer limit, Integer offset) {
    requireNonNull(namespace, "namespace must no the null");

    final List<DatasetRow> datasetRows = datasetDao.findAll(namespace.getValue(), limit, offset);
    final List<Dataset> datasets = datasetMapper.map(datasetRows);
    return unmodifiableList(datasets);
  }
}
