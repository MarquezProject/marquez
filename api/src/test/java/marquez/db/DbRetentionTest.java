/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.temporal.ChronoUnit.DAYS;
import static marquez.db.models.DbModelGenerator.newDatasetRowsWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newSourceRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import marquez.db.exceptions.DbRetentionException;
import marquez.db.models.DatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The test suite for {@link DbRetention}. */
@Tag("IntegrationTests")
public class DbRetentionTest extends DbTest {
  private static final int NUMBER_OF_ROWS_PER_BATCH = 10;
  private static final int RETENTION_DAYS = 30;
  private static final int OLDER_THAN_X_DAYS = 31;

  @Test
  public void testRetentionOnDbOrError_withDatasetsOlderThanXDays() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add datasets older than X days.
    final Instant olderThan30Days = Instant.now().minus(OLDER_THAN_X_DAYS, DAYS);
    final Set<DatasetRow> rowsOlderThan30Days =
        newDatasetRowsWith(
            olderThan30Days,
            namespaceRow.getUuid(),
            namespaceRow.getName(),
            sourceRow.getUuid(),
            sourceRow.getName(),
            4);
    DB.upsertAll(rowsOlderThan30Days);

    // (3) Add datasets with tha X days.
    final Instant last30Days = Instant.now();
    final Set<DatasetRow> rowsLast30Days =
        DB.upsertAll(
            newDatasetRowsWith(
                last30Days,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (4) Apply retention policy.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }

    // (5) Query 'datasets' table for rows deleted.
    try (final Handle handle = DB.open()) {
      // Ensure rows older than X days have been deleted.
      final Set<UUID> uuidsForRowsOlderThan30Days =
          rowsOlderThan30Days.stream().map(DatasetRow::getUuid).collect(toImmutableSet());
      final boolean rowsOlderThan30DaysExist =
          handle
              .createQuery(
                  "SELECT EXISTS (SELECT 1 FROM datasets WHERE uuid IN (<uuidsForRowsOlderThan30Days>))")
              .bindList("uuidsForRowsOlderThan30Days", uuidsForRowsOlderThan30Days)
              .mapTo(Boolean.class)
              .one();
      assertThat(rowsOlderThan30DaysExist).isFalse();
      // Ensure rows within last X days have not been deleted.
      final Set<UUID> uuidsForRowsLast30Days =
          rowsLast30Days.stream().map(DatasetRow::getUuid).collect(toImmutableSet());
      final boolean rowsLast30DaysExist =
          handle
              .createQuery(
                  "SELECT EXISTS (SELECT 1 FROM datasets WHERE uuid IN (<uuidsForRowsLast30Days>))")
              .bindList("uuidsForRowsLast30Days", uuidsForRowsLast30Days)
              .mapTo(Boolean.class)
              .one();
      assertThat(rowsLast30DaysExist).isTrue();
    }
  }
}
