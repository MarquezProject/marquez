package marquez.service;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import marquez.db.DatasetDao;
import marquez.service.mappers.DatasetMapper;
import marquez.service.models.Dataset;

public final class DatasetService {
  private static final int DEFAULT_LIMIT = 200;
  private static final int DEFAULT_OFFSET = 200;

  private final DatasetMapper datasetMapper = new DatasetMapper();
  private final DatasetDao datasetDao;

  public DatasetService(final DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  public List<Dataset> getAll(String namespace, Integer limit, Integer offset) {
    requireNonNull(namespace, "namespace must no the null");

    final List<DatasetRow> datasetRows =
        datasetDao.findAll(namespace, limit.orElse(DEFAULT_LIMIT), offset.orElse(DEFAULT_OFFSET));
    final List<Dataset> datasets = datasetMapper.map(datasetRows);
    return unmodifiableList(datasets);
  }
}
