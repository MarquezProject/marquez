/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.common.models.CommonModelGenerator.newFields;

import com.google.common.collect.ImmutableSet;
import io.openlineage.client.OpenLineage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.models.DatasetType;
import marquez.common.models.JobType;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.IntervalMetric;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.LineageMetric;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

/**
 * Forwarding wrapper around an open connection to test database; utility methods {@code upsert()}
 * and {@code upsertAll()} implementations are intended for use in testing only. When querying the
 * test database (or opening a new {@link Handle}), interactions are delegated to underlying {@code
 * jdbi}.
 */
final class TestingDb {
  private final Jdbi delegate;

  private static final Instant NOW = Instant.now();

  private TestingDb(@NonNull final Jdbi delegate) {
    this.delegate = delegate;
  }

  /** Returns a new {@code TestingDb} object with the specified {@code jdbi} for delegation. */
  static TestingDb newInstance(@NonNull final Jdbi delegate) {
    return new TestingDb(delegate);
  }

  /** Execute {@code UPSERT} for the specified {@link NamespaceRow} object. */
  NamespaceRow upsert(@NonNull NamespaceRow row) {
    return delegate
        .onDemand(NamespaceDao.class)
        .upsertNamespaceRow(
            row.getUuid(),
            row.getCreatedAt(),
            row.getName(),
            row.getCurrentOwnerName(),
            row.getDescription().orElse(null));
  }

  /** Execute {@code UPSERT} for the specified {@link SourceRow} object. */
  SourceRow upsert(@NonNull SourceRow row) {
    return delegate
        .onDemand(SourceDao.class)
        .upsert(
            row.getUuid(),
            row.getType(),
            row.getCreatedAt(),
            row.getName(),
            row.getConnectionUrl());
  }

  <T> Set<T> upsertAll(@NonNull Object... rows) {
    return upsertAll(rows);
  }

  /** ... */
  <T> Set<T> upsertAll(@NonNull Set<?> rows) {
    ImmutableSet.Builder<T> upserted = new ImmutableSet.Builder<>();
    rows.forEach(
        row -> {
          upserted.add((T) upsert(row));
        });
    return upserted.build();
  }

  private Object upsert(Object row) {
    if (row instanceof DatasetRow) {
      return upsert((DatasetRow) row);
    } else if (row instanceof DatasetVersionRow) {
      return upsert((DatasetVersionRow) row);
    } else if (row instanceof JobRow) {
      return upsert((JobRow) row);
    } else if (row instanceof JobVersionRow) {
      return upsert((JobVersionRow) row);
    } else if (row instanceof RunRow) {
      return upsert((RunRow) row);
    }
    throw new IllegalArgumentException();
  }

  /** Execute {@code UPSERT} for the specified {@link DatasetRow} object. */
  DatasetRow upsert(@NonNull DatasetRow row) {
    return delegate
        .onDemand(DatasetDao.class)
        .upsert(
            row.getUuid(),
            DatasetType.valueOf(row.getType()),
            row.getCreatedAt(),
            row.getNamespaceUuid(),
            row.getNamespaceName(),
            row.getSourceUuid(),
            row.getSourceName(),
            row.getName(),
            row.getPhysicalName(),
            row.getDescription().orElse(null),
            row.isDeleted());
  }

  /** Execute {@code UPSERT} for the specified {@link DatasetRow} object. */
  DatasetVersionRow upsert(@NonNull DatasetVersionRow row) {
    return upsert(row, false);
  }

  /** Execute {@code UPSERT} for the specified {@link DatasetRow} object. */
  DatasetVersionRow upsert(@NonNull DatasetVersionRow row, boolean isCurrentVersion) {
    final DatasetVersionRow upserted =
        delegate
            .onDemand(DatasetVersionDao.class)
            .upsert(
                row.getUuid(),
                row.getCreatedAt(),
                row.getDatasetUuid(),
                row.getVersion(),
                row.getSchemaVersionUuid().orElse(null),
                row.getRunUuid().orElseThrow(),
                Columns.toPgObject(newFields(4)),
                row.getNamespaceName(),
                row.getDatasetName(),
                row.getLifecycleState());

    // ...
    if (isCurrentVersion) {
      delegate
          .onDemand(DatasetDao.class)
          .updateVersion(row.getDatasetUuid(), row.getCreatedAt(), row.getVersion());
    }
    return upserted;
  }

  /** Execute {@code UPSERT} for the specified {@link RunArgsRow} object. */
  RunArgsRow upsert(@NonNull RunArgsRow row) {
    return delegate
        .onDemand(RunArgsDao.class)
        .upsertRunArgs(row.getUuid(), row.getCreatedAt(), row.getArgs(), row.getChecksum());
  }

