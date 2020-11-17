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

package marquez.service.mappers;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.temporal.ChronoUnit.MILLIS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.FieldType;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.common.models.TagName;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.ExtendedDatasetRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceOwnershipRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.OwnerRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.RunStateRow;
import marquez.db.models.SourceRow;
import marquez.db.models.StreamVersionRow;
import marquez.db.models.TagRow;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DbTable;
import marquez.service.models.DbTableMeta;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;
import marquez.service.models.Stream;
import marquez.service.models.StreamMeta;
import marquez.service.models.Tag;
import marquez.service.models.Version;

public final class Mapper {
  private Mapper() {}

  public static Namespace toNamespace(@NonNull final NamespaceRow row) {
    return new Namespace(
        NamespaceName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        OwnerName.of(row.getCurrentOwnerName()),
        row.getDescription().orElse(null));
  }

  public static List<Namespace> toNamespaces(@NonNull final List<NamespaceRow> rows) {
    return rows.stream().map(Mapper::toNamespace).collect(toImmutableList());
  }

  public static NamespaceRow toNamespaceRow(
      @NonNull final NamespaceName name, @NonNull final NamespaceMeta meta) {
    final Instant now = newTimestamp();
    return new NamespaceRow(
        newRowUuid(),
        now,
        now,
        name.getValue(),
        meta.getDescription().orElse(null),
        meta.getOwnerName().getValue());
  }

  public static OwnerRow toOwnerRow(@NonNull final OwnerName name) {
    return new OwnerRow(newRowUuid(), newTimestamp(), name.getValue());
  }

  public static NamespaceOwnershipRow toNamespaceOwnershipRow(
      @NonNull final UUID namespaceRowUuid, @NonNull final UUID ownerRowUuid) {
    return new NamespaceOwnershipRow(
        newRowUuid(), newTimestamp(), null, namespaceRowUuid, ownerRowUuid);
  }

  public static Source toSource(@NonNull final SourceRow row) {
    return new Source(
        SourceType.valueOf(row.getType()),
        SourceName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        URI.create(row.getConnectionUrl()),
        row.getDescription().orElse(null));
  }

  public static List<Source> toSources(@NonNull final List<SourceRow> rows) {
    return rows.stream().map(Mapper::toSource).collect(toImmutableList());
  }

  public static SourceRow toSourceRow(
      @NonNull final SourceName name, @NonNull final SourceMeta meta) {
    final Instant now = newTimestamp();
    return new SourceRow(
        newRowUuid(),
        meta.getType().toString(),
        now,
        now,
        name.getValue(),
        meta.getConnectionUrl().toASCIIString(),
        meta.getDescription().orElse(null));
  }

  public static DatasetId toDatasetId(@NonNull final ExtendedDatasetRow row) {
    return new DatasetId(NamespaceName.of(row.getNamespaceName()), DatasetName.of(row.getName()));
  }

  public static JobId toJobId(@NonNull final JobRow row) {
    return new JobId(NamespaceName.of(row.getNamespaceName()), JobName.of(row.getName()));
  }

  public static Dataset toDataset(
      @NonNull final ExtendedDatasetRow row,
      @NonNull final ImmutableSet<TagName> tags,
      @NonNull final DatasetVersionRow versionRow,
      @NonNull final ImmutableList<Field> fields) {
    final DatasetType type = DatasetType.valueOf(row.getType());
    switch (type) {
      case DB_TABLE:
        return toDbTable(row, tags, fields);
      case STREAM:
        return toStream(row, tags, versionRow, fields);
      default:
        throw new IllegalArgumentException();
    }
  }

