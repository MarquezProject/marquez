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

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.FieldType;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
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

  public static List<Namespace> toNamespace(@NonNull final List<NamespaceRow> rows) {
    return rows.stream().map(Mapper::toNamespace).collect(toImmutableList());
  }

  public static NamespaceRow toNamespaceRow(
      @NonNull final NamespaceName name, @NonNull final NamespaceMeta meta) {
    final Instant now = Instant.now();
    return new NamespaceRow(
        UUID.randomUUID(),
        now,
        now,
        name.getValue(),
        meta.getDescription().orElse(null),
        meta.getOwnerName().getValue());
  }

  public static OwnerRow toOwnerRow(@NonNull final OwnerName name) {
    return new OwnerRow(UUID.randomUUID(), Instant.now(), name.getValue());
  }

  public static NamespaceOwnershipRow toNamespaceOwnershipRow(
      @NonNull final UUID namespaceRowUuid, @NonNull final UUID ownerRowUuid) {
    return new NamespaceOwnershipRow(
        UUID.randomUUID(), Instant.now(), null, namespaceRowUuid, ownerRowUuid);
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

  public static List<Source> toSource(@NonNull final List<SourceRow> rows) {
    return rows.stream().map(Mapper::toSource).collect(toImmutableList());
  }

  public static SourceRow toSourceRow(
      @NonNull final SourceName name, @NonNull final SourceMeta meta) {
    final Instant now = Instant.now();
    return new SourceRow(
        UUID.randomUUID(),
        meta.getType().toString(),
        now,
        now,
        name.getValue(),
        meta.getConnectionUrl().toASCIIString(),
        meta.getDescription().orElse(null));
  }

  public static Dataset toDataset(
      @NonNull final ExtendedDatasetRow extendedRow,
      @NonNull final List<DatasetFieldRow> fieldRows,
      @NonNull final DatasetVersionRow versionRow) {
    final DatasetName name = DatasetName.of(extendedRow.getName());
    final DatasetName physicalName = DatasetName.of(extendedRow.getPhysicalName());
    final Instant createdAt = extendedRow.getCreatedAt();
    final Instant updatedAt = extendedRow.getUpdatedAt();
    final SourceName sourceName = SourceName.of(extendedRow.getSourceName());
    final String description = extendedRow.getDescription().orElse(null);

    final DatasetType type = DatasetType.valueOf(extendedRow.getType());
    switch (type) {
      case DB_TABLE:
        return new DbTable(
            name, physicalName, createdAt, updatedAt, sourceName, toField(fieldRows), description);
      case STREAM:
        final URL schemaLocation = Utils.toUrl(((StreamVersionRow) versionRow).getSchemaLocation());
        return new Stream(
            name,
            physicalName,
            createdAt,
            updatedAt,
            sourceName,
            schemaLocation,
            toField(fieldRows),
            description);
      default:
        throw new IllegalArgumentException();
    }
  }

  public static Field toField(@NonNull final DatasetFieldRow row) {
    return new Field(
        FieldName.of(row.getName()),
        FieldType.valueOf(row.getType()),
        row.getDescription().orElse(null));
  }

  public static List<Field> toField(@NonNull final List<DatasetFieldRow> rows) {
    return rows.stream().map(Mapper::toField).collect(toImmutableList());
  }

  public static DatasetRow toDatasetRow(
      @NonNull final UUID namespaceRowUuid,
      @NonNull final UUID sourceRowUuid,
      @NonNull final DatasetName name,
      @NonNull final DatasetMeta meta) {
    final Instant now = Instant.now();
    return new DatasetRow(
        UUID.randomUUID(),
        toDatasetType(meta).toString(),
        now,
        now,
        namespaceRowUuid,
        sourceRowUuid,
        name.getValue(),
        meta.getPhysicalName().getValue(),
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

  public static DatasetFieldRow toDatasetFieldRow(
      @NonNull final UUID datasetUuid, @NonNull final Field field) {
    final Instant now = Instant.now();
    return new DatasetFieldRow(
        UUID.randomUUID(),
        field.getType().toString(),
        now,
        now,
        datasetUuid,
        field.getName().getValue(),
        field.getDescription().orElse(null));
  }

  public static DatasetVersionRow toDatasetVersionRow(
      @NonNull final UUID datasetUuid,
      @NonNull final UUID version,
      @NonNull final List<UUID> fieldUuids,
      @NonNull final DatasetMeta meta) {
    final UUID rowUuid = UUID.randomUUID();
    final Instant now = Instant.now();
    final UUID runUuid = meta.getRunId().orElse(null);

    if (meta instanceof StreamMeta) {
      final String schemaLocationString = ((StreamMeta) meta).getSchemaLocation().toString();
      return new StreamVersionRow(
          rowUuid, now, datasetUuid, version, fieldUuids, runUuid, schemaLocationString);
    }

    return new DatasetVersionRow(rowUuid, now, datasetUuid, version, fieldUuids, runUuid);
  }

  public static Job toJob(
      @NonNull final JobRow row,
      @NonNull final List<DatasetName> inputs,
      @NonNull final List<DatasetName> outputs,
      @Nullable final String locationString,
      @NonNull final String contextString,
      @Nullable final ExtendedRunRow runRow) {
    return new Job(
        JobType.valueOf(row.getType()),
        JobName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        inputs,
        outputs,
        (locationString == null) ? null : Utils.toUrl(locationString),
        Utils.fromJson(contextString, new TypeReference<Map<String, String>>() {}),
        row.getDescription().orElse(null),
        (runRow == null) ? null : toRun(runRow));
  }

  public static JobRow toJobRow(
      @NonNull final UUID namespaceUuid, @NonNull final JobName name, @NonNull final JobMeta meta) {
    final Instant now = Instant.now();
    return new JobRow(
        UUID.randomUUID(),
        meta.getType().toString(),
        now,
        now,
        namespaceUuid,
        name.getValue(),
        meta.getDescription().orElse(null),
        null);
  }

  public static JobContextRow toJobContextRow(
      @NonNull final Map<String, String> context, String checksum) {
    return new JobContextRow(UUID.randomUUID(), Instant.now(), Utils.toJson(context), checksum);
  }

  public static JobVersionRow toJobVersionRow(
      @NonNull final UUID jobRowUuid,
      @NonNull final UUID jobContextRowUuid,
      @NonNull final List<UUID> inputs,
      @NonNull final List<UUID> outputs,
      @Nullable final URL location,
      @NonNull final UUID version) {
    final Instant now = Instant.now();
    return new JobVersionRow(
        UUID.randomUUID(),
        now,
        now,
        jobRowUuid,
        jobContextRowUuid,
        inputs,
        outputs,
        (location == null) ? null : location.toString(),
        version,
        null);
  }

  public static Run toRun(@NonNull final ExtendedRunRow extendedRow) {
    return new Run(
        extendedRow.getUuid(),
        extendedRow.getCreatedAt(),
        extendedRow.getUpdatedAt(),
        extendedRow.getNominalStartTime().orElse(null),
        extendedRow.getNominalEndTime().orElse(null),
        Run.State.valueOf(extendedRow.getCurrentRunState().get()),
        Utils.fromJson(extendedRow.getArgs(), new TypeReference<Map<String, String>>() {}));
  }

  public static List<Run> toRun(@NonNull final List<ExtendedRunRow> rows) {
    return rows.stream().map(Mapper::toRun).collect(toImmutableList());
  }

  public static RunRow toRunRow(
      @NonNull final UUID jobVersionUuid,
      @NonNull final UUID runArgsUuid,
      @NonNull final RunMeta runMeta) {
    final Instant now = Instant.now();
    return new RunRow(
        UUID.randomUUID(),
        now,
        now,
        jobVersionUuid,
        runArgsUuid,
        runMeta.getNominalStartTime().orElse(null),
        runMeta.getNominalEndTime().orElse(null),
        null);
  }

  public static RunArgsRow toRunArgsRow(
      @NonNull final Map<String, String> args, @NonNull final String checksum) {
    return new RunArgsRow(UUID.randomUUID(), Instant.now(), Utils.toJson(args), checksum);
  }

  public static RunStateRow toRunStateRow(
      @NonNull final UUID runId, @NonNull final Run.State runState) {
    return new RunStateRow(UUID.randomUUID(), Instant.now(), runId, runState.toString());
  }
}
