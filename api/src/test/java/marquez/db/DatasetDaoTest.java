/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static marquez.db.LineageTestUtils.createLineageRow;
import static marquez.db.LineageTestUtils.newDatasetFacet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.SchemaField;
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

  private final JobFacet jobFacet = new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP);

  static Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    DatasetDaoTest.jdbi = jdbi;
    datasetDao = jdbi.onDemand(DatasetDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  private Dataset newCommonDataset(Map<String, Object> facets) {
    return new Dataset(
        NAMESPACE,
        DATASET,
        newDatasetFacet(
            facets,
            new SchemaField("firstname", "string", "the first name"),
            new SchemaField("lastname", "string", "the last name")));
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
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));

    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(ImmutableMap.of("inputFacet", new CustomValueFacet("aFacetValue")))),
        Collections.emptyList());
    createLineageRow(
        openLineageDao,
        "aSecondReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
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
        .hasSize(7)
        .containsKeys(
            "documentation",
            "schema",
            "dataSource",
            "description",
            "writeFacet",
            "inputFacet",
            "anotherInputFacet");
  }

  @Test
  public void testGetDatasetWithlifecycleStatePresent() {
    Dataset dataset =
        new Dataset(
            NAMESPACE,
            DATASET,
            LineageEvent.DatasetFacets.builder()
                .lifecycleStateChange(
                    new LineageEvent.LifecycleStateChangeFacet(PRODUCER_URL, SCHEMA_URL, "CREATE"))
                .build());

    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(dataset));

    Optional<marquez.service.models.Dataset> datasetByName =
        datasetDao.findDatasetByName(NAMESPACE, DATASET);
    assertThat(datasetByName.get().getLastLifecycleState().get()).isEqualTo("CREATE");
  }

  @Test
  public void testGetDatasetWithDatasetMarkedDeleted() {
    // create dataset
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            new Dataset(NAMESPACE, DATASET, LineageEvent.DatasetFacets.builder().build())));

    // mark it deleted
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            new Dataset(
                NAMESPACE,
                DATASET,
                LineageEvent.DatasetFacets.builder()
                    .lifecycleStateChange(
                        new LineageEvent.LifecycleStateChangeFacet(
                            PRODUCER_URL, SCHEMA_URL, "DROP"))
                    .build())));

    // make sure it's returned by DAO and marked as deleted
    assertThat(datasetDao.findDatasetByName(NAMESPACE, DATASET).get().isDeleted()).isTrue();
    assertThat(datasetDao.findWithTags(NAMESPACE, DATASET).get().isDeleted()).isTrue();
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
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue")))),
        Collections.emptyList());

    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")))),
        Collections.emptyList());
    createLineageRow(
        openLineageDao,
        "aSecondReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
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
        .hasSize(6)
        .containsKeys(
            "documentation", "description", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "thirdReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));
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
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));

    String secondDatasetName = "secondDataset";
    createLineageRow(
        openLineageDao,
        "secondWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            new Dataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")),
                    new SchemaField("age", "int", "the age"),
                    new SchemaField("address", "string", "the address")))));

    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Arrays.asList(
            newCommonDataset(ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue"))),
            new Dataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")),
                    new SchemaField("age", "int", "the age"),
                    new SchemaField("address", "string", "the address")))),
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
        .hasSize(6)
        .containsKeys(
            "documentation", "description", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "firstWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "firstReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));

    assertThat(datasets.get(1))
        .matches(ds -> ds.getName().getValue().equals(secondDatasetName))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(6)
        .containsKeys(
            "documentation", "description", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "secondReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));
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
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("firstWriteValue")))));
    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("firstReadValue")))),
        Collections.singletonList(
            new Dataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", "readJobFirstWriteValue"),
                    new SchemaField("age", "int", "the age"),
                    new SchemaField("address", "string", "the address")))));

    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("secondWriteValue")))));

    createLineageRow(
        openLineageDao,
        "aReadJob",
        "COMPLETE",
        jobFacet,
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("inputFacet", new CustomValueFacet("secondReadValue")))),
        Collections.singletonList(
            new Dataset(
                NAMESPACE,
                secondDatasetName,
                newDatasetFacet(
                    ImmutableMap.of("writeFacet", new CustomValueFacet("readJobSecondWriteValue")),
                    new SchemaField("age", "int", "the age"),
                    new SchemaField("address", "string", "the address")))));

    List<marquez.service.models.Dataset> datasets = datasetDao.findAll(NAMESPACE, 5, 0);
    assertThat(datasets).hasSize(2);

    // datasets sorted alphabetically, so commonDataset is first
    assertThat(datasets.get(0))
        .matches(ds -> ds.getName().getValue().equals(DATASET))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(6)
        .containsKeys(
            "documentation", "description", "schema", "dataSource", "writeFacet", "inputFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "secondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"))
        .containsEntry(
            "inputFacet",
            ImmutableMap.of(
                "value",
                "secondReadValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));

    assertThat(datasets.get(1))
        .matches(ds -> ds.getName().getValue().equals(secondDatasetName))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "description", "schema", "dataSource", "writeFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "readJobSecondWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));

    // write a third version of the writeJob
    // since there is no read of this version, all input facets will be missing from the response
    createLineageRow(
        openLineageDao,
        "aWriteJob",
        "COMPLETE",
        jobFacet,
        Collections.emptyList(),
        Collections.singletonList(
            newCommonDataset(
                ImmutableMap.of("writeFacet", new CustomValueFacet("thirdWriteValue")))));

    datasets = datasetDao.findAll(NAMESPACE, 5, 0);
    assertThat(datasets).hasSize(2);

    assertThat(datasets.get(0))
        .matches(ds -> ds.getName().getValue().equals(DATASET))
        .extracting(
            marquez.service.models.Dataset::getFacets,
            InstanceOfAssertFactories.map(String.class, Object.class))
        .isNotEmpty()
        .hasSize(5)
        .containsKeys("documentation", "description", "schema", "dataSource", "writeFacet")
        .containsEntry(
            "writeFacet",
            ImmutableMap.of(
                "value",
                "thirdWriteValue",
                "_producer",
                "http://test.producer/",
                "_schemaURL",
                "http://test.schema/"));
  }

  @Getter
  public static class CustomValueFacet implements OpenLineage.BaseFacet {
    private String value;

    public CustomValueFacet(String value) {
      this.value = value;
    }

    @Override
    public URI get_producer() {
      return PRODUCER_URL;
    }

    @Override
    public URI get_schemaURL() {
      return SCHEMA_URL;
    }

    @Override
    public Map<String, Object> getAdditionalProperties() {
      return null;
    }
  }
}
