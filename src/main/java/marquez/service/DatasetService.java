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
import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import io.prometheus.client.Counter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private static final Counter datasets =
      Counter.build()
          .namespace("marquez")
          .name("dataset_total")
          .labelNames("namespace_name", "dataset_type")
          .help("Total number of datasets.")
          .register();
  private static final Counter versions =
      Counter.build()
          .namespace("marquez")
          .name("dataset_versions_total")
          .labelNames("namespace_name", "dataset_type", "dataset_name")
          .help("Total number of dataset versions.")
          .register();

  private final NamespaceDao namespaceDao;
  private final SourceDao sourceDao;
  private final DatasetDao datasetDao;
  private final DatasetFieldDao fieldDao;
  private final DatasetVersionDao versionDao;
  private final TagDao tagDao;

  public DatasetService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final SourceDao sourceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final DatasetFieldDao fieldDao,
      @NonNull final DatasetVersionDao versionDao,
      @NonNull final TagDao tagDao) {
    this.namespaceDao = namespaceDao;
    this.sourceDao = sourceDao;
    this.datasetDao = datasetDao;
    this.fieldDao = fieldDao;
    this.versionDao = versionDao;
    this.tagDao = tagDao;
  }

  public Dataset createOrUpdate(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull DatasetMeta datasetMeta)
      throws MarquezServiceException {
    try {
      if (!exists(namespaceName, datasetName)) {
        log.info(
            "No dataset with name '{}' for namespace '{}' found, creating...",
            datasetName.getValue(),
            namespaceName.getValue());
        final NamespaceRow namespaceRow = namespaceDao.findBy(namespaceName.getValue()).get();
        final SourceRow sourceRow = sourceDao.findBy(datasetMeta.getSourceName().getValue()).get();
        final List<UUID> tagUuids =
            tagDao.findAllInStringList(datasetMeta.getTags()).stream()
                .map(TagRow::getUuid)
                .collect(toImmutableList());
        final DatasetRow newDatasetRow =
            Mapper.toDatasetRow(
                namespaceRow.getUuid(), sourceRow.getUuid(), datasetName, datasetMeta, tagUuids);
        datasetDao.insert(newDatasetRow);
        log.info(
            "Successfully created dataset '{}' for namespace '{}' with meta: {}",
            datasetName.getValue(),
            namespaceName.getValue(),
            datasetMeta);
        datasets.labels(namespaceName.getValue(), datasetMeta.getType().toString()).inc();
      }
      final UUID version = datasetMeta.version(namespaceName, datasetName);
      if (!versionDao.exists(version)) {
        log.info("Creating version '{}' for dataset '{}'...", version, datasetName.getValue());
        final ExtendedDatasetRow datasetRow =
            datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
        final List<DatasetFieldRow> fieldRows = fieldDao.findAll(datasetRow.getUuid());
        final List<DatasetFieldRow> newFieldRows =
            datasetMeta.getFields().stream()
                .map(field -> toDatasetFieldRow(datasetRow.getUuid(), field))
                .collect(toImmutableList());
        final List<DatasetFieldRow> newFieldRowsForVersion =
            newFieldRows.stream()
                .filter(
                    newFieldRow ->
                        fieldRows.stream()
                            .noneMatch(
                                fieldRow -> newFieldRow.getName().equals(fieldRow.getName())))
                .collect(toImmutableList());
        final List<DatasetFieldRow> fieldRowsForVersion =
            Stream.concat(fieldRows.stream(), newFieldRowsForVersion.stream())
                .collect(toImmutableList());
        final List<UUID> fieldUuids =
            fieldRowsForVersion.stream().map(DatasetFieldRow::getUuid).collect(toImmutableList());
        final DatasetVersionRow newVersionRow =
            Mapper.toDatasetVersionRow(datasetRow.getUuid(), version, fieldUuids, datasetMeta);
        versionDao.insertWith(newVersionRow, newFieldRowsForVersion);
        log.info(
            "Successfully created version '{}' for dataset '{}'.", version, datasetName.getValue());
        versions
            .labels(
                namespaceName.getValue(), datasetMeta.getType().toString(), datasetName.getValue())
            .inc();
      }
      return get(namespaceName, datasetName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create or update dataset '{}' for namespace '{}' with meta: {}",
          datasetName.getValue(),
          namespaceName.getValue(),
          datasetMeta,
          e);
      throw new MarquezServiceException();
    }
  }

  /** Creates a {@link DatasetFieldRow} instance from the given {@link Field}. */
  private DatasetFieldRow toDatasetFieldRow(@NonNull UUID datasetUuid, @NonNull Field field) {
    final List<UUID> tagUuids =
        tagDao.findAllInStringList(field.getTags()).stream()
            .map(TagRow::getUuid)
            .collect(toImmutableList());
    return Mapper.toDatasetFieldRow(datasetUuid, field, tagUuids);
  }

  public boolean exists(@NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName)
      throws MarquezServiceException {
    try {
      return datasetDao.exists(namespaceName.getValue(), datasetName.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check dataset '{}'.", datasetName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull String fieldName)
      throws MarquezServiceException {
    checkNotBlank(fieldName, "fieldName must not be blank");
    try {
      return fieldDao.exists(namespaceName.getValue(), datasetName.getValue(), fieldName);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check dataset '{}'.", datasetName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Dataset> get(
      @NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName)
      throws MarquezServiceException {
    try {
      return datasetDao.find(namespaceName.getValue(), datasetName.getValue()).map(this::toDataset);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get dataset '{}'.", datasetName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public List<Dataset> getAll(@NonNull NamespaceName namespaceName, int limit, int offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<ExtendedDatasetRow> datasetRows =
          datasetDao.findAll(namespaceName.getValue(), limit, offset);
      return datasetRows.stream().map(this::toDataset).collect(toImmutableList());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get datasets for namespace '{}'.", namespaceName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  /** Creates a {@link Dataset} instance from the given {@link ExtendedDatasetRow}. */
  private Dataset toDataset(@NonNull ExtendedDatasetRow datasetRow) {
    final List<String> tags =
        tagDao.findAllInUuidList(datasetRow.getTagUuids()).stream()
            .map(TagRow::getName)
            .collect(toImmutableList());
    final DatasetVersionRow versionRow =
        versionDao
            .find(datasetRow.getType(), datasetRow.getCurrentVersionUuid().orElse(null))
            .get();
    final List<Field> fields =
        fieldDao.findAllInUuidList(versionRow.getFieldUuids()).stream()
            .map(this::toField)
            .collect(toImmutableList());
    return Mapper.toDataset(datasetRow, tags, versionRow, fields);
  }

  /** Creates a {@link Field} instance from the given {@link DatasetFieldRow}. */
  private Field toField(@NonNull DatasetFieldRow fieldRow) {
    final List<String> tags =
        tagDao.findAllInUuidList(fieldRow.getTagUuids()).stream()
            .map(TagRow::getName)
            .collect(toImmutableList());
    return Mapper.toField(fieldRow, tags);
  }

  public Dataset tagWith(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull String tagName)
      throws MarquezServiceException {
    checkNotBlank(tagName, "tagName must not be blank");
    try {
      final ExtendedDatasetRow datasetRow =
          datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
      final TagRow tagRow = tagDao.findBy(tagName).get();
      final Instant taggedAt = Instant.now();
      datasetDao.updateTags(datasetRow.getUuid(), tagRow.getUuid(), taggedAt);
      log.info("Successfully tagged dataset '{}' with '{}'.", datasetName.getValue(), tagName);
      return get(namespaceName, datasetName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to tag dataset '{}' with '{}'.", datasetName.getValue(), tagName, e);
      throw new MarquezServiceException();
    }
  }

  public Dataset tagWith(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull String fieldName,
      @NonNull String tagName)
      throws MarquezServiceException {
    checkNotBlank(fieldName, "fieldName must not be blank");
    checkNotBlank(tagName, "tagName must not be blank");
    try {
      final ExtendedDatasetRow datasetRow =
          datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
      final DatasetFieldRow fieldRow = fieldDao.find(datasetRow.getUuid(), fieldName).get();
      final TagRow tagRow = tagDao.findBy(tagName).get();
      final Instant taggedAt = Instant.now();
      fieldDao.updateTags(fieldRow.getUuid(), tagRow.getUuid(), taggedAt);
      log.info(
          "Successfully tagged field '{}' for dataset '{}' with '{}'.",
          fieldName,
          datasetName.getValue(),
          tagName);
      return get(namespaceName, datasetName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to tag field '{}' for dataset '{}' with '{}'.",
          fieldName,
          datasetName.getValue(),
          tagName,
          e);
      throw new MarquezServiceException();
    }
  }
}
