/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Dataset;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Regression tests for https://github.com/MarquezProject/marquez/issues/3083: a {@code
 * dataset_fields} row whose {@code type} is {@code null} must not be duplicated on repeated
 * upserts, since duplicate {@code dataset_fields} rows each independently accumulate their own
 * {@code column_lineage} edges, causing a combinatorial explosion.
 */
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class DatasetFieldDaoTest {

  private static DatasetFieldDao datasetFieldDao;
  private static DatasetDao datasetDao;
  private static Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    DatasetFieldDaoTest.jdbi = jdbi;
    datasetFieldDao = jdbi.onDemand(DatasetFieldDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    marquez.api.JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testUpsert_withNullType_doesNotDuplicateOnRepeatedUpserts() {
    final Dataset dataset = DbTestUtils.newDataset(jdbi);
    final DatasetRow datasetRow =
        datasetDao.getUuid(dataset.getNamespace().getValue(), dataset.getName().getValue()).get();

    final String fieldName = "modified_at";
    final Instant now = Instant.now();

    // Simulate the same field, with an unknown/null type, being reported across multiple
    // OpenLineage events for the same dataset - each upsert uses a freshly-generated candidate
    // UUID, exactly as OpenLineageDao.upsertFields(...) does for every incoming event.
    DatasetFieldRow first =
        datasetFieldDao.upsert(
            UUID.randomUUID(), now, fieldName, null, null, datasetRow.getUuid());
    DatasetFieldRow second =
        datasetFieldDao.upsert(
            UUID.randomUUID(), now, fieldName, null, null, datasetRow.getUuid());
    DatasetFieldRow third =
        datasetFieldDao.upsert(
            UUID.randomUUID(), now, fieldName, null, null, datasetRow.getUuid());

    // All three upserts must resolve to the *same* underlying row.
    assertThat(second.getUuid()).isEqualTo(first.getUuid());
    assertThat(third.getUuid()).isEqualTo(first.getUuid());

    // And only a single dataset_fields row should exist for this field.
    Integer rowCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT count(*) FROM dataset_fields WHERE dataset_uuid = :datasetUuid AND name = :name")
                    .bind("datasetUuid", datasetRow.getUuid())
                    .bind("name", fieldName)
                    .mapTo(Integer.class)
                    .one());
    assertThat(rowCount).isEqualTo(1);
  }

  @Test
  public void testUpsert_withNullType_isStoredAsUnknownSentinel() {
    final Dataset dataset = DbTestUtils.newDataset(jdbi);
    final DatasetRow datasetRow =
        datasetDao.getUuid(dataset.getNamespace().getValue(), dataset.getName().getValue()).get();

    DatasetFieldRow row =
        datasetFieldDao.upsert(
            UUID.randomUUID(), Instant.now(), "unknown_type_field", null, null, datasetRow.getUuid());

    assertThat(row.getType()).isEqualTo("UNKNOWN");
  }

  @Test
  public void testUpsert_withRealType_isUnaffected() {
    final Dataset dataset = DbTestUtils.newDataset(jdbi);
    final DatasetRow datasetRow =
        datasetDao.getUuid(dataset.getNamespace().getValue(), dataset.getName().getValue()).get();

    final Instant now = Instant.now();
    DatasetFieldRow first =
        datasetFieldDao.upsert(
            UUID.randomUUID(), now, "typed_field", "VARCHAR", null, datasetRow.getUuid());
    DatasetFieldRow second =
        datasetFieldDao.upsert(
            UUID.randomUUID(), now, "typed_field", "VARCHAR", null, datasetRow.getUuid());

    assertThat(first.getType()).isEqualTo("VARCHAR");
    assertThat(second.getUuid()).isEqualTo(first.getUuid());
  }
}
