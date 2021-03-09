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
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.toArray;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.prometheus.client.Counter;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.TagName;
import marquez.common.models.Version;
import marquez.db.DatasetDao;
import marquez.db.DatasetFieldDao;
import marquez.db.DatasetVersionDao;
import marquez.db.MarquezDao;
import marquez.db.NamespaceDao;
import marquez.db.RunDao;
import marquez.db.SourceDao;
import marquez.db.TagDao;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.StreamVersionRow;
import marquez.db.models.TagRow;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DatasetVersion;
import marquez.service.models.Run;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasetService {
  private static final Counter datasetCounterMetric =
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
  private final DatasetFieldDao datasetFieldDao;
  private final DatasetVersionDao datasetVersionDao;
  private final RunDao runDao;
  private final TagDao tagDao;

  private final RunService runService;

  public DatasetService(
      @NonNull final MarquezDao marquezDao, @NonNull final RunService runService) {
    this.namespaceDao = marquezDao.createNamespaceDao();
    this.sourceDao = marquezDao.createSourceDao();
    this.datasetDao = marquezDao.createDatasetDao();
    this.datasetFieldDao = marquezDao.createDatasetFieldDao();
    this.datasetVersionDao = marquezDao.createDatasetVersionDao();
    this.tagDao = marquezDao.createTagDao();
    this.runDao = marquezDao.createRunDao();
    this.runService = runService;
  }

  public DatasetService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final SourceDao sourceDao,
      @NonNull final DatasetDao datasetDao,
      @NonNull final DatasetFieldDao datasetFieldDao,
      @NonNull final DatasetVersionDao datasetVersionDao,
      @NonNull final TagDao tagDao,
      @NonNull RunDao runDao,
      @NonNull final RunService runService) {
    this.namespaceDao = namespaceDao;
    this.sourceDao = sourceDao;
    this.datasetDao = datasetDao;
    this.datasetFieldDao = datasetFieldDao;
    this.datasetVersionDao = datasetVersionDao;
    this.tagDao = tagDao;
    this.runDao = runDao;
    this.runService = runService;
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
            tagDao
                .findAllIn(
                    toArray(
                        datasetMeta.getTags().stream()
                            .map(TagName::getValue)
                            .collect(toImmutableList()),
                        String.class))
                .stream()
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
        datasetCounterMetric
            .labels(namespaceName.getValue(), datasetMeta.getType().toString())
            .inc();
      }
      final Version version = datasetMeta.version(namespaceName, datasetName);
      if (!datasetVersionDao.exists(version.getValue())) {
        log.info(
            "Creating version '{}' for dataset '{}'...",
            version.getValue(),
            datasetName.getValue());
        final ExtendedDatasetRow datasetRow =
            datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
        final List<DatasetFieldRow> fieldRows = datasetFieldDao.findAll(datasetRow.getUuid());
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
                                fieldRow ->
                                    newFieldRow.getName().equals(fieldRow.getName())
                                        && newFieldRow.getType().equals(fieldRow.getType())))
                .collect(toImmutableList());
        final List<DatasetFieldRow> fieldRowsForVersion =
            Stream.concat(
                    fieldRows.stream()
                        .filter(
                            fieldRow ->
                                newFieldRows.stream()
                                    .noneMatch(
                                        newFieldRow ->
                                            newFieldRow.getName().equals(fieldRow.getName())
                                                && !newFieldRow
                                                    .getType()
                                                    .equals(fieldRow.getType()))),
                    newFieldRowsForVersion.stream())
                .collect(toImmutableList());
        final List<UUID> fieldUuids =
            fieldRowsForVersion.stream().map(DatasetFieldRow::getUuid).collect(toImmutableList());
        final DatasetVersionRow newVersionRow =
            Mapper.toDatasetVersionRow(datasetRow.getUuid(), version, fieldUuids, datasetMeta);
        datasetVersionDao.insertWith(newVersionRow, newFieldRowsForVersion);
        log.info(
            "Successfully created version '{}' for dataset '{}'.",
            version.getValue(),
            datasetName.getValue());
        versions
            .labels(
                namespaceName.getValue(), datasetMeta.getType().toString(), datasetName.getValue())
            .inc();
      }

      if (datasetMeta.getRunId().isPresent()) {
        UUID runUuid = datasetMeta.getRunId().get().getValue();
        ExtendedRunRow runRow = runDao.findBy(runUuid).get();

        List<ExtendedDatasetVersionRow> outputs = datasetVersionDao.findByRunId(runUuid);
        runService.notify(
            new JobOutputUpdate(
                RunId.of(runRow.getUuid()),
                null,
                JobName.of(runRow.getJobName()),
                NamespaceName.of(runRow.getNamespaceName()),
                RunService.buildRunOutputs(outputs)));
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
        tagDao
            .findAllIn(
                toArray(
                    field.getTags().stream().map(TagName::getValue).collect(toImmutableList()),
                    String.class))
            .stream()
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

  public boolean fieldExists(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull FieldName fieldName)
      throws MarquezServiceException {
    try {
      return datasetFieldDao.exists(
          namespaceName.getValue(), datasetName.getValue(), fieldName.getValue());
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

  public Optional<Dataset> getBy(@NonNull DatasetVersionId datasetVersionId)
      throws MarquezServiceException {
    try {
      return datasetDao
          .find(datasetVersionId.getNamespace().getValue(), datasetVersionId.getName().getValue())
          .map(datasetRow -> toDataset(datasetRow, datasetVersionId.getVersionUuid()));
    } catch (UnableToExecuteStatementException e) {
      throw new MarquezServiceException(
          String.format("Failed to get dataset version with ID '%s'.", datasetVersionId), e);
    }
  }

  public Optional<DatasetVersion> getVersion(@NonNull Version version) {
    return datasetVersionDao.findBy(version.getValue()).map(this::toDatasetVersion);
  }

  public List<DatasetVersion> getVersionsFor(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      int limit,
      int offset) {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    final ImmutableList.Builder<DatasetVersion> datasetVersions = ImmutableList.builder();
    final List<ExtendedDatasetVersionRow> datasetVersionRows =
        datasetVersionDao.findAll(namespaceName.getValue(), datasetName.getValue(), limit, offset);
    for (final ExtendedDatasetVersionRow datasetVersionRow : datasetVersionRows) {
      datasetVersions.add(toDatasetVersion(datasetVersionRow));
    }
    return datasetVersions.build();
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
    return toDataset(datasetRow, null);
  }

  private Dataset toDataset(
      @NonNull ExtendedDatasetRow datasetRow, @Nullable UUID datasetVersionUuid) {
    final ImmutableSet<TagName> tags =
        tagDao.findAllIn(toArray(datasetRow.getTagUuids(), UUID.class)).stream()
            .map(TagRow::getName)
            .map(TagName::of)
            .collect(toImmutableSet());
    final DatasetVersionRow versionRow =
        datasetVersionDao
            .find(
                datasetRow.getType(),
                (datasetVersionUuid == null)
                    ? datasetRow.getCurrentVersionUuid().orElse(null)
                    : datasetVersionUuid)
            .get();
    final ImmutableList<Field> fields =
        datasetFieldDao.findAllIn(toArray(versionRow.getFieldUuids(), UUID.class)).stream()
            .map(this::toField)
            .collect(toImmutableList());
    return Mapper.toDataset(datasetRow, tags, versionRow, fields);
  }

  private DatasetVersion toDatasetVersion(@NonNull DatasetVersionRow datasetVersionRow) {
    final ExtendedDatasetRow datasetRow =
        datasetDao.findBy(datasetVersionRow.getDatasetUuid()).get();
    final ImmutableSet<TagName> tags =
        tagDao.findAllIn(toArray(datasetRow.getTagUuids(), UUID.class)).stream()
            .map(TagRow::getName)
            .map(TagName::of)
            .collect(toImmutableSet());
    final ImmutableList<Field> fields =
        datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)).stream()
            .map(this::toField)
            .collect(toImmutableList());
    final Run createdByRun =
        datasetVersionRow
            .getRunUuid()
            .map(runUuid -> runService.getRun(RunId.of(runUuid)).orElse(null))
            .orElse(null);
    return Mapper.toDatasetVersion(datasetRow, tags, datasetVersionRow, fields, null, createdByRun);
  }

  private DatasetVersion toDatasetVersion(@NonNull ExtendedDatasetVersionRow datasetVersionRow) {
    final ExtendedDatasetRow datasetRow =
        datasetDao.findBy(datasetVersionRow.getDatasetUuid()).get();
    final ImmutableSet<TagName> tags =
        tagDao.findAllIn(toArray(datasetRow.getTagUuids(), UUID.class)).stream()
            .map(TagRow::getName)
            .map(TagName::of)
            .collect(toImmutableSet());
    final ImmutableList<Field> fields =
        datasetFieldDao.findAllIn(toArray(datasetVersionRow.getFieldUuids(), UUID.class)).stream()
            .map(this::toField)
            .collect(toImmutableList());
    final Run createdByRun =
        datasetVersionRow
            .getRunUuid()
            .map(runUuid -> runService.getRun(RunId.of(runUuid)).orElse(null))
            .orElse(null);
    final String schemaLocation =
        (DatasetType.valueOf(datasetRow.getType()) == DatasetType.STREAM)
            ? ((StreamVersionRow)
                    datasetVersionDao.find(datasetRow.getType(), datasetVersionRow.getUuid()).get())
                .getSchemaLocation()
            : null;
    return Mapper.toDatasetVersion(
        datasetRow, tags, datasetVersionRow, fields, schemaLocation, createdByRun);
  }

  /** Creates a {@link Field} instance from the given {@link DatasetFieldRow}. */
  private Field toField(@NonNull DatasetFieldRow fieldRow) {
    final ImmutableSet<TagName> tags =
        tagDao.findAllIn(toArray(fieldRow.getTagUuids(), UUID.class)).stream()
            .map(row -> TagName.of(row.getName()))
            .collect(toImmutableSet());
    return Mapper.toField(fieldRow, tags);
  }

  public Dataset tagWith(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull TagName tagName)
      throws MarquezServiceException {
    try {
      final ExtendedDatasetRow datasetRow =
          datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
      final TagRow tagRow =
          tagDao.findBy(tagName.getValue().toUpperCase(Locale.getDefault())).get();
      final Instant taggedAt = Instant.now();
      datasetDao.updateTags(datasetRow.getUuid(), tagRow.getUuid(), taggedAt);
      log.info("Successfully tagged dataset '{}' with '{}'.", datasetName.getValue(), tagName);
      return get(namespaceName, datasetName).get();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to tag dataset '{}' with '{}'.", datasetName.getValue(), tagName, e);
      throw new MarquezServiceException();
    }
  }

  public Dataset tagFieldWith(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull FieldName fieldName,
      @NonNull TagName tagName)
      throws MarquezServiceException {
    try {
      final ExtendedDatasetRow datasetRow =
          datasetDao.find(namespaceName.getValue(), datasetName.getValue()).get();
      final DatasetFieldRow fieldRow =
          datasetFieldDao.find(datasetRow.getUuid(), fieldName.getValue()).get();
      final TagRow tagRow =
          tagDao.findBy(tagName.getValue().toUpperCase(Locale.getDefault())).get();
      final Instant taggedAt = Instant.now();
      datasetFieldDao.updateTags(fieldRow.getUuid(), tagRow.getUuid(), taggedAt);
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
