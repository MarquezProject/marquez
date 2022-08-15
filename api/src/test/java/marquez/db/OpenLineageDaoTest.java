/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.DatasetFacets;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class OpenLineageDaoTest {

  public static final String WRITE_JOB_NAME = "writeJobName";
  public static final String READ_JOB_NAME = "readJobName";
  public static final String DATASET_NAME = "theDataset";

  private static OpenLineageDao dao;
  private final DatasetFacets datasetFacets =
      LineageTestUtils.newDatasetFacet(
          new SchemaField("name", "STRING", "my name"), new SchemaField("age", "INT", "my age"));

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(OpenLineageDao.class);
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

    List<LineageEvent> lineageEvents = dao.findOlEventsByRunUuid(writeJob.getRun().getUuid());
    assertThat(lineageEvents).hasSize(1);

    assertThat(lineageEvents.get(0).getEventType()).isEqualTo("COMPLETE");

    LineageEvent.Job job = lineageEvents.get(0).getJob();
    assertThat(job)
        .extracting("namespace", "name")
        .contains(LineageTestUtils.NAMESPACE, WRITE_JOB_NAME);
  }
}
