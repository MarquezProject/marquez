/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.NamespaceName;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.DatasetFacets;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import marquez.service.models.Run;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.groups.Tuple;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class OpenLineageDaoTest {

  public static final String WRITE_JOB_NAME = "writeJobName";
  public static final String READ_JOB_NAME = "readJobName";
  public static final String DATASET_NAME = "theDataset";

  public static final String OUTPUT_COLUMN = "output_column";
  public static final String INPUT_NAMESPACE = "input_namespace";
  public static final String INPUT_DATASET = "input_dataset";
  public static final String INPUT_FIELD_NAME = "input_field_name";
  public static final String TRANSFORMATION_TYPE = "transformation_type";
  public static final String TRANSFORMATION_DESCRIPTION = "transformation_description";

  private static OpenLineageDao dao;
  private static DatasetSymlinkDao symlinkDao;
  private static NamespaceDao namespaceDao;
  private static DatasetFieldDao datasetFieldDao;
  private static RunDao runDao;
  private final DatasetFacets datasetFacets =
      LineageTestUtils.newDatasetFacet(
          new SchemaField("name", "STRING", "my name"), new SchemaField("age", "INT", "my age"));

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(OpenLineageDao.class);
    symlinkDao = jdbi.onDemand(DatasetSymlinkDao.class);
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    datasetFieldDao = jdbi.onDemand(DatasetFieldDao.class);
    runDao = jdbi.onDemand(RunDao.class);
  }

  /** When reading a dataset, the version is assumed to be the version last written */
  @Test
  void testUpdateMarquezModel() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));

    UpdateLineageRow readJob =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)),
            Arrays.asList());

    assertThat(writeJob.getJob().getLocation()).isNull();
    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob.getOutputs().get().get(0).getDatasetVersionRow());
  }

  @Test
  void testUpdateMarquezModelLifecycleStateChangeFacet() {
    Dataset dataset =
        new Dataset(
            LineageTestUtils.NAMESPACE,
            DATASET_NAME,
            LineageEvent.DatasetFacets.builder()
                .lifecycleStateChange(
                    new LineageEvent.LifecycleStateChangeFacet(
                        PRODUCER_URL, SCHEMA_URL, "TRUNCATE"))
                .build());

    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao, WRITE_JOB_NAME, "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList(dataset));

    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(writeJob.getOutputs().get().get(0).getDatasetVersionRow().getLifecycleState())
        .isEqualTo("TRUNCATE");
  }

  @Test
  void testUpdateMarquezModelDatasetWithColumnLineageFacet() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(getInputDataset()),
            Arrays.asList(getOutputDatasetWithColumnLineage()));

    UUID inputDatasetVersion = writeJob.getInputs().get().get(0).getDatasetVersionRow().getUuid();
    UUID outputDatasetVersion = writeJob.getOutputs().get().get(0).getDatasetVersionRow().getUuid();

    assertThat(
            writeJob.getOutputs().get().stream().toList().stream()
                .findAny()
                .orElseThrow()
                .getColumnLineageRows())
        .size()
        .isEqualTo(1);

    assertThat(
            writeJob.getOutputs().get().stream().toList().stream()
                .findAny()
                .orElseThrow()
                .getColumnLineageRows())
        .extracting(
            (ds) -> ds.getInputDatasetFieldUuid(),
            (ds) -> ds.getInputDatasetVersionUuid(),
            (ds) -> ds.getOutputDatasetFieldUuid(),
            (ds) -> ds.getOutputDatasetVersionUuid(),
            (ds) -> ds.getTransformationDescription().get(),
            (ds) -> ds.getTransformationType().get())
        .containsExactly(
            Tuple.tuple(
                datasetFieldDao
                    .findUuid(
                        writeJob.getInputs().get().get(0).getDatasetRow().getUuid(),
                        INPUT_FIELD_NAME)
                    .get(),
                inputDatasetVersion,
                datasetFieldDao
                    .findUuid(
                        writeJob.getOutputs().get().get(0).getDatasetRow().getUuid(), OUTPUT_COLUMN)
                    .get(),
                outputDatasetVersion,
                TRANSFORMATION_DESCRIPTION,
                TRANSFORMATION_TYPE));
  }

  @Test
  void testUpdateMarquezModelDatasetWithColumnLineageFacetWhenInputFieldDoesNotExist() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Collections.emptyList(),
            Arrays.asList(getOutputDatasetWithColumnLineage()));

    // make sure no column lineage was written
    assertEquals(0, writeJob.getOutputs().get().get(0).getColumnLineageRows().size());
  }

  @Test
  void testUpdateMarquezModelDatasetWithColumnLineageFacetWhenOutputFieldDoesNotExist() {
    Dataset outputDatasetWithoutOutputFieldSchema =
        new Dataset(
            LineageTestUtils.NAMESPACE,
            DATASET_NAME,
            LineageEvent.DatasetFacets.builder() // schema is missing
                .columnLineage(
                    new LineageEvent.ColumnLineageDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        new LineageEvent.ColumnLineageDatasetFacetFields(
                            Collections.singletonMap(
                                OUTPUT_COLUMN,
                                new LineageEvent.ColumnLineageOutputColumn(
                                    Collections.singletonList(
                                        new LineageEvent.ColumnLineageInputField(
                                            INPUT_NAMESPACE, INPUT_DATASET, INPUT_FIELD_NAME)),
                                    TRANSFORMATION_DESCRIPTION,
                                    TRANSFORMATION_TYPE)))))
                .build());

    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(getInputDataset()),
            Arrays.asList(outputDatasetWithoutOutputFieldSchema));

    // make sure no column lineage was written
    assertEquals(0, writeJob.getOutputs().get().get(0).getColumnLineageRows().size());
  }

  @Test
  /**
   * When trying to insert new column level lineage data, do not create additional row if triad
   * (dataset_version_uuid, output_column_name and input_field) is the same. Upsert instead.
   */
  void testUpsertColumnLineageData() {
    final String UPDATED_TRANSFORMATION_TYPE = "transformation_type";
    final String UPDATED_TRANSFORMATION_DESCRIPTION = "updated_transformation_description";

    Dataset inputDataset = getInputDataset();
    Dataset dataset = getOutputDatasetWithColumnLineage();

    Dataset updateDataset =
        new Dataset(
            LineageTestUtils.NAMESPACE,
            DATASET_NAME,
            LineageEvent.DatasetFacets.builder()
                .schema(
                    new SchemaDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        Arrays.asList(new SchemaField(OUTPUT_COLUMN, "STRING", "my name"))))
                .columnLineage(
                    new LineageEvent.ColumnLineageDatasetFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        new LineageEvent.ColumnLineageDatasetFacetFields(
                            Collections.singletonMap(
                                OUTPUT_COLUMN,
                                new LineageEvent.ColumnLineageOutputColumn(
                                    Collections.singletonList(
                                        new LineageEvent.ColumnLineageInputField(
                                            INPUT_NAMESPACE, INPUT_DATASET, INPUT_FIELD_NAME)),
                                    UPDATED_TRANSFORMATION_DESCRIPTION,
                                    UPDATED_TRANSFORMATION_TYPE)))))
                .build());

    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob1 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(inputDataset),
            Arrays.asList(dataset));

    UpdateLineageRow writeJob2 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(inputDataset),
            Arrays.asList(updateDataset));

    // try to read with same inputs as writeJob1 and check if size=1
    UpdateLineageRow readJob2 =
        LineageTestUtils.createLineageRow(
            dao, WRITE_JOB_NAME, "COMPLETE", jobFacet, Arrays.asList(dataset), Arrays.asList());

    // only 1 row should be present (no multiple Optional<DatasetVersionRow> candidates)
    assertThat(readJob2.getInputs()).isPresent().get().asList().size().isEqualTo(1);

    // finally, test if upsert was successful
    assertThat(readJob2.getInputs().get().get(0).getDatasetVersionRow())
        .isNotEqualTo(writeJob1.getOutputs().get().get(0).getDatasetVersionRow());

    assertThat(readJob2.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob2.getOutputs().get().get(0).getDatasetVersionRow());
  }

  @Test
  void testUpdateMarquezModelDatasetWithSymlinks() {
    Dataset dataset =
        new Dataset(
            LineageTestUtils.NAMESPACE,
            DATASET_NAME,
            LineageEvent.DatasetFacets.builder()
                .symlinks(
                    new LineageEvent.DatasetSymlinkFacet(
                        PRODUCER_URL,
                        SCHEMA_URL,
                        Collections.singletonList(
                            new LineageEvent.SymlinkIdentifier(
                                "symlinkNamespace", "symlinkName", "some-type"))))
                .build());

    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao, WRITE_JOB_NAME, "COMPLETE", jobFacet, Arrays.asList(), Arrays.asList(dataset));

    UpdateLineageRow readJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                new Dataset(
                    "symlinkNamespace",
                    "symlinkName",
                    LineageEvent.DatasetFacets.builder().build())),
            Arrays.asList());

    // make sure writeJob output dataset and readJob input dataset are the same (have the same uuid)
    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(writeJob.getOutputs().get().get(0).getDatasetRow().getUuid())
        .isEqualTo(readJob.getInputs().get().get(0).getDatasetRow().getUuid());
    // make sure symlink is stored with type in dataset_symlinks table
    assertThat(
            symlinkDao
                .findDatasetSymlinkByNamespaceUuidAndName(
                    namespaceDao.findNamespaceByName("symlinkNamespace").get().getUuid(),
                    "symlinkName")
                .get()
                .getType()
                .get())
        .isEqualTo("some-type");
  }

  /**
   * When reading a new dataset, a version is created and the dataset's current version is updated
   * immediately.
   */
  @Test
  void testUpdateMarquezModelWithInputOnlyDataset() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "RUNNING",
            jobFacet,
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)),
            Arrays.asList());

    assertThat(writeJob.getInputs())
        .isPresent()
        .get(InstanceOfAssertFactories.list(DatasetRecord.class))
        .hasSize(1)
        .first()
        .matches(v -> v.getDatasetRow().getCurrentVersionUuid().isPresent());
  }

  /**
   * When reading a dataset, even when reporting a schema that differs from the prior written
   * schema, the dataset version doesn't change.
   */
  @Test
  void testUpdateMarquezModelWithNonMatchingReadSchema() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));

    DatasetFacets overrideFacet =
        new DatasetFacets(
            this.datasetFacets.getDocumentation(),
            new SchemaDatasetFacet(
                LineageTestUtils.PRODUCER_URL,
                LineageTestUtils.SCHEMA_URL,
                Arrays.asList(
                    new SchemaField("name", "STRING", "my name"),
                    new SchemaField("age", "INT", "my age"),
                    new SchemaField("eyeColor", "STRING", "my eye color"))),
            this.datasetFacets.getLifecycleStateChange(),
            this.datasetFacets.getDataSource(),
            this.datasetFacets.getColumnLineage(),
            null,
            this.datasetFacets.getDescription(),
            this.datasetFacets.getAdditionalFacets());
    UpdateLineageRow readJob =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, overrideFacet)),
            Arrays.asList());

    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob.getOutputs().get().get(0).getDatasetVersionRow());
  }

  /**
   * When a dataset is written, its version changes. When read the version is assumed to be the last
   * version written.
   */
  @Test
  void testUpdateMarquezModelWithPriorWrites() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob1 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));
    UpdateLineageRow readJob1 =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)),
            Arrays.asList());

    UpdateLineageRow writeJob2 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));
    UpdateLineageRow writeJob3 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));

    UpdateLineageRow readJob2 =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)),
            Arrays.asList());

    // verify readJob1 read the version written by writeJob1
    assertThat(writeJob1.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob1.getInputs()).isPresent().get().asList().size().isEqualTo(1);

    assertThat(readJob1.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob1.getOutputs().get().get(0).getDatasetVersionRow());

    // verify that writeJob2 and writeJob3 wrote different versions from writeJob1
    assertThat(writeJob2.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(writeJob3.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(writeJob1.getOutputs())
        .get()
        .extracting((ds) -> ds.get(0).getDatasetVersionRow().getUuid())
        .isNotEqualTo(writeJob2.getOutputs().get().get(0).getDatasetVersionRow().getUuid())
        .isNotEqualTo(writeJob3.getOutputs().get().get(0).getDatasetVersionRow().getUuid());
    assertThat(writeJob2.getOutputs())
        .get()
        .extracting((ds) -> ds.get(0).getDatasetVersionRow().getUuid())
        .isNotEqualTo(writeJob3.getOutputs().get().get(0).getDatasetVersionRow().getUuid());

    // verify that readJob2 read the version produced by writeJob3
    assertThat(readJob2.getInputs()).isPresent().get().asList().size().isEqualTo(1);

    assertThat(readJob2.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob3.getOutputs().get().get(0).getDatasetVersionRow());
  }

  @Test
  void testGetOpenLineageEvents() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets)));

    List<LineageEvent> lineageEvents = dao.findLineageEventsByRunUuid(writeJob.getRun().getUuid());
    assertThat(lineageEvents).hasSize(1);

    assertThat(lineageEvents.get(0).getEventType()).isEqualTo("COMPLETE");

    LineageEvent.Job job = lineageEvents.get(0).getJob();
    assertThat(job)
        .extracting("namespace", "name")
        .contains(LineageTestUtils.NAMESPACE, WRITE_JOB_NAME);
  }

  @Test
  void testInputOutputDatasetFacets() {
    JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                new Dataset(
                    "namespace",
                    "dataset_input",
                    null,
                    LineageEvent.InputDatasetFacets.builder()
                        .additional(
                            ImmutableMap.of(
                                "inputFacet1", "{some-facet1}",
                                "inputFacet2", "{some-facet2}"))
                        .build(),
                    null)),
            Arrays.asList(
                new Dataset(
                    "namespace",
                    "dataset_output",
                    null,
                    null,
                    LineageEvent.OutputDatasetFacets.builder()
                        .additional(
                            ImmutableMap.of(
                                "outputFacet1", "{some-facet1}",
                                "outputFacet2", "{some-facet2}"))
                        .build())));

    Run run = runDao.findRunByUuid(lineageRow.getRun().getUuid()).get();

    assertThat(run.getInputDatasetVersions()).hasSize(1);
    assertThat(run.getInputDatasetVersions().get(0).getDatasetVersionId())
        .isEqualTo(
            new DatasetVersionId(
                NamespaceName.of("namespace"),
                DatasetName.of("dataset_input"),
                lineageRow.getInputs().get().get(0).getDatasetVersionRow().getVersion()));
    assertThat(run.getInputDatasetVersions().get(0).getFacets())
        .containsAllEntriesOf(
            ImmutableMap.of(
                "inputFacet1", "{some-facet1}",
                "inputFacet2", "{some-facet2}"));

    assertThat(run.getOutputDatasetVersions()).hasSize(1);
    assertThat(run.getOutputDatasetVersions().get(0).getDatasetVersionId())
        .isEqualTo(
            new DatasetVersionId(
                NamespaceName.of("namespace"),
                DatasetName.of("dataset_output"),
                lineageRow.getOutputs().get().get(0).getDatasetVersionRow().getVersion()));
    assertThat(run.getOutputDatasetVersions().get(0).getFacets())
        .containsAllEntriesOf(
            ImmutableMap.of(
                "outputFacet1", "{some-facet1}",
                "outputFacet2", "{some-facet2}"));
  }

  private Dataset getInputDataset() {
    return new Dataset(
        INPUT_NAMESPACE,
        INPUT_DATASET,
        LineageEvent.DatasetFacets.builder()
            .schema(
                new SchemaDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    Arrays.asList(new SchemaField(INPUT_FIELD_NAME, "STRING", "my name"))))
            .build());
  }

  private Dataset getOutputDatasetWithColumnLineage() {
    return new Dataset(
        LineageTestUtils.NAMESPACE,
        DATASET_NAME,
        LineageEvent.DatasetFacets.builder()
            .schema(
                new SchemaDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    Arrays.asList(new SchemaField(OUTPUT_COLUMN, "STRING", "my name"))))
            .columnLineage(
                new LineageEvent.ColumnLineageDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    new LineageEvent.ColumnLineageDatasetFacetFields(
                        Collections.singletonMap(
                            OUTPUT_COLUMN,
                            new LineageEvent.ColumnLineageOutputColumn(
                                Collections.singletonList(
                                    new LineageEvent.ColumnLineageInputField(
                                        INPUT_NAMESPACE, INPUT_DATASET, INPUT_FIELD_NAME)),
                                TRANSFORMATION_DESCRIPTION,
                                TRANSFORMATION_TYPE)))))
            .build());
  }
}