  private static Dataset toDbTable(
      @NonNull final ExtendedDatasetRow row,
      @NonNull final ImmutableSet<TagName> tags,
      @NonNull final ImmutableList<Field> fields) {
    return new DbTable(
        new DatasetId(NamespaceName.of(row.getNamespaceName()), DatasetName.of(row.getName())),
        DatasetName.of(row.getName()),
        DatasetName.of(row.getPhysicalName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        SourceName.of(row.getSourceName()),
        fields,
        tags,
        row.getLastModifiedAt().orElse(null),
        row.getDescription().orElse(null));
  }

  private static Dataset toStream(
      @NonNull final ExtendedDatasetRow row,
      @NonNull final ImmutableSet<TagName> tags,
      @NonNull final DatasetVersionRow versionRow,
      @NonNull final ImmutableList<Field> fields) {
    return new Stream(
        new DatasetId(NamespaceName.of(row.getNamespaceName()), DatasetName.of(row.getName())),
        DatasetName.of(row.getName()),
        DatasetName.of(row.getPhysicalName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        SourceName.of(row.getSourceName()),
        Utils.toUrl(((StreamVersionRow) versionRow).getSchemaLocation()),
        fields,
        tags,
        row.getLastModifiedAt().orElse(null),
        row.getDescription().orElse(null));
  }

  public static DatasetRow toDatasetRow(
      @NonNull final UUID namespaceRowUuid,
      @NonNull final UUID sourceRowUuid,
      @NonNull final DatasetName name,
      @NonNull final DatasetMeta meta,
      @NonNull final List<UUID> tagUuids) {
    final Instant now = newTimestamp();
    return new DatasetRow(
        newRowUuid(),
        toDatasetType(meta).toString(),
        now,
        now,
        namespaceRowUuid,
        sourceRowUuid,
        name.getValue(),
        meta.getPhysicalName().getValue(),
        tagUuids,
        null,
        meta.getDescription().orElse(null),
        null);
  }

  private static DatasetType toDatasetType(@NonNull final DatasetMeta meta) {
    if (meta instanceof DbTableMeta) {
      return DatasetType.DB_TABLE;
    } else if (meta instanceof StreamMeta) {
      return DatasetType.STREAM;
    }
    throw new IllegalArgumentException();
  }

  public static Field toField(
      @NonNull final DatasetFieldRow row, @NonNull final ImmutableSet<TagName> tags) {
    return new Field(
        FieldName.of(row.getName()),
        FieldType.valueOf(row.getType()),
        tags,
        row.getDescription().orElse(null));
  }

  public static DatasetFieldRow toDatasetFieldRow(
      @NonNull final UUID datasetUuid, @NonNull final Field field, @NonNull List<UUID> tagUuids) {
    final Instant now = Instant.now();
    return new DatasetFieldRow(
        newRowUuid(),
        field.getType().toString(),
        now,
        now,
        datasetUuid,
        field.getName().getValue(),
        tagUuids,
        field.getDescription().orElse(null));
  }

  public static DatasetVersionRow toDatasetVersionRow(
      @NonNull final UUID datasetUuid,
      @NonNull final Version version,
      @NonNull final List<UUID> fieldUuids,
      @NonNull final DatasetMeta meta) {
    if (meta instanceof StreamMeta) {
      return toStreamVersionRow(datasetUuid, version, fieldUuids, meta);
    }
    return new DatasetVersionRow(
        newRowUuid(),
        newTimestamp(),
        datasetUuid,
        version.getValue(),
        fieldUuids,
        meta.getRunId().map(RunId::getValue).orElse(null));
  }

  private static DatasetVersionRow toStreamVersionRow(
      @NonNull final UUID datasetUuid,
      @NonNull final Version version,
      @NonNull final List<UUID> fieldUuids,
      @NonNull final DatasetMeta meta) {
    return new StreamVersionRow(
        newRowUuid(),
        newTimestamp(),
        datasetUuid,
        version.getValue(),
        fieldUuids,
        meta.getRunId().map(RunId::getValue).orElse(null),
        ((StreamMeta) meta).getSchemaLocation().toString());
  }

  public static Tag toTag(@NonNull final TagRow row) {
    return new Tag(TagName.of(row.getName()), row.getDescription().orElse(null));
  }

  public static List<Tag> toTags(@NonNull final List<TagRow> rows) {
    return rows.stream().map(Mapper::toTag).collect(toImmutableList());
  }

  public static Job toJob(
      @NonNull final JobRow row,
      @NonNull final ImmutableSet<DatasetId> inputs,
      @NonNull final ImmutableSet<DatasetId> outputs,
      @Nullable final String locationString,
      @NonNull final String contextString,
      @Nullable final ExtendedRunRow runRow) {
    return new Job(
        toJobId(row),
        JobType.valueOf(row.getType()),
        JobName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        inputs,
        outputs,
        (locationString == null) ? null : Utils.toUrl(locationString),
        Utils.fromJson(contextString, new TypeReference<ImmutableMap<String, String>>() {}),
        row.getDescription().orElse(null),
        (runRow == null) ? null : toRun(runRow));
  }

  public static JobRow toJobRow(
      @NonNull final NamespaceRow namespace,
      @NonNull final JobName name,
      @NonNull final JobMeta meta) {
    final Instant now = Instant.now();
    return new JobRow(
        newRowUuid(),
        meta.getType().toString(),
        now,
        now,
        namespace.getUuid(),
        namespace.getName(),
        name.getValue(),
        meta.getDescription().orElse(null),
        null);
  }

  public static JobContextRow toJobContextRow(
      @NonNull final Map<String, String> context, @NonNull final String checksum) {
    return new JobContextRow(newRowUuid(), newTimestamp(), Utils.toJson(context), checksum);
  }

  public static JobVersionRow toJobVersionRow(
      @NonNull final UUID jobRowUuid,
      @NonNull final UUID jobContextRowUuid,
      @NonNull final List<UUID> inputs,
      @NonNull final List<UUID> outputs,
      @Nullable final URL location,
      @NonNull final Version version) {
    final Instant now = newTimestamp();
    return new JobVersionRow(
        newRowUuid(),
        now,
        now,
        jobRowUuid,
        jobContextRowUuid,
        inputs,
        outputs,
        (location == null) ? null : location.toString(),
        version.getValue(),
        null);
  }

  public static Run toRun(@NonNull final ExtendedRunRow row) {
    Optional<Long> durationMs =
        row.getEndedAt()
            .flatMap(
                endedAt -> row.getStartedAt().map(startedAt -> startedAt.until(endedAt, MILLIS)));
    return new Run(
        RunId.of(row.getUuid()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        row.getNominalStartTime().orElse(null),
        row.getNominalEndTime().orElse(null),
        RunState.valueOf(row.getCurrentRunState().orElse(RunState.NEW.name())),
        row.getStartedAt().orElse(null),
        row.getEndedAt().orElse(null),
        durationMs.orElse(null),
        Utils.fromJson(row.getArgs(), new TypeReference<ImmutableMap<String, String>>() {}));
  }

  public static List<Run> toRuns(@NonNull final List<ExtendedRunRow> rows) {
    return rows.stream().map(Mapper::toRun).collect(toImmutableList());
  }

  public static RunRow toRunRow(
      @NonNull final UUID jobVersionUuid,
      @NonNull final UUID runArgsUuid,
      @NonNull final List<UUID> inputVersionUuids,
      @NonNull final RunMeta runMeta) {
    final Instant now = newTimestamp();
    return new RunRow(
        runMeta.getId().map(runId -> runId.getValue()).orElseGet(Mapper::newRowUuid),
        now,
        now,
        jobVersionUuid,
        runArgsUuid,
        inputVersionUuids,
        runMeta.getNominalStartTime().orElse(null),
        runMeta.getNominalEndTime().orElse(null),
        null,
        null,
        null,
        null,
        null);
  }

  public static RunArgsRow toRunArgsRow(
      @NonNull final Map<String, String> args, @NonNull final String checksum) {
    return new RunArgsRow(newRowUuid(), newTimestamp(), Utils.toJson(args), checksum);
  }

  public static RunStateRow toRunStateRow(
      @NonNull final UUID runId,
      @NonNull final RunState runState,
      @Nullable Instant transitionedAt) {
    return new RunStateRow(
        newRowUuid(),
        transitionedAt == null ? newTimestamp() : transitionedAt,
        runId,
        runState.name());
  }

  private static UUID newRowUuid() {
    return UUID.randomUUID();
  }

  private static Instant newTimestamp() {
    return Instant.now();
  }

  public static ImmutableMap<String, String> toRunArgs(String args) {
    if (args == null) {
      return null;
    }
    return Utils.fromJson(
        args,
        Utils.getMapper()
            .getTypeFactory()
            .constructMapType(ImmutableMap.class, String.class, String.class));
  }
}
