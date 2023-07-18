/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static java.time.temporal.ChronoUnit.DAYS;
import static marquez.api.models.ApiModelGenerator.newRunEvents;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.db.models.DbModelGenerator.newDatasetRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetRowsWith;
import static marquez.db.models.DbModelGenerator.newDatasetVersionRowWith;
import static marquez.db.models.DbModelGenerator.newDatasetVersionsRowWith;
import static marquez.db.models.DbModelGenerator.newJobRowWith;
import static marquez.db.models.DbModelGenerator.newJobRowsWith;
import static marquez.db.models.DbModelGenerator.newJobVersionRowWith;
import static marquez.db.models.DbModelGenerator.newJobVersionRowsWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newRunArgRow;
import static marquez.db.models.DbModelGenerator.newRunRowWith;
import static marquez.db.models.DbModelGenerator.newRunRowsWith;
import static marquez.db.models.DbModelGenerator.newSourceRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import marquez.db.exceptions.DbRetentionException;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

/** The test suite for {@link DbRetention}. */
@Tag("IntegrationTests")
public class DbRetentionTest extends DbTest {
  private static final int NUMBER_OF_ROWS_PER_BATCH = 10;
  private static final int RETENTION_DAYS = 30;
  private static final boolean DRY_RUN = true;
  private static final Instant OLDER_THAN_X_DAYS = Instant.now().minus(RETENTION_DAYS + 1, DAYS);
  private static final Instant LAST_X_DAYS = Instant.now().minus(RETENTION_DAYS - 1, DAYS);

  @Test
  public void testRetentionOnDbOrErrorWithJobsOlderThanXDays() {
    // (1) Add namespace.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());

    // (2) Add jobs older than X days.
    final Set<JobRow> rowsOlderThanXDays =
        DB.upsertAll(
            newJobRowsWith(OLDER_THAN_X_DAYS, namespaceRow.getUuid(), namespaceRow.getName(), 4));

    // (3) Add jobs within last X days.
    final Set<JobRow> rowsLastXDays =
        DB.upsertAll(
            newJobRowsWith(LAST_X_DAYS, namespaceRow.getUuid(), namespaceRow.getName(), 2));

    // (4) Apply retention policy as dry run on jobs older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS, DRY_RUN);
      // (5) Query 'jobs' table for rows. We want to ensure: jobs older than X days
      // have not deleted; jobs within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply dry run", e);
    }

    // (6) Apply retention policy on jobs older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (7) Query 'jobs' table for rows deleted. We want to ensure: jobs older than X days
      // have been deleted; jobs within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void testRetentionOnDbOrErrorWithJobVersionsOlderThanXDays() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add dataset (as inputs) associated with job version.
    final Set<DatasetRow> datasetsAsInput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (3) Add dataset (as outputs) associated with job version.
    final Set<DatasetRow> datasetsAsOutput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (4) Use any output dataset for job versions to obtain namespace and associate with job.
    final DatasetRow datasetAsOutput = datasetsAsOutput.stream().findAny().orElseThrow();
    final UUID namespaceUuid = datasetAsOutput.getNamespaceUuid();
    final String namespaceName = datasetAsOutput.getNamespaceName();

    // (5) Add job.
    final JobRow jobRow = DB.upsert(newJobRowWith(namespaceUuid, namespaceName));

    // (6) Add job versions older than X days associated with job.
    final Set<JobVersionRow> rowsOlderThanXDays =
        DB.upsertAll(
            newJobVersionRowsWith(
                OLDER_THAN_X_DAYS,
                namespaceUuid,
                namespaceName,
                jobRow.getUuid(),
                jobRow.getName(),
                datasetsAsInput,
                datasetsAsOutput,
                4));

    // (7) Add job versions within last X days associated with job.
    final Set<JobVersionRow> rowsLastXDays =
        DB.upsertAll(
            newJobVersionRowsWith(
                LAST_X_DAYS,
                namespaceUuid,
                namespaceName,
                jobRow.getUuid(),
                jobRow.getName(),
                datasetsAsInput,
                datasetsAsOutput,
                2));

