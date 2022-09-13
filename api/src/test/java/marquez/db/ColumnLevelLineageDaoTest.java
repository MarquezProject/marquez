/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.common.models.DatasetType;
import marquez.db.models.ColumnLevelLineageRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLevelLineageDaoTest {

  private static ColumnLevelLineageDao dao;
  private static DatasetFieldDao fieldDao;
  private static DatasetDao datasetDao;
  private static NamespaceDao namespaceDao;
  private static SourceDao sourceDao;
  private static DatasetVersionDao datasetVersionDao;

  private UUID outputDatasetFieldUuid = UUID.randomUUID();
  private String transformationDescription = "some-description";
  private String transformationType = "some-type";
  private Instant now = Instant.now();
  private DatasetRow datasetRow;
  private DatasetVersionRow versionRow;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(ColumnLevelLineageDao.class);
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
    datasetRow =
        datasetDao.upsert(
            UUID.randomUUID(),
            DatasetType.DB_TABLE,
            now,
            namespaceRow.getUuid(),
            "",
            sourceRow.getUuid(),
            "",
            "",
            "",
            "",
            false);
    versionRow =
        datasetVersionDao.upsert(
            UUID.randomUUID(),
            now,
            datasetRow.getUuid(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            "",
            "",
            "");

    // insert output dataset field
    fieldDao.upsert(
        outputDatasetFieldUuid, now, "output-field", "string", "desc", datasetRow.getUuid());
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM column_level_lineage");
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
    UUID input1 = UUID.randomUUID();
    UUID input2 = UUID.randomUUID();

    // insert input dataset fields
    fieldDao.upsert(input1, now, "a", "string", "desc", datasetRow.getUuid());
    fieldDao.upsert(input2, now, "b", "string", "desc", datasetRow.getUuid());

    List<ColumnLevelLineageRow> rows =
        dao.upsertColumnLevelLineageRow(
            versionRow.getUuid(),
            outputDatasetFieldUuid,
            Arrays.asList(input1, input2),
            transformationDescription,
            transformationType,
            now);

    assertEquals(2, rows.size());
    assertEquals(versionRow.getUuid(), rows.get(0).getOutputDatasetVersionUuid());
    assertEquals(outputDatasetFieldUuid, rows.get(0).getOutputDatasetFieldUuid());
    assertEquals(transformationDescription, rows.get(0).getTransformationDescription());
    assertEquals(transformationType, rows.get(0).getTransformationType());
    assertEquals(now, rows.get(0).getCreatedAt());
    assertEquals(now, rows.get(0).getUpdatedAt());
  }

  @Test
  void testUpsertEmptyList() {
    List<ColumnLevelLineageRow> rows =
        dao.upsertColumnLevelLineageRow(
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
    UUID input = UUID.randomUUID();
    fieldDao.upsert(input, now, "a", "string", "desc", datasetRow.getUuid());

    dao.upsertColumnLevelLineageRow(
        versionRow.getUuid(),
        outputDatasetFieldUuid,
        Arrays.asList(input),
        transformationDescription,
        transformationType,
        now);
    List<ColumnLevelLineageRow> rows =
        dao.upsertColumnLevelLineageRow(
            versionRow.getUuid(),
            outputDatasetFieldUuid,
            Arrays.asList(input),
            transformationDescription,
            transformationType,
            now.plusSeconds(1000));

    // make sure there is one row with updatedAt modified
    assertEquals(1, rows.size());
    assertEquals(now.plusSeconds(1000), rows.get(0).getUpdatedAt());
  }
}
