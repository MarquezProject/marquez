package marquez.db;

import static marquez.db.LineageTestUtils.OPEN_LINEAGE;
import static org.assertj.core.api.Assertions.assertThat;

import io.openlineage.client.OpenLineage;
import java.util.Arrays;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
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
  private final OpenLineage.DatasetFacets datasetFacets =
      LineageTestUtils.newDatasetFacet(
          OPEN_LINEAGE.newSchemaDatasetFacetFields("name", "STRING", "my name"),
          OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "INT", "my age"));
  private final OpenLineage.JobFacets jobFacet = OPEN_LINEAGE.newJobFacetsBuilder().build();

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(OpenLineageDao.class);
  }

  /** When reading a dataset, the version is assumed to be the version last written */
  @Test
  void testUpdateMarquezModel() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(
                OPEN_LINEAGE.newOutputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)));

    UpdateLineageRow readJob =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)),
            Arrays.asList());

    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob.getOutputs().get().get(0).getDatasetVersionRow());
  }

  /**
   * When reading a new dataset, a version is created and the dataset's current version is updated
   * immediately.
   */
  @Test
  void testUpdateMarquezModelWithInputOnlyDataset() {
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "RUNNING",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)),
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
    UpdateLineageRow writeJob =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(
                OPEN_LINEAGE.newOutputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)));

    OpenLineage.DatasetFacets overrideFacet =
        OPEN_LINEAGE.newDatasetFacets(
            this.datasetFacets.getDocumentation(),
            OPEN_LINEAGE.newSchemaDatasetFacet(
                Arrays.asList(
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("name", "STRING", "my name"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "INT", "my age"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields(
                        "eyeColor", "STRING", "my eye color"))),
            this.datasetFacets.getDataSource());
    UpdateLineageRow readJob =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, overrideFacet, null)),
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
    UpdateLineageRow writeJob1 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(
                OPEN_LINEAGE.newOutputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)));
    UpdateLineageRow readJob1 =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)),
            Arrays.asList());

    UpdateLineageRow writeJob2 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(
                OPEN_LINEAGE.newOutputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)));
    UpdateLineageRow writeJob3 =
        LineageTestUtils.createLineageRow(
            dao,
            WRITE_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(),
            Arrays.asList(
                OPEN_LINEAGE.newOutputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)));

    UpdateLineageRow readJob2 =
        LineageTestUtils.createLineageRow(
            dao,
            READ_JOB_NAME,
            "COMPLETE",
            jobFacet,
            Arrays.asList(
                OPEN_LINEAGE.newInputDataset(
                    LineageTestUtils.NAMESPACE, DATASET_NAME, datasetFacets, null)),
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
}
