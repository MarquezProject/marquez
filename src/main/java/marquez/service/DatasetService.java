package marquez.service;

import static marquez.common.models.Description.NO_DESCRIPTION;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Namespace;
import marquez.core.exceptions.UnexpectedException;
import marquez.db.DatasetDao;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.mappers.DatasetMapper;
import marquez.service.models.Dataset;
import marquez.service.models.DbTableVersion;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private final DatasetMapper datasetMapper = new DatasetMapper();
  private final DatasetDao datasetDao;

  public DatasetService(@NonNull final DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  public Dataset create(@NonNull Namespace namespace, @NonNull DbTableVersion dbTableVersion)
      throws UnexpectedException {
    final DataSourceRow dataSourceRow =
        DataSourceRow.builder()
            .uuid(UUID.randomUUID())
            .connectionUrl(dbTableVersion.getConnectionUrl())
            .build();
    final DatasetRow datasetRow =
        DatasetRow.builder()
            .uuid(UUID.randomUUID())
            .dataSourceUuid(dataSourceRow.getUuid())
            .urn(dbTableVersion.toDatasetUrn(namespace))
            .description(dbTableVersion.getDescription().orElse(NO_DESCRIPTION))
            .build();
    final DbTableInfoRow dbTableInfoRow =
        DbTableInfoRow.builder()
            .uuid(UUID.randomUUID())
            .db(dbTableVersion.getConnectionUrl().getDb())
            .dbSchema(dbTableVersion.getDbSchema())
            .build();
    final DbTableVersionRow dbTableVersionRow =
        DbTableVersionRow.builder()
            .uuid(UUID.randomUUID())
            .datasetUuid(datasetRow.getUuid())
            .dbTableInfoUuid(dbTableInfoRow.getUuid())
            .dbTable(dbTableVersion.getDbTable())
            .build();
    try {
      datasetDao.insertAll(dataSourceRow, datasetRow, dbTableInfoRow, dbTableVersionRow);
      final Optional<DatasetRow> datasetRowIfFound = datasetDao.findBy(datasetRow.getUuid());
      return datasetRowIfFound.map(datasetMapper::map).orElseThrow(UnexpectedException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error("");
      throw new UnexpectedException();
    }
  }

  public Optional<Dataset> get(@NonNull DatasetUrn datasetUrn) throws UnexpectedException {
    try {
      final Optional<DatasetRow> datasetRowIfFound = datasetDao.findBy(datasetUrn);
      return datasetRowIfFound.map(datasetMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error("");
      throw new UnexpectedException();
    }
  }

  public List<Dataset> getAll(
      @NonNull Namespace namespace, @NonNull Integer limit, @NonNull Integer offset)
      throws UnexpectedException {
    try {
      final List<DatasetRow> datasetRowsIfFound = datasetDao.findAll(namespace, limit, offset);
      return Collections.unmodifiableList(datasetMapper.map(datasetRowsIfFound));
    } catch (UnableToExecuteStatementException e) {
      log.error("");
      throw new UnexpectedException();
    }
  }
}
