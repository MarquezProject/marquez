package marquez.db;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.OPEN_LINEAGE;
import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.createLineageRow;
import static marquez.db.LineageTestUtils.newDatasetFacet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.openlineage.client.OpenLineage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class DatasetDaoTest {

  public static final String DATASET = "commonDataset";
  private static DatasetDao datasetDao;
  private static OpenLineageDao openLineageDao;

  private final OpenLineage.JobFacets jobFacet = OPEN_LINEAGE.newJobFacets(null, null, null);

  static Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    DatasetDaoTest.jdbi = jdbi;
    datasetDao = jdbi.onDemand(DatasetDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  private OpenLineage.OutputDataset newCommonOutputDataset(
      Map<String, OpenLineage.CustomFacet> facets) {
    return OPEN_LINEAGE.newOutputDataset(
        NAMESPACE,
        DATASET,
        newDatasetFacet(
            facets,
            OPEN_LINEAGE.newSchemaDatasetFacetFields("firstname", "string", "the first name"),
            OPEN_LINEAGE.newSchemaDatasetFacetFields("lastname", "string", "the last name")),
        null);
  }

  private OpenLineage.InputDataset newCommonInputDataset(
      Map<String, OpenLineage.CustomFacet> facets) {
    return OPEN_LINEAGE.newInputDataset(
        NAMESPACE,
        DATASET,
        newDatasetFacet(
            facets,
            OPEN_LINEAGE.newSchemaDatasetFacetFields("firstname", "string", "the first name"),
            OPEN_LINEAGE.newSchemaDatasetFacetFields("lastname", "string", "the last name")),
        null);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM dataset_versions_field_mapping");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
          handle.execute("DELETE FROM run_states");
          handle.execute("DELETE FROM runs");
          handle.execute("DELETE FROM run_args");
          handle.execute("DELETE FROM job_versions_io_mapping");
          handle.execute("DELETE FROM job_versions");
          handle.execute("DELETE FROM jobs");
          handle.execute("DELETE FROM dataset_fields_tag_mapping");
          handle.execute("DELETE FROM dataset_fields");
          handle.execute("DELETE FROM datasets");
          handle.execute("DELETE FROM sources");
          handle.execute("DELETE FROM namespaces");
          return null;
        });
  }

  @Test
  public void testGetDataset() {
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("aFacetValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("aFacetValue")))),
        Collections.emptyList());
    createLineageRow(
        openLineageDao,
        "aSecondReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("anotherInputFacet", new CustomValueFacet("aFacetValue")))),
        Collections.emptyList());

    Optional<marquez.service.models.Dataset> datasetByName =
        datasetDao.findDatasetByName(NAMESPACE, DATASET);
    assertThat(datasetByName)
        .isPresent()
        .get()
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(6)
        .containsKeys(
            "documentation",
            "schema",
            "dataSource",
            "writeFacet",
            "inputFacet",
            "anotherInputFacet");
  }

  @Test
  public void testGetDatasetWithMultipleVersions() {
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue")))),
        Collections.emptyList());

    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")))),
        Collections.emptyList());
    createLineageRow(
        openLineageDao,
        "aSecondReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("thirdReadValue")))),
        Collections.emptyList());

    Optional<marquez.service.models.Dataset> datasetByName =
        datasetDao.findDatasetByName(NAMESPACE, DATASET);
    assertThat(datasetByName)
        .isPresent()
        .get()
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "thirdReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));
  }

  @Test
  public void testGetDatasets() {
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));

    String secondDatasetName = "secondDataset";
    createLineageRow(
        openLineageDao,
        "secondWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            OPEN_LINEAGE.newOutputDataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "int", "the age"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("address", "string", "the address")),
                null)));

    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue"))),
            OPEN_LINEAGE.newInputDataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "int", "the age"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("address", "string", "the address")),
                null)),
        Collections.emptyList());

    List<marquez.service.models.Dataset> datasets = datasetDao.findAll(NAMESPACE, 5, 0);
    assertThat(datasets).hasSize(2);

    // datasets sorted alphabetically, so commonDataset is first
    assertThat(datasets.get(0))
        .matches(ds -> ds.getName().getValue().equals(DATASET))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "firstWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "firstReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));

    assertThat(datasets.get(1))
        .matches(ds -> ds.getName().getValue().equals(secondDatasetName))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "secondReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));
  }

  @Test
  public void testGetDatasetsWithMultipleVersions() {
    String secondDatasetName = "secondDataset";
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue")))),
        Collections.singletonList(
            OPEN_LINEAGE.newOutputDataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", new CustomValueFacet("readJobFirstWriteValue")),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "int", "the age"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("address", "string", "the address")),
                null)));
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")))));

    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonInputDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")))),
        Collections.singletonList(
            OPEN_LINEAGE.newOutputDataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", new CustomValueFacet("readJobSecondWriteValue")),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("age", "int", "the age"),
                    OPEN_LINEAGE.newSchemaDatasetFacetFields("address", "string", "the address")),
                null)));

    List<marquez.service.models.Dataset> datasets = datasetDao.findAll(NAMESPACE, 5, 0);
    assertThat(datasets).hasSize(2);

    // datasets sorted alphabetically, so commonDataset is first
    assertThat(datasets.get(0))
        .matches(ds -> ds.getName().getValue().equals(DATASET))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "secondReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));

    assertThat(datasets.get(1))
        .matches(ds -> ds.getName().getValue().equals(secondDatasetName))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(4)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "readJobSecondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));

    // write a third version of the writeJob
    // since there is no read of this version, all input facets will be missing from the response
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonOutputDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("thirdWriteValue")))));

    datasets = datasetDao.findAll(NAMESPACE, 5, 0);
    assertThat(datasets).hasSize(2);

    assertThat(datasets.get(0))
        .matches(ds -> ds.getName().getValue().equals(DATASET))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(4)
        .containsKeys("documentation", "schema", "dataSource", "writeFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "thirdWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/CustomFacet"));
  }

  @Getter
  public static class CustomValueFacet extends OpenLineage.CustomFacet {
    private String value;

    public CustomValueFacet(String value) {
      super(PRODUCER_URL);
      this.value = value;
    }
  }
}
