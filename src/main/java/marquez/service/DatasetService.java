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

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.DatasourceDao;
import marquez.db.NamespaceDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.DatasourceRow;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.DatasetMapper;
import marquez.service.mappers.DatasetRowMapper;
import marquez.service.models.Dataset;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private final NamespaceDao namespaceDao;
  private final DatasourceDao datasourceDao;
  private final DatasetDao datasetDao;

  public DatasetService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final DatasourceDao datasourceDao,
      @NonNull final DatasetDao datasetDao) {
    this.namespaceDao = namespaceDao;
    this.datasourceDao = datasourceDao;
    this.datasetDao = datasetDao;
  }

  public Dataset create(@NonNull NamespaceName namespaceName, @NonNull Dataset dataset)
      throws MarquezServiceException {
    try {
      final NamespaceRow namespaceRow =
          namespaceDao
              .findBy(namespaceName)
              .orElseThrow(
                  () ->
                      new MarquezServiceException(
                          "Namespace row not found: " + namespaceName.getValue()));
      final DatasourceRow datasourceRow =
          datasourceDao
              .findBy(dataset.getDatasourceUrn())
              .orElseThrow(
                  () ->
                      new MarquezServiceException(
                          "Datasource row not found: " + dataset.getDatasourceUrn().getValue()));
      final DatasetRow newDatasetRow = DatasetRowMapper.map(namespaceRow, datasourceRow, dataset);
      final DatasetUrn datasetUrn = DatasetUrn.of(newDatasetRow.getUrn());
      final Optional<Dataset> datasetIfFound = get(datasetUrn);
      if (datasetIfFound.isPresent()) {
        return datasetIfFound.get();
      }
      return datasetDao
          .insertAndGet(newDatasetRow)
          .map(
              datasetRow -> {
                final DatasourceUrn datasourceUrn = DatasourceUrn.of(datasourceRow.getUrn());
                return DatasetMapper.map(datasourceUrn, datasetRow);
              })
          .orElseThrow(
              () ->
                  new MarquezServiceException(
                      String.format("Failed to insert dataset row: %s", newDatasetRow)));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create dataset: {}", dataset, e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull DatasetUrn urn) throws MarquezServiceException {
    try {
      return datasetDao.exists(urn);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check dataset: {}", urn.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Dataset> get(@NonNull DatasetUrn urn) throws MarquezServiceException {
    try {
      return datasetDao.findBy(urn).map(DatasetMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get dataset: {}", urn.getValue(), e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public List<Dataset> getAll(
      @NonNull NamespaceName namespaceName, @NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    try {
      final List<DatasetRowExtended> datasetRowsExtended =
          datasetDao.findAll(namespaceName, limit, offset);
      return unmodifiableList(DatasetMapper.map(datasetRowsExtended));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get datasets for namespace: {}", namespaceName.getValue(), e);
      throw new MarquezServiceException();
    }
  }
}
