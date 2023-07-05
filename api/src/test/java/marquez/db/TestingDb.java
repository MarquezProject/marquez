package marquez.db;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.NonNull;
import marquez.common.models.DatasetType;
import marquez.db.models.DatasetRow;
import marquez.db.models.NamespaceRow;
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

  private TestingDb(@NonNull final Jdbi delegate) {
    this.delegate = delegate;
  }

  /** Returns a new {@code TestingDb} object with the specified {@code jdbi} for delegation. */
  static TestingDb newInstance(@NonNull final Jdbi delegate) {
    return new TestingDb(delegate);
  }

  /** Execute {@code UPSERT} for the specified {@link NamespaceRow}. */
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

  /** Execute {@code UPSERT} for the specified {@link SourceRow}. */
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

  /** Execute {@code UPSERT} for all {@link SourceRow}s. */
  Set<DatasetRow> upsertAll(@NonNull Set<DatasetRow> rows) {
    final DatasetDao dao = delegate.onDemand(DatasetDao.class);
    final ImmutableSet.Builder<DatasetRow> rowsAdded = ImmutableSet.builder();
    for (final DatasetRow row : rows) {
      final DatasetRow upserted =
          dao.upsert(
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
      rowsAdded.add(upserted);
    }
    return rowsAdded.build();
  }

  /** Obtain a new {@link Handle} by delegating to underlying {@code jdbi}. */
  Handle open() {
    return delegate.open();
  }
}
