/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static java.time.temporal.ChronoUnit.DAYS;
import static marquez.db.models.DbModelGenerator.newDatasetRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowsWith;
import static marquez.db.models.DbModelGenerator.newDatasetVersionRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetVersionsRowWith;
import static marquez.db.models.DbModelGenerator.newJobRow;
import static marquez.db.models.DbModelGenerator.newJobVersionRowWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newSourceRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import marquez.db.exceptions.DbRetentionException;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.JobRow;
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
  private static final Instant OLDER_THAN_X_DAYS = Instant.now().minus(RETENTION_DAYS + 1, DAYS);
  private static final Instant LAST_X_DAYS = Instant.now().minus(RETENTION_DAYS - 1, DAYS);

  @Test
  public void testRetentionOnDbOrErrorWithDatasetsOlderThanXDays() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add datasets older than X days.
    final Set<DatasetRow> rowsOlderThanXDays =
        DB.upsertAll(
            newDatasetRowsWith(
                OLDER_THAN_X_DAYS,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (3) Add datasets within last X days.
    final Set<DatasetRow> rowsLastXDays =
        DB.upsertAll(
            newDatasetRowsWith(
                LAST_X_DAYS,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (4) Apply retention policy on datasets older than X days
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (5) Query 'datasets' table for rows deleted. We want to ensure: datasets older than X days
      // have been deleted; datasets within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void
      testRetentionOnDbOrErrorWithDatasetsOlderThanXDays_skipIfDatasetAsInputOrOutputForJobVersion() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add datasets older than X days not associated with a job version; therefore, datasets
    // will be deleted when applying retention policy.
    final Set<DatasetRow> rowsOlderThanXDays =
        DB.upsertAll(
            newDatasetRowsWith(
                OLDER_THAN_X_DAYS,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (3) Add datasets (as inputs) older than X days associated with a job version; therefore,
    // datasets will be skipped when applying retention policy.
    final Set<DatasetRow> rowsOlderThanXDaysAsInput =
        DB.upsertAll(
            newDatasetRowsWith(
                OLDER_THAN_X_DAYS,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (4) Add datasets (as outputs) within last X days associated with a job version; therefore,
    // datasets will be skipped when applying retention policy.
    final Set<DatasetRow> rowsLastXDaysAsOutput =
        DB.upsertAll(
            newDatasetRowsWith(
                LAST_X_DAYS,
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (5) Use any output dataset to obtain namespace and associate with job.
    final DatasetRow rowOlderThanXDaysAsInput =
        rowsOlderThanXDaysAsInput.stream().findAny().orElseThrow();
    final String namespaceName = rowOlderThanXDaysAsInput.getNamespaceName();
    final UUID namespaceUuid = rowOlderThanXDaysAsInput.getNamespaceUuid();

    // (6) Add job and associate with job version; the job version will have input and output
    // datasets older than X days and within last X days, respectively.
    final JobRow jobRow = DB.upsert(newJobRow(namespaceUuid, namespaceName));
    DB.upsert(
        newJobVersionRowWith(
            namespaceUuid,
            namespaceName,
            jobRow.getUuid(),
            jobRow.getName(),
            rowsOlderThanXDaysAsInput,
            rowsLastXDaysAsOutput));

    // (7) Apply retention policy.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (8) Query 'datasets' table for rows deleted. We want to ensure: datasets older than X days
      // not associated with a job version have been deleted; datasets older than X days associated
      // with a job version have not been deleted; datasets within last X days associated with a job
      // version have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDaysAsInput)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDaysAsOutput)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void testRetentionOnDbOrErrorWithDatasetVersionsOlderThanXDays() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());
    final DatasetRow datasetRow =
        DB.upsert(
            newDatasetRowWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName()));

    // (2) Add dataset versions older than X days.
    final Set<DatasetVersionRow> rowsOlderThanXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                OLDER_THAN_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                4));

    // (3) Add dataset versions within last X days.
    final Set<DatasetVersionRow> rowsLastXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                LAST_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                2));

    // (4) Apply retention policy.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (5) Query 'dataset versions' table for rows deleted. We want to ensure: dataset versions
      // older than X days have been deleted; datasets within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void
      testRetentionOnDbOrErrorWithDatasetVersionsOlderThanXDays_skipIfVersionIsCurrentForDataset() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());
    final DatasetRow datasetRow =
        DB.upsert(
            newDatasetRowWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName()));

    // (2) Add dataset versions older than X days.
    final Set<DatasetVersionRow> rowsOlderThanXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                OLDER_THAN_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                4));

    // (3) Add dataset versions within last X days.
    final Set<DatasetVersionRow> rowsLastXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                LAST_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                2));

    // (4) Add dataset version older than X days associated with dataset (as current version);
    // therefore, the dataset version will be skipped when applying retention policy.
    final DatasetVersionRow rowOlderThanXDaysAsCurrent =
        DB.upsert(
            newDatasetVersionRowWith(
                LAST_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName()),
            true);

    // (5) Apply retention policy.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (6) Query 'dataset versions' table for rows deleted. We want to ensure: dataset versions
      // older than X days have been deleted; dataset versions within last X days have not been
      // deleted; dataset versions older than X days associated with a dataset (as current version)
      // has not been deleted
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
        assertThat(DbTestUtils.rowExist(handle, rowOlderThanXDaysAsCurrent)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }
}
