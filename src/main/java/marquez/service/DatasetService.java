/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetUrn;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.mappers.DataSourceRowMapper;
import marquez.service.mappers.DatasetMapper;
import marquez.service.mappers.DatasetRowMapper;
import marquez.service.mappers.DbTableInfoRowMapper;
import marquez.service.mappers.DbTableVersionRowMapper;
import marquez.service.models.Dataset;
import marquez.service.models.DbTableVersion;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private final DatasetDao datasetDao;

  public DatasetService(@NonNull final DatasetDao datasetDao) {
    this.datasetDao = datasetDao;
  }

  public Dataset create(
      @NonNull NamespaceName namespaceName, @NonNull DbTableVersion dbTableVersion)
      throws UnexpectedException {
    final DataSourceRow dataSourceRow = DataSourceRowMapper.map(dbTableVersion);
    final DatasetRow datasetRow =
        DatasetRowMapper.map(namespaceName, dataSourceRow, dbTableVersion);
    final DbTableInfoRow dbTableInfoRow = DbTableInfoRowMapper.map(dbTableVersion);
    final DbTableVersionRow dbTableVersionRow =
        DbTableVersionRowMapper.map(datasetRow, dbTableInfoRow, dbTableVersion);
    try {
      datasetDao.insertAll(dataSourceRow, datasetRow, dbTableInfoRow, dbTableVersionRow);
      final Optional<DatasetRow> datasetRowIfFound = datasetDao.findBy(datasetRow.getUuid());
      return datasetRowIfFound.map(DatasetMapper::map).orElseThrow(UnexpectedException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new UnexpectedException();
    }
  }

  public Optional<Dataset> get(@NonNull DatasetUrn urn) throws UnexpectedException {
    try {
      final Optional<DatasetRow> datasetRowIfFound = datasetDao.findBy(urn);
      return datasetRowIfFound.map(DatasetMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new UnexpectedException();
    }
  }

  public List<Dataset> getAll(
      @NonNull NamespaceName namespaceName, @NonNull Integer limit, @NonNull Integer offset)
      throws UnexpectedException {
    try {
      final List<DatasetRow> datasetRows = datasetDao.findAll(namespaceName, limit, offset);
      return Collections.unmodifiableList(DatasetMapper.map(datasetRows));
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new UnexpectedException();
    }
  }
}
