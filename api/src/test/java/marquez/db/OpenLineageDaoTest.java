package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.common.Utils;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.DatasetFacet;
import marquez.service.models.LineageEvent.DatasourceDatasetFacet;
import marquez.service.models.LineageEvent.DocumentationDatasetFacet;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.NominalTimeRunFacet;
import marquez.service.models.LineageEvent.Run;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class OpenLineageDaoTest {

  public static final ZoneId LOCAL_ZONE = ZoneId.of("America/Los_Angeles");
  public static final ImmutableMap<String, Object> EMPTY_MAP = ImmutableMap.of();
  public static final URI PRODUCER_URL = URI.create("http://test.producer/");
  public static final URI SCHEMA_URL = URI.create("http://test.schema/");
  public static final String NAMESPACE = "namespace";
  public static final String WRITE_JOB_NAME = "writeJobName";
  public static final String READ_JOB_NAME = "readJobName";
  public static final String DATASET_NAME = "theDataset";

  private static OpenLineageDao dao;
  private final DatasetFacet datasetFacet =
      new DatasetFacet(
          new DocumentationDatasetFacet(PRODUCER_URL, SCHEMA_URL, "the dataset documentation"),
          new SchemaDatasetFacet(
              PRODUCER_URL,
              SCHEMA_URL,
              Arrays.asList(
                  new SchemaField("name", "STRING", "my name"),
                  new SchemaField("age", "INT", "my age"))),
          new DatasourceDatasetFacet(
              PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"),
          "the dataset description",
          EMPTY_MAP);

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    dao = jdbi.onDemand(OpenLineageDao.class);
  }

  /** When reading a dataset, the version is assumed to be the version last written */
  @Test
  void testUpdateMarquezModel() {
    JobFacet jobFacet = new JobFacet(null, null, null, EMPTY_MAP);
    UpdateLineageRow writeJob =
        createLineageRow(
            WRITE_JOB_NAME,
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)));

    UpdateLineageRow readJob =
        createLineageRow(
            READ_JOB_NAME,
            jobFacet,
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)),
            Arrays.asList());

    assertThat(writeJob.getOutputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs()).isPresent().get().asList().size().isEqualTo(1);
    assertThat(readJob.getInputs().get().get(0).getDatasetVersionRow())
        .isEqualTo(writeJob.getOutputs().get().get(0).getDatasetVersionRow());
  }

  /**
   * When reading a dataset, even when reporting a schema that differs from the prior written
   * schema, the dataset version doesn't change.
   */
  @Test
  void testUpdateMarquezModelWithNonMatchingReadSchema() {
    JobFacet jobFacet = new JobFacet(null, null, null, EMPTY_MAP);
    UpdateLineageRow writeJob =
        createLineageRow(
            WRITE_JOB_NAME,
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)));

    DatasetFacet overrideFacet =
        new DatasetFacet(
            this.datasetFacet.getDocumentation(),
            new SchemaDatasetFacet(
                PRODUCER_URL,
                SCHEMA_URL,
                Arrays.asList(
                    new SchemaField("name", "STRING", "my name"),
                    new SchemaField("age", "INT", "my age"),
                    new SchemaField("eyeColor", "STRING", "my eye color"))),
            this.datasetFacet.getDataSource(),
            this.datasetFacet.getDescription(),
            this.datasetFacet.getAdditionalFacets());
    UpdateLineageRow readJob =
        createLineageRow(
            READ_JOB_NAME,
            jobFacet,
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, overrideFacet)),
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
    JobFacet jobFacet = new JobFacet(null, null, null, EMPTY_MAP);
    UpdateLineageRow writeJob1 =
        createLineageRow(
            WRITE_JOB_NAME,
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)));
    UpdateLineageRow readJob1 =
        createLineageRow(
            READ_JOB_NAME,
            jobFacet,
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)),
            Arrays.asList());

    UpdateLineageRow writeJob2 =
        createLineageRow(
            WRITE_JOB_NAME,
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)));
    UpdateLineageRow writeJob3 =
        createLineageRow(
            WRITE_JOB_NAME,
            jobFacet,
            Arrays.asList(),
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)));

    UpdateLineageRow readJob2 =
        createLineageRow(
            READ_JOB_NAME,
            jobFacet,
            Arrays.asList(new Dataset(NAMESPACE, DATASET_NAME, datasetFacet)),
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

  private UpdateLineageRow createLineageRow(
      String jobName, JobFacet jobFacet, List<Dataset> inputs, List<Dataset> outputs) {
    NominalTimeRunFacet nominalTimeRunFacet = new NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));

    return dao.updateMarquezModel(
        new LineageEvent(
            "COMPLETE",
            Instant.now().atZone(LOCAL_ZONE),
            new Run(
                UUID.randomUUID().toString(),
                new RunFacet(nominalTimeRunFacet, null, ImmutableMap.of())),
            new Job(NAMESPACE, jobName, jobFacet),
            inputs, // no input
            outputs,
            PRODUCER_URL.toString()),
        Utils.getMapper());
  }
}
