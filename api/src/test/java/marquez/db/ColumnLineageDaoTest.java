/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.common.models.DatasetType;
import marquez.db.models.ColumnLineageRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLineageDaoTest {

  private static ColumnLineageDao dao;
  private static DatasetFieldDao fieldDao;
  private static DatasetDao datasetDao;
  private static NamespaceDao namespaceDao;
  private static SourceDao sourceDao;
  private static DatasetVersionDao datasetVersionDao;

  private UUID outputDatasetFieldUuid = UUID.randomUUID();
  private String transformationDescription = "some-description";
  private String transformationType = "some-type";
  private Instant now = Instant.now();
  private DatasetRow inputDatasetRow;
  private DatasetRow outputDatasetRow;
  private DatasetVersionRow inputDatasetVersionRow;
  private DatasetVersionRow outputDatasetVersionRow;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(ColumnLineageDao.class);
    fieldDao = jdbi.onDemand(DatasetFieldDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    sourceDao = jdbi.onDemand(SourceDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
  }

  @BeforeEach
  public void setup() {
    // setup some dataset
    NamespaceRow namespaceRow =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, "", DEFAULT_NAMESPACE_OWNER);
    SourceRow sourceRow = sourceDao.upsertOrDefault(UUID.randomUUID(), "", now, "", "");
    inputDatasetRow =
        datasetDao.upsert(
            UUID.randomUUID(),
            DatasetType.DB_TABLE,
            now,
            namespaceRow.getUuid(),
            "",
            sourceRow.getUuid(),
            "",
            "inputDataset",
            "",
            "",
            false);
    outputDatasetRow =
        datasetDao.upsert(
            UUID.randomUUID(),
            DatasetType.DB_TABLE,
            now,
            namespaceRow.getUuid(),
            "",
            sourceRow.getUuid(),
            "",
            "outputDataset",
            "",
            "",
            false);

    inputDatasetVersionRow =
        datasetVersionDao.upsert(
            UUID.randomUUID(),
            now,
            inputDatasetRow.getUuid(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "",
            "",
            "");
    outputDatasetVersionRow =
        datasetVersionDao.upsert(
            UUID.randomUUID(),
            now,
            outputDatasetRow.getUuid(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "",
            "",
            "");

    inputDatasetVersionRow =
        datasetVersionDao.upsert(
            UUID.randomUUID(),
            now,
            inputDatasetRow.getUuid(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "",
            "",
            "");

    // insert output dataset field
    fieldDao.upsert(
        outputDatasetFieldUuid, now, "output-field", "string", "desc", outputDatasetRow.getUuid());
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM column_lineage");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("DELETE FROM dataset_fields");
          handle.execute("DELETE FROM datasets");
          handle.execute("DELETE FROM sources");
          handle.execute("DELETE FROM namespaces");
          return null;
        });
  }

  @Test
  void testUpsertMultipleColumns() {
    UUID inputFieldUuid1 = UUID.randomUUID();
    UUID inputFieldUuid2 = UUID.randomUUID();

    // insert input dataset fields
    fieldDao.upsert(inputFieldUuid1, now, "a", "string", "desc", inputDatasetRow.getUuid());
    fieldDao.upsert(inputFieldUuid2, now, "b", "string", "desc", inputDatasetRow.getUuid());

    List<ColumnLineageRow> rows =
        dao.upsertColumnLineageRow(
            outputDatasetVersionRow.getUuid(),
            outputDatasetFieldUuid,
            Arrays.asList(
                Pair.of(inputDatasetVersionRow.getUuid(), inputFieldUuid1),
                Pair.of(inputDatasetVersionRow.getUuid(), inputFieldUuid2)),
            transformationDescription,
            transformationType,
            now);

    assertEquals(2, rows.size());
    assertEquals(inputDatasetVersionRow.getUuid(), rows.get(0).getInputDatasetVersionUuid());
    assertEquals(outputDatasetVersionRow.getUuid(), rows.get(0).getOutputDatasetVersionUuid());
    assertEquals(outputDatasetFieldUuid, rows.get(0).getOutputDatasetFieldUuid());
    assertTrue(
        Arrays.asList(inputFieldUuid1, inputFieldUuid2)
            .contains(rows.get(0).getInputDatasetFieldUuid())); // ordering may differ per run
    assertEquals(transformationDescription, rows.get(0).getTransformationDescription());
    assertEquals(transformationType, rows.get(0).getTransformationType());
    assertEquals(now.getEpochSecond(), rows.get(0).getCreatedAt().getEpochSecond());
    assertEquals(now.getEpochSecond(), rows.get(0).getUpdatedAt().getEpochSecond());
  }

  @Test
  void testUpsertEmptyList() {
    List<ColumnLineageRow> rows =
        dao.upsertColumnLineageRow(
            UUID.randomUUID(),
            outputDatasetFieldUuid,
            Collections.emptyList(), // provide empty list
            transformationDescription,
            transformationType,
            now);

    assertEquals(0, rows.size());
  }

  @Test
  void testUpsertOnUpdatePreventsDuplicates() {
    // insert input dataset fields
    UUID inputFieldUuid = UUID.randomUUID();
    fieldDao.upsert(inputFieldUuid, now, "a", "string", "desc", inputDatasetRow.getUuid());

    dao.upsertColumnLineageRow(
        inputDatasetVersionRow.getUuid(),
        outputDatasetFieldUuid,
        Arrays.asList(Pair.of(inputDatasetVersionRow.getUuid(), inputFieldUuid)),
        transformationDescription,
        transformationType,
        now);
    List<ColumnLineageRow> rows =
        dao.upsertColumnLineageRow(
            inputDatasetVersionRow.getUuid(),
            outputDatasetFieldUuid,
            Arrays.asList(Pair.of(inputDatasetVersionRow.getUuid(), inputFieldUuid)),
            transformationDescription,
            transformationType,
            now.plusSeconds(1000));

    // make sure there is one row with updatedAt modified
    assertEquals(1, rows.size());
    assertEquals(
        now.plusSeconds(1000).getEpochSecond(), rows.get(0).getUpdatedAt().getEpochSecond());
  }
}
