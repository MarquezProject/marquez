/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.ColumnLineageTestUtils.getDatasetC;
import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static marquez.db.OpenLineageDao.DEFAULT_NAMESPACE_OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.common.models.DatasetType;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.ColumnLineageRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class ColumnLineageDaoTest {

  private static OpenLineageDao openLineageDao;
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
  private LineageEvent.JobFacet jobFacet;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
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

    jobFacet = new LineageEvent.JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    ColumnLineageTestUtils.tearDown(jdbi);
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
    assertEquals(transformationDescription, rows.get(0).getTransformationDescription().get());
    assertEquals(transformationType, rows.get(0).getTransformationType().get());
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

  // dataset_A (col_a, col_b)
  // dataset_B (col_c) depends on (col_a, col_b)
  // dataset_C (col_d) depends on col_c
  @Test
  void testGetLineage() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();
    Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job2",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_B),
            Arrays.asList(dataset_C));

    UpdateLineageRow.DatasetRecord datasetRecord_c = lineageRow.getOutputs().get().get(0);
    UUID field_col_d = fieldDao.findUuid(datasetRecord_c.getDatasetRow().getUuid(), "col_d").get();
    Set<ColumnLineageNodeData> lineage =
        dao.getLineage(20, Collections.singletonList(field_col_d), false, Instant.now());

    assertEquals(2, lineage.size());

    ColumnLineageNodeData dataset_b =
        lineage.stream().filter(cd -> cd.getDataset().equals("dataset_b")).findAny().get();
    ColumnLineageNodeData dataset_c =
        lineage.stream().filter(cd -> cd.getDataset().equals("dataset_c")).findAny().get();

    // test dataset_c
    assertThat(dataset_c.getInputFields()).hasSize(1);
    assertEquals("col_d", dataset_c.getField());
    assertEquals("namespace", dataset_c.getInputFields().get(0).getNamespace());
    assertEquals("dataset_b", dataset_c.getInputFields().get(0).getDataset());
    assertEquals("col_c", dataset_c.getInputFields().get(0).getField());
    assertEquals("type2", dataset_c.getTransformationType());
    assertEquals("description2", dataset_c.getTransformationDescription());

    // test dataset_b
    assertThat(dataset_b.getInputFields()).hasSize(2);
    assertEquals("col_c", dataset_b.getField());
    assertEquals(
        "col_b",
        dataset_b.getInputFields().stream()
            .filter(f -> f.getField().equals("col_b"))
            .findAny()
            .get()
            .getField());
    assertEquals(
        "col_a",
        dataset_b.getInputFields().stream()
            .filter(f -> f.getField().equals("col_a"))
            .findAny()
            .get()
            .getField());

    assertEquals("namespace", dataset_b.getInputFields().get(0).getNamespace());
    assertEquals("dataset_a", dataset_b.getInputFields().get(0).getDataset());
    assertEquals("type1", dataset_b.getTransformationType());
    assertEquals("description1", dataset_b.getTransformationDescription());
  }

  @Test
  void testGetLineageWhenNoLineageForColumn() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();
    Dataset dataset_C = getDatasetC();

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job1",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_A),
            Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    UpdateLineageRow.DatasetRecord datasetRecord_a = lineageRow.getInputs().get().get(0);
    UUID field_col_a = fieldDao.findUuid(datasetRecord_a.getDatasetRow().getUuid(), "col_a").get();

    // assert lineage is empty
    assertThat(dao.getLineage(20, Collections.singletonList(field_col_a), false, Instant.now()))
        .isEmpty();
  }

  /**
   * Create dataset_d build on the topi of dataset_c. Lineage of depth 1 of dataset_d should be of
   * size 2 (instead of 3)
   */
  @Test
  void testGetLineageWithLimitedDepth() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();
    Dataset dataset_C = getDatasetC();
    Dataset dataset_D =
        new Dataset(
            "namespace",
            "dataset_d",
            LineageEvent.DatasetFacets.builder()
                .schema(
                    new LineageEvent.SchemaDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        Arrays.asList(new LineageEvent.SchemaField("col_e", "STRING", ""))))
                .columnLineage(
                    new LineageEvent.ColumnLineageDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        new LineageEvent.ColumnLineageDatasetFacetFields(
                            Collections.singletonMap(
                                "col_e",
                                new LineageEvent.ColumnLineageOutputColumn(
                                    Arrays.asList(
                                        new LineageEvent.ColumnLineageInputField(
                                            "namespace", "dataset_c", "col_d")),
                                    "",
                                    "")))))
                .build());

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job2",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_C),
            Arrays.asList(dataset_D));

    UpdateLineageRow.DatasetRecord datasetRecord_d = lineageRow.getOutputs().get().get(0);
    UUID field_col_e = fieldDao.findUuid(datasetRecord_d.getDatasetRow().getUuid(), "col_e").get();

    // make sure dataset are constructed properly
    assertThat(dao.getLineage(20, Collections.singletonList(field_col_e), false, Instant.now()))
        .hasSize(3);

    // depth 1 corresponds to single ColumnLineageData with other nodes as node inputFields
    assertThat(dao.getLineage(1, Collections.singletonList(field_col_e), false, Instant.now()))
        .hasSize(1);
  }

  @Test
  void testGetLineageWhenCycleExists() {
    Dataset dataset_A =
        new Dataset(
            "namespace",
            "dataset_a",
            LineageEvent.DatasetFacets.builder()
                .schema(
                    new LineageEvent.SchemaDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        Arrays.asList(
                            new LineageEvent.SchemaField("col_a", "STRING", ""),
                            new LineageEvent.SchemaField("col_b", "STRING", ""))))
                .columnLineage(
                    new LineageEvent.ColumnLineageDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        new LineageEvent.ColumnLineageDatasetFacetFields(
                            Collections.singletonMap(
                                "col_a",
                                new LineageEvent.ColumnLineageOutputColumn(
                                    Arrays.asList(
                                        new LineageEvent.ColumnLineageInputField(
                                            "namespace", "dataset_c", "col_d")),
                                    "description3",
                                    "type3")))))
                .build());
    Dataset dataset_B = getDatasetB();
    Dataset dataset_C = getDatasetC();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job2",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_B),
        Arrays.asList(dataset_C));

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job3",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_C),
            Arrays.asList(dataset_A));

    UpdateLineageRow.DatasetRecord datasetRecord_a = lineageRow.getOutputs().get().get(0);
    UpdateLineageRow.DatasetRecord datasetRecord_c = lineageRow.getInputs().get().get(0);

    UUID field_col_a = fieldDao.findUuid(datasetRecord_a.getDatasetRow().getUuid(), "col_a").get();
    UUID field_col_d = fieldDao.findUuid(datasetRecord_c.getDatasetRow().getUuid(), "col_d").get();

    // column lineages for col_a and col_e should be of size 3
    assertThat(dao.getLineage(20, Collections.singletonList(field_col_a), false, Instant.now()))
        .hasSize(3);
    assertThat(dao.getLineage(20, Collections.singletonList(field_col_d), false, Instant.now()))
        .hasSize(3);
  }

  /**
   * Run two jobs that write to dataset_b using dataset_a and dataset_c. Both input fields should be
   * returned
   */
  @Test
  void testGetLineageWhenTwoJobsWriteToSameDataset() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();
    Dataset dataset_C = getDatasetC();
    Dataset dataset_B_another_job =
        new Dataset(
            "namespace",
            "dataset_b",
            LineageEvent.DatasetFacets.builder()
                .schema(
                    new LineageEvent.SchemaDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        Arrays.asList(new LineageEvent.SchemaField("col_c", "STRING", ""))))
                .columnLineage(
                    new LineageEvent.ColumnLineageDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        new LineageEvent.ColumnLineageDatasetFacetFields(
                            Collections.singletonMap(
                                "col_c",
                                new LineageEvent.ColumnLineageOutputColumn(
                                    Arrays.asList(
                                        new LineageEvent.ColumnLineageInputField(
                                            "namespace", "dataset_c", "col_d")),
                                    "description1",
                                    "type1")))))
                .build());

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job1",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_C),
            Arrays.asList(dataset_B_another_job));

    UpdateLineageRow.DatasetRecord datasetRecord_b = lineageRow.getOutputs().get().get(0);
    UUID field_col_c = fieldDao.findUuid(datasetRecord_b.getDatasetRow().getUuid(), "col_c").get();

    // assert input fields for col_d contain col_a and col_c
    List<String> inputFields =
        dao.getLineage(20, Collections.singletonList(field_col_c), false, Instant.now()).stream()
            .filter(node -> node.getDataset().equals("dataset_b"))
            .flatMap(node -> node.getInputFields().stream())
            .map(input -> input.getField())
            .collect(Collectors.toList());

    assertThat(inputFields).hasSize(3).contains("col_a", "col_b", "col_d");
  }

  @Test
  void testGetLineagePointInTime() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job1",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_A),
            Arrays.asList(dataset_B));

    UpdateLineageRow.DatasetRecord datasetRecord_b = lineageRow.getOutputs().get().get(0);
    UUID field_col_b = fieldDao.findUuid(datasetRecord_b.getDatasetRow().getUuid(), "col_c").get();
    Instant columnLineageCreatedAt =
        dao.findColumnLineageByDatasetVersionColumnAndOutputDatasetField(
                datasetRecord_b.getDatasetVersionRow().getUuid(), field_col_b)
            .get(0)
            .getCreatedAt();

    // assert lineage is empty before and present after
    assertThat(
            dao.getLineage(
                20,
                Collections.singletonList(field_col_b),
                false,
                columnLineageCreatedAt.minusSeconds(1)))
        .isEmpty();
    assertThat(
            dao.getLineage(
                20,
                Collections.singletonList(field_col_b),
                false,
                columnLineageCreatedAt.plusSeconds(1)))
        .hasSize(1);
  }

  @Test
  void testGetLineageWhenJobRunMultipleTimes() {
    Dataset dataset_A = getDatasetA();
    Dataset dataset_B = getDatasetB();

    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job1",
        "COMPLETE",
        jobFacet,
        Arrays.asList(dataset_A),
        Arrays.asList(dataset_B));
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job1",
            "COMPLETE",
            jobFacet,
            Arrays.asList(dataset_A),
            Arrays.asList(dataset_B));

    UpdateLineageRow.DatasetRecord datasetRecord_b = lineageRow.getOutputs().get().get(0);
    UUID field_col_b = fieldDao.findUuid(datasetRecord_b.getDatasetRow().getUuid(), "col_c").get();

    assertThat(dao.getLineage(20, Collections.singletonList(field_col_b), false, Instant.now()))
        .hasSize(1);
  }
}