  /** Execute {@code UPSERT} for the specified {@link JobRow} object. */
  JobRow upsert(@NonNull JobRow row) {
    return delegate
        .onDemand(JobDao.class)
        .upsertJob(
            row.getUuid(),
            JobType.valueOf(row.getType()),
            row.getCreatedAt(),
            row.getNamespaceUuid(),
            row.getNamespaceName(),
            row.getName(),
            row.getDescription().orElse(null),
            row.getLocation(),
            null,
            null);
  }

  /** Execute {@code UPSERT} for the specified {@link JobVersionRow} object. */
  JobVersionRow upsert(@NonNull JobVersionRow row) {
    final JobVersionDao dao = delegate.onDemand(JobVersionDao.class);
    final JobVersionRow upserted =
        dao.upsertJobVersion(
            row.getUuid(),
            row.getCreatedAt(),
            row.getJobUuid(),
            row.getLocation().orElse(null),
            row.getVersion(),
            row.getJobName(),
            row.getNamespaceUuid(),
            row.getNamespaceName());
    row.getInputUuids()
        .forEach(in -> dao.upsertInputDatasetFor(row.getUuid(), in, row.getJobUuid(), null));
    row.getInputUuids()
        .forEach(out -> dao.upsertInputDatasetFor(row.getUuid(), out, row.getJobUuid(), null));
    // ...
    delegate.onDemand(JobDao.class).updateVersionFor(row.getJobUuid(), NOW, upserted.getUuid());
    return upserted;
  }

  RunRow upsert(@NonNull RunRow row) {
    return upsertWith(row, null);
  }

  RunRow upsertWith(@NonNull RunRow row, @Nullable final UUID datasetVersionUuidAsInput) {
    final RunDao dao = delegate.onDemand(RunDao.class);
    final RunRow upserted =
        dao.upsert(
            row.getUuid(),
            row.getParentRunUuid().orElse(null),
            null, // ...
            row.getCreatedAt(),
            row.getJobUuid(),
            row.getJobVersionUuid().orElse(null),
            row.getRunArgsUuid(),
            row.getNominalStartTime().orElse(null),
            row.getNominalEndTime().orElse(null),
            row.getNamespaceName(),
            row.getJobName(),
            null // ...
            );
    // ...
    Optional.ofNullable(datasetVersionUuidAsInput)
        .ifPresent(uuidOfInput -> dao.updateInputMapping(row.getUuid(), uuidOfInput));
    return upserted;
  }

  void insertAll(@NonNull Set<OpenLineage.RunEvent> olEvents) {
    for (final OpenLineage.RunEvent olEvent : olEvents) {
      insert(olEvent);
    }
  }

  void insert(@NonNull OpenLineage.RunEvent olEvent) {
    delegate
        .onDemand(OpenLineageDao.class)
        .createLineageEvent(
            olEvent.getEventType().toString(),
            olEvent.getEventTime().toInstant(),
            olEvent.getRun().getRunId(),
            olEvent.getJob().getName(),
            olEvent.getJob().getNamespace(),
            Columns.toPgObject(olEvent),
            olEvent.getProducer().toASCIIString());
  }

  /** Obtain a new {@link Handle} by delegating to underlying {@code jdbi}. */
  Handle open() {
    return delegate.open();
  }

  List<LineageMetric> lastDayLineageMetrics() {
    return delegate.onDemand(StatsDao.class).getLastDayMetrics();
  }

  List<LineageMetric> lastWeekLineageMetrics(String timezone) {
    return delegate.onDemand(StatsDao.class).getLastWeekMetrics(timezone);
  }

  List<IntervalMetric> lastDayJobMetrics() {
    return delegate.onDemand(StatsDao.class).getLastDayJobs();
  }

  List<IntervalMetric> lastWeekJobMetrics(String timezone) {
    return delegate.onDemand(StatsDao.class).getLastWeekJobs(timezone);
  }

  List<IntervalMetric> lastDayDatasetMetrics() {
    return delegate.onDemand(StatsDao.class).getLastDayDatasets();
  }

  List<IntervalMetric> lastWeekDatasetMetrics(String timezone) {
    return delegate.onDemand(StatsDao.class).getLastWeekDatasets(timezone);
  }

  List<IntervalMetric> lastDaySourceMetrics() {
    return delegate.onDemand(StatsDao.class).getLastDaySources();
  }

  List<IntervalMetric> lastWeekSourceMetrics(String timezone) {
    return delegate.onDemand(StatsDao.class).getLastWeekSources(timezone);
  }
}