    // (8) Apply retention policy as dry run on job versions older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS, DRY_RUN);
      // (9) Query 'job versions' table for rows. We want to ensure: job versions older
      // than X days have not been deleted; job versions within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply dry run", e);
    }

    // (10) Apply retention policy on job versions older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (11) Query 'job versions' table for rows deleted. We want to ensure: job versions older
      // than X days have been deleted; job versions within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void testRetentionOnDbOrErrorWithRunsOlderThanXDays() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add dataset (as inputs) associated with job.
    final Set<DatasetRow> datasetsAsInput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (3) Add dataset (as outputs) associated with job.
    final Set<DatasetRow> datasetsAsOutput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (4) Use any output dataset for run to obtain namespace and associate with job.
    final DatasetRow datasetAsOutput = datasetsAsOutput.stream().findAny().orElseThrow();
    final UUID namespaceUuid = datasetAsOutput.getNamespaceUuid();
    final String namespaceName = datasetAsOutput.getNamespaceName();

    // (5) Add version for job.
    final JobRow jobRow = DB.upsert(newJobRowWith(namespaceUuid, namespaceName));
    final JobVersionRow jobVersionRow =
        DB.upsert(
            newJobVersionRowWith(
                namespaceUuid,
                namespaceName,
                jobRow.getUuid(),
                jobRow.getName(),
                datasetsAsInput,
                datasetsAsOutput));

    // (6) Add args for run.
    final RunArgsRow runArgsRow = DB.upsert(newRunArgRow());

    // (7) Add runs older than X days.
    final Set<RunRow> rowsOlderThanXDays =
        DB.upsertAll(
            newRunRowsWith(
                OLDER_THAN_X_DAYS,
                jobRow.getUuid(),
                jobVersionRow.getUuid(),
                runArgsRow.getUuid(),
                4));

    // (8) Add runs within last X days.
    final Set<RunRow> rowsLastXDays =
        DB.upsertAll(
            newRunRowsWith(
                LAST_X_DAYS, jobRow.getUuid(), jobVersionRow.getUuid(), runArgsRow.getUuid(), 2));

    // (9) Apply retention policy as dry run on runs older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS, DRY_RUN);
      // (10) Query 'runs' table for rows. We want to ensure: runs older than X days have not been
      // deleted; runs within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply dry run", e);
    }

    // (11) Apply retention policy on runs older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (12) Query 'runs' table for rows deleted. We want to ensure: runs older than X days have
      // been deleted; runs within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

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

    // (4) Apply retention policy as dry run on datasets older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS, DRY_RUN);
      // (5) Query 'datasets' table for rows. We want to ensure: datasets older than X days
      // have not been deleted; datasets within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply dry run", e);
    }

    // (6) Apply retention policy on datasets older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (7) Query 'datasets' table for rows deleted. We want to ensure: datasets older than X days
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
    final DatasetRow rowLastXDaysAsOutput = rowsLastXDaysAsOutput.stream().findAny().orElseThrow();
    final UUID namespaceUuid = rowLastXDaysAsOutput.getNamespaceUuid();
    final String namespaceName = rowLastXDaysAsOutput.getNamespaceName();

    // (6) Add job and associate with job version; the job version will have input and output
    // datasets older than X days and within last X days, respectively.
    final JobRow jobRow = DB.upsert(newJobRowWith(namespaceUuid, namespaceName));
    DB.upsert(
        newJobVersionRowWith(
            namespaceUuid,
            namespaceName,
            jobRow.getUuid(),
            jobRow.getName(),
            rowsOlderThanXDaysAsInput,
            rowsLastXDaysAsOutput));

    // (7) Apply retention policy on datasets older than X days.
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

    // (2) Add dataset.
    final DatasetRow datasetRow =
        DB.upsert(
            newDatasetRowWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName()));

    // (3) Add versions for dataset older than X days.
    final Set<DatasetVersionRow> rowsOlderThanXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                OLDER_THAN_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                4));

    // (4) Add versions for dataset within last X days.
    final Set<DatasetVersionRow> rowsLastXDays =
        DB.upsertAll(
            newDatasetVersionsRowWith(
                LAST_X_DAYS,
                datasetRow.getUuid(),
                datasetRow.getName(),
                datasetRow.getNamespaceName(),
                2));

    // (5) Apply retention policy on dataset versions older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (6) Query 'dataset versions' table for rows deleted. We want to ensure: dataset versions
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
      testRetentionOnDbOrErrorWithDatasetVersionsOlderThanXDays_skipIfVersionAsCurrentForDataset() {
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

    // (5) Apply retention policy on dataset versions older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (6) Query 'dataset versions' table for rows deleted. We want to ensure: dataset versions
      // older than X days have been deleted; dataset versions within last X days have not been
      // deleted; dataset versions older than X days associated with a dataset (as current version)
      // has not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDays)).isTrue();
        assertThat(DbTestUtils.rowExists(handle, rowOlderThanXDaysAsCurrent)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void
      testRetentionOnDbOrErrorWithDatasetVersionsOlderThanXDays_skipIfVersionAsInputForRun() {
    // (1) Add namespace and source.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());
    final SourceRow sourceRow = DB.upsert(newSourceRow());

    // (2) Add dataset (as inputs) associated with job.
    final Set<DatasetRow> datasetsAsInput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                2));

    // (3) Add dataset (as outputs) associated with job.
    final Set<DatasetRow> datasetsAsOutput =
        DB.upsertAll(
            newDatasetRowsWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName(),
                4));

    // (4) Add dataset versions older than X days for each input datasets associated with run.
    final ImmutableSet.Builder<DatasetVersionRow> builderRowsOlderThanXDaysAsInput =
        ImmutableSet.builder();
    for (final DatasetRow rowAsInput : datasetsAsInput) {
      builderRowsOlderThanXDaysAsInput.addAll(
          DB.upsertAll(
              newDatasetVersionsRowWith(
                  OLDER_THAN_X_DAYS,
                  rowAsInput.getUuid(),
                  rowAsInput.getName(),
                  rowAsInput.getNamespaceName(),
                  4)));
    }
    final Set<DatasetVersionRow> rowsOlderThanXDaysAsInput =
        builderRowsOlderThanXDaysAsInput.build();

    // (5) Add dataset versions within last X days for each output datasets associated with run.
    final ImmutableSet.Builder<DatasetVersionRow> builderRowsLastXDaysAsOutput =
        ImmutableSet.builder();
    for (final DatasetRow rowAsOutput : datasetsAsOutput) {
      builderRowsLastXDaysAsOutput.addAll(
          DB.upsertAll(
              newDatasetVersionsRowWith(
                  LAST_X_DAYS,
                  rowAsOutput.getUuid(),
                  rowAsOutput.getName(),
                  rowAsOutput.getNamespaceName(),
                  2)));
    }
    final Set<DatasetVersionRow> rowsLastXDaysAsOutput = builderRowsLastXDaysAsOutput.build();

    // (6) Use any output dataset for run to obtain namespace and associate with job.
    final DatasetRow datasetAsOutput = datasetsAsOutput.stream().findAny().orElseThrow();
    final UUID namespaceUuid = datasetAsOutput.getNamespaceUuid();
    final String namespaceName = datasetAsOutput.getNamespaceName();

    // (7) Add version for job.
    final JobRow jobRow = DB.upsert(newJobRowWith(namespaceUuid, namespaceName));
    final JobVersionRow jobVersionRow =
        DB.upsert(
            newJobVersionRowWith(
                namespaceUuid,
                namespaceName,
                jobRow.getUuid(),
                jobRow.getName(),
                datasetsAsInput,
                datasetsAsOutput));

    // (8) Add run and associate with job and version.
    final RunArgsRow runArgsRow = DB.upsert(newRunArgRow());
    final RunRow runRow =
        newRunRowWith(jobRow.getUuid(), jobVersionRow.getUuid(), runArgsRow.getUuid());

    // (9) Add dataset version (as input) older than X days associated with run;
    // therefore, the dataset version will be skipped when applying retention policy.
    final DatasetRow datasetAsInput = datasetsAsInput.stream().findAny().orElseThrow();
    final DatasetVersionRow rowOlderThanXDaysAsInput =
        DB.upsert(
            newDatasetVersionRowWith(
                LAST_X_DAYS,
                datasetAsInput.getUuid(),
                datasetAsInput.getName(),
                datasetAsInput.getNamespaceName(),
                runRow.getUuid()));
    DB.upsertWith(runRow, rowOlderThanXDaysAsInput.getUuid());

    // (10) Apply retention policy on dataset versions older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (11) Query 'dataset versions' table for rows deleted. We want to ensure: dataset versions
      // older than X days associated with a run (as input) has not been deleted; dataset versions
      // older than X days have been deleted; dataset versions within last X days have not been
      // deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.rowExists(handle, rowOlderThanXDaysAsInput)).isTrue();
        assertThat(DbTestUtils.rowsExist(handle, rowsOlderThanXDaysAsInput)).isFalse();
        assertThat(DbTestUtils.rowsExist(handle, rowsLastXDaysAsOutput)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }

  @Test
  public void testRetentionOnDbOrErrorWithOlEventsOlderThanXDays() {
    // (1) Configure OL.
    final URI olProducer = URI.create("https://test.com/test");
    final OpenLineage ol = new OpenLineage(olProducer);

    // (2) Add namespace and job for OL events.
    final String namespaceName = newNamespaceName().getValue();
    final String jobName = newJobName().getValue();

    // (3) Add OL events older than X days.
    final Set<OpenLineage.RunEvent> olEventsOlderThanXDays =
        newRunEvents(ol, OLDER_THAN_X_DAYS, namespaceName, jobName, 4);
    DB.insertAll(olEventsOlderThanXDays);

    // (4) Add OL events within last X days.
    final Set<OpenLineage.RunEvent> olEventsLastXDays =
        newRunEvents(ol, LAST_X_DAYS, namespaceName, jobName, 2);
    DB.insertAll(olEventsLastXDays);

    // (5) Apply retention policy as dry run on OL events older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS, DRY_RUN);
      // (6) Query 'lineage events' table for events. We want to ensure: OL events older than X
      // days have not been deleted; OL events within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.olEventsExist(handle, olEventsOlderThanXDays)).isTrue();
        assertThat(DbTestUtils.olEventsExist(handle, olEventsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply dry run", e);
    }

    // (7) Apply retention policy on OL events older than X days.
    try {
      DbRetention.retentionOnDbOrError(
          jdbiExtension.getJdbi(), NUMBER_OF_ROWS_PER_BATCH, RETENTION_DAYS);
      // (8) Query 'lineage events' table for events deleted. We want to ensure: OL events older
      // than X days have been deleted; OL events within last X days have not been deleted.
      try (final Handle handle = DB.open()) {
        assertThat(DbTestUtils.olEventsExist(handle, olEventsOlderThanXDays)).isFalse();
        assertThat(DbTestUtils.olEventsExist(handle, olEventsLastXDays)).isTrue();
      }
    } catch (DbRetentionException e) {
      fail("failed to apply retention policy", e);
    }
  }
}
