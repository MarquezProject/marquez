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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.SourceDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private final NamespaceDao namespaceDao;
  private final SourceDao sourceDao;
  private final DatasetDao datasetDao;
  private final DatasetVersionDao versionDao;

  public DatasetService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final SourceDao sourceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final DatasetVersionDao versionDao) {
    this.namespaceDao = namespaceDao;
    this.sourceDao = sourceDao;
    this.datasetDao = datasetDao;
    this.versionDao = versionDao;
  }

  public Dataset createOrUpdate(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull DatasetMeta meta)
      throws MarquezServiceException {
    try {
      if (!exists(datasetName)) {
        final NamespaceRow namespaceRow = namespaceDao.findBy(namespaceName.getValue()).get();
        final SourceRow sourceRow = sourceDao.findBy(meta.getSourceName().getValue()).get();
        final DatasetRow newDatasetRow =
            Mapper.toDatasetRow(namespaceRow.getUuid(), sourceRow.getUuid(), datasetName, meta);

        datasetDao.insert(newDatasetRow);
      }

      final Optional<UUID> version = meta.version(namespaceName, datasetName);
      if (version.isPresent()) {
        if (!versionDao.exists(version.get())) {
          final ExtendedDatasetRow row = datasetDao.findBy(datasetName.getValue()).get();
          final DatasetVersionRow newVersionRow =
              Mapper.toDatasetVersionRow(row.getUuid(), version.get(), meta);

          versionDao.insertAndUpdate(newVersionRow);
        }
      }

      return get(datasetName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create or update dataset for namespace {} with meta: {}",
          namespaceName.getValue(),
          meta,
          e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull DatasetName name) throws MarquezServiceException {
    try {
      return datasetDao.exists(name.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check dataset {}.", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Dataset> get(@NonNull DatasetName name) throws MarquezServiceException {
    try {
      return datasetDao
          .findBy(name.getValue())
          .map(row -> Mapper.toDataset(row, getVersionRowOrNull(row)));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get dataset {}.", name.getValue(), e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public List<Dataset> getAll(@NonNull NamespaceName namespaceName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<ExtendedDatasetRow> rows =
          datasetDao.findAll(namespaceName.getValue(), limit, offset);

      final ImmutableList.Builder<Dataset> builder = ImmutableList.builder();
      rows.forEach(
          row -> {
            final Dataset dataset = Mapper.toDataset(row, getVersionRowOrNull(row));
            builder.add(dataset);
          });

      return builder.build();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to get datasets for namespace {}: limit={}, offset={}",
          namespaceName.getValue(),
          limit,
          offset,
          e);
      throw new MarquezServiceException();
    }
  }

  private DatasetVersionRow getVersionRowOrNull(@NonNull ExtendedDatasetRow row) {
    return (row.getCurrentVersionUuid().isPresent())
        ? versionDao.findBy(row.getType(), row.getCurrentVersionUuid().get()).get()
        : null;
  }
}
