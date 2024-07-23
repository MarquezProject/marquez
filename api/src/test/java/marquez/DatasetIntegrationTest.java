/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static marquez.db.ColumnLineageTestUtils.getDatasetA;
import static marquez.db.ColumnLineageTestUtils.getDatasetB;
import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import marquez.api.JdbiUtils;
import marquez.client.models.ColumnLineage;
import marquez.client.models.Dataset;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetVersion;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Namespace;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.StreamVersion;
import marquez.common.Utils;
import marquez.db.LineageTestUtils;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@org.junit.jupiter.api.Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class DatasetIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    createSource(STREAM_SOURCE_NAME);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testApp_testTags() {
    DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(ImmutableList.of(newFieldWith(SENSITIVE.getName()), newField()))
            .tags(ImmutableSet.of(PII.getName()))
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset dataset = client.createDataset(NAMESPACE_NAME, "test-dataset-tags", DB_TABLE_META);
    assertThat(dataset.getFields().get(0).getTags())
        .isEqualTo(ImmutableSet.of(SENSITIVE.getName()));
    assertThat(dataset.getFields().get(1).getTags()).isEmpty();
    assertThat(dataset.getTags()).isEqualTo(ImmutableSet.of(PII.getName()));

    DbTableMeta UPDATED_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(
                ImmutableList.of(
                    newFieldWith(PII.getName()),
                    DB_TABLE_META.getFields().get(0))) // changed fields
            .tags(ImmutableSet.of(SENSITIVE.getName())) // added dataset tag
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset updateDataset = client.createDataset(NAMESPACE_NAME, "test-dataset-tags", UPDATED_META);
    assertThat(updateDataset.getTags())
        .isEqualTo(ImmutableSet.of(SENSITIVE.getName(), PII.getName()));
    assertThat(updateDataset.getFields()).isEqualTo(UPDATED_META.getFields());

    Dataset getDataset = client.getDataset(NAMESPACE_NAME, "test-dataset-tags");
    assertThat(getDataset.getFields()).isEqualTo(UPDATED_META.getFields());
    assertThat(getDataset.getTags()).isEqualTo(ImmutableSet.of(SENSITIVE.getName(), PII.getName()));
  }

  @Test
  public void testApp_getTableVersions() {
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);

    ImmutableMap<String, Object> outputFacets =
        ImmutableMap.of("outputFacetKey", "outputFacetValue");
    ImmutableMap<String, Object> inputFacets = ImmutableMap.of("inputFacetKey", "inputFacetValue");

    final LineageEvent.DatasetFacets datasetFacets =
        LineageTestUtils.newDatasetFacet(
            outputFacets,
            LineageEvent.SchemaField.builder()
                .name("firstname")
                .type("string")
                .description("the first name")
                .build());
    datasetFacets
        .getDocumentation()
        .setDescription(DB_TABLE_META.getDescription().orElse("the dataset documentation"));

    final LineageEvent lineageEvent =
        LineageEvent.builder()
            .producer("testApp_getTableVersions")
            .eventType("COMPLETE")
            .run(
                new LineageEvent.Run(
                    UUID.randomUUID().toString(), LineageEvent.RunFacet.builder().build()))
            .job(LineageEvent.Job.builder().namespace(NAMESPACE_NAME).name(JOB_NAME).build())
            .eventTime(ZonedDateTime.now())
            .inputs(Collections.emptyList())
            .outputs(
                Collections.singletonList(
                    LineageEvent.Dataset.builder()
                        .namespace(NAMESPACE_NAME)
                        .name(DB_TABLE_NAME)
                        .facets(datasetFacets)
                        .build()))
            .build();

    final CompletableFuture<Integer> resp = sendEvent(lineageEvent);
    assertThat(resp.join()).isEqualTo(201);

    datasetFacets.setAdditional(inputFacets);
    final LineageEvent readEvent =
        LineageEvent.builder()
            .producer("testApp_getTableVersions")
            .eventType("COMPLETE")
            .run(
                new LineageEvent.Run(
                    UUID.randomUUID().toString(), LineageEvent.RunFacet.builder().build()))
            .job(LineageEvent.Job.builder().namespace(NAMESPACE_NAME).name("aReadOnlyJob").build())
            .eventTime(ZonedDateTime.now())
            .inputs(
                Collections.singletonList(
                    LineageEvent.Dataset.builder()
                        .namespace(NAMESPACE_NAME)
                        .name(DB_TABLE_NAME)
                        .facets(datasetFacets)
                        .build()))
            .outputs(Collections.emptyList())
            .build();

    final CompletableFuture<Integer> readResp = sendEvent(readEvent);
    assertThat(readResp.join()).isEqualTo(201);

    // update dataset facet to include input and output facets
    // save the expected facets as a map for comparison
    datasetFacets.setAdditional(
        ImmutableMap.<String, Object>builder().putAll(inputFacets).putAll(outputFacets).build());
    Map<String, Object> expectedFacetsMap =
        Utils.getMapper().convertValue(datasetFacets, new TypeReference<Map<String, Object>>() {});

    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, DB_TABLE_NAME);
    assertThat(versions).hasSizeGreaterThanOrEqualTo(2);
    versions.forEach(
        datasetVersion -> {
          assertThat(datasetVersion.getId())
              .isEqualTo(new DatasetId(NAMESPACE_NAME, DB_TABLE_NAME));
          assertThat(datasetVersion.getName()).isEqualTo(DB_TABLE_NAME);
          assertThat(datasetVersion.getCreatedAt()).isNotNull();
          assertThat(datasetVersion.getNamespace()).isEqualTo(NAMESPACE_NAME);
          assertThat(datasetVersion.getVersion()).isNotNull();
          assertThat(datasetVersion.getDescription()).isEqualTo(DB_TABLE_META.getDescription());
        });
    assertThat(versions.get(0).getFacets()).isEqualTo(expectedFacetsMap);

    final DatasetVersion initialDatasetVersion =
        client.getDatasetVersion(
            NAMESPACE_NAME, DB_TABLE_NAME, versions.get(versions.size() - 1).getVersion());
    assertThat(initialDatasetVersion.getPhysicalName()).isEqualTo(DB_TABLE_META.getPhysicalName());
    assertThat(initialDatasetVersion.getSourceName()).isEqualTo(DB_TABLE_META.getSourceName());
    assertThat(initialDatasetVersion.getFields()).hasSameElementsAs(DB_TABLE_META.getFields());
    assertThat(initialDatasetVersion.getTags()).isEqualTo(DB_TABLE_META.getTags());
    assertThat(initialDatasetVersion.getCreatedByRun()).isNotPresent();
    assertThat(initialDatasetVersion.hasFacets()).isFalse();

    final DatasetVersion latestDatasetVersion =
        client.getDatasetVersion(NAMESPACE_NAME, DB_TABLE_NAME, versions.get(0).getVersion());
    assertThat(latestDatasetVersion.getCreatedByRun()).isPresent();
    assertThat(latestDatasetVersion.getCreatedByRun().get().getId())
        .isEqualTo(lineageEvent.getRun().getRunId());
    assertThat(latestDatasetVersion.hasFacets()).isTrue();
    assertThat(latestDatasetVersion.getFacets()).isEqualTo(expectedFacetsMap);
  }

  @Test
  public void testApp_getStreamVersion() {
    client.createDataset(NAMESPACE_NAME, STREAM_NAME, STREAM_META);
    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, STREAM_NAME);
    assertThat(versions).hasSizeGreaterThan(0);
    DatasetVersion datasetVersion =
        client.getDatasetVersion(NAMESPACE_NAME, STREAM_NAME, versions.get(0).getVersion());

    assertThat(datasetVersion).isInstanceOf(StreamVersion.class);
    assertThat(datasetVersion.getId()).isEqualTo(new DatasetId(NAMESPACE_NAME, STREAM_NAME));
    assertThat(datasetVersion.getName()).isEqualTo(STREAM_NAME);
    assertThat(datasetVersion.getCreatedAt()).isNotNull();
    assertThat(datasetVersion.getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(datasetVersion.getVersion()).isNotNull();
    assertThat(datasetVersion.getPhysicalName()).isEqualTo(STREAM_META.getPhysicalName());
    assertThat(datasetVersion.getSourceName()).isEqualTo(STREAM_META.getSourceName());
    assertThat(datasetVersion.getDescription()).isEqualTo(STREAM_META.getDescription());
    assertThat(datasetVersion.getFields()).hasSameElementsAs(STREAM_META.getFields());
    assertThat(datasetVersion.getTags()).isEqualTo(STREAM_META.getTags());
    assertThat(((StreamVersion) datasetVersion).getSchemaLocation())
        .isEqualTo(STREAM_META.getSchemaLocation());
    assertThat(datasetVersion.getCreatedByRun()).isEqualTo(Optional.empty());
  }

  @Test
  public void testApp_getDBTableVersionWithRun() {
    DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();
    client.createDataset(NAMESPACE_NAME, "table1", DB_TABLE_META);

    final JobMeta jobMeta =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(NAMESPACE_NAME, "table1")
            .location(JOB_LOCATION)
            .description(JOB_DESCRIPTION)
            .build();

    client.createJob(NAMESPACE_NAME, JOB_NAME, jobMeta);

    final RunMeta runMeta = RunMeta.builder().build();
    final Run run = client.createRun(NAMESPACE_NAME, JOB_NAME, runMeta);

    DbTableMeta DB_TABLE_META_WITH_RUN =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(run.getId())
            .build();
    client.createDataset(NAMESPACE_NAME, "table1", DB_TABLE_META_WITH_RUN);

    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, "table1");
    assertThat(versions).hasSizeGreaterThan(1);
    DatasetVersion version = versions.get(0); // most recent dataset version
    assertThat(version.getCreatedByRun()).isNotEqualTo(Optional.empty());
    Run createdRun = version.getCreatedByRun().get();
    assertThat(createdRun.getCreatedAt()).isEqualTo(run.getCreatedAt());
    assertThat(createdRun.getId()).isEqualTo(run.getId());
    assertThat(createdRun.getUpdatedAt()).isEqualTo(run.getUpdatedAt());
    assertThat(createdRun.getDurationMs()).isEqualTo(run.getDurationMs());
    assertThat(createdRun.getState()).isEqualTo(run.getState());
    assertThat(createdRun.getArgs()).isEqualTo(run.getArgs());
    assertThat(createdRun.getNominalStartTime()).isEqualTo(run.getNominalStartTime());
    assertThat(createdRun.getNominalEndTime()).isEqualTo(run.getNominalEndTime());
  }

  @Test
  public void testApp_notExistsDatasetName() {
    Assertions.assertThrows(
        Exception.class, () -> client.getDataset(NAMESPACE_NAME, "not-existing"));
  }

  @Test
  public void testApp_notExistsDatasetVersionName() {
    Assertions.assertThrows(
        Exception.class,
        () ->
            client.getDatasetVersion(NAMESPACE_NAME, "not-existing", UUID.randomUUID().toString()));
  }

  @Test
  public void testApp_notExistsNamespace() {
    Assertions.assertThrows(
        Exception.class, () -> client.getDataset("non-existing", "not-existing"));
  }

  @Test
  public void testApp_notExistsRun() {
    DbTableMeta RUN_NOT_EXISTS =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    Assertions.assertThrows(
        Exception.class, () -> client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS));
  }

  @Test
  public void testApp_notExistsSource() {
    DbTableMeta RUN_NOT_EXISTS =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName("not-exists")
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(UUID.randomUUID().toString())
            .build();
    Assertions.assertThrows(
        Exception.class, () -> client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, RUN_NOT_EXISTS));
  }

  @Test
  public void testApp_upsertDescription() {
    DbTableMeta DESCRIPTION =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    Dataset dataset = client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DESCRIPTION);
    assertThat(dataset.getDescription()).isEqualTo(DESCRIPTION.getDescription());

    DbTableMeta WO_DESCRIPTION =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .build();

    Dataset dataset2 = client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, WO_DESCRIPTION);
    // Description stays
    assertThat(dataset2.getDescription()).isEqualTo(DESCRIPTION.getDescription());
  }

  @Test
  public void testApp_doesNotShowDeletedDataset() throws IOException {
    String namespace = "namespace";
    String name = "table";
    LineageEvent event =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job("namespace", "job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(namespace, name, LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    final CompletableFuture<Integer> resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(201);

    client.deleteDataset(namespace, name);

    List<Dataset> datasets = client.listDatasets(namespace);
    assertThat(datasets).hasSize(0);
  }

  @Test
  public void testApp_showsDeletedDatasetAfterReceivingNewVersion() throws IOException {
    String namespace = "namespace";
    String name = "anotherTable";
    LineageEvent event =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job("namespace", "job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(namespace, name, LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    CompletableFuture<Integer> resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(201);

    client.deleteDataset(namespace, name);

    List<Dataset> datasets = client.listDatasets(namespace);
    assertThat(datasets).hasSize(0);
    resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(201);

    datasets = client.listDatasets(namespace);
    assertThat(datasets).hasSize(1);
  }

  @Test
  public void testApp_getDatasetContainsColumnLineage() {
    LineageEvent event =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job("namespace", "job_name", null))
            .inputs(List.of(getDatasetA()))
            .outputs(List.of(getDatasetB()))
            .producer("the_producer")
            .build();

    CompletableFuture<Integer> resp =
        this.sendLineage(Utils.toJson(event))
            .thenApply(HttpResponse::statusCode)
            .whenComplete(
                (val, error) -> {
                  if (error != null) {
                    Assertions.fail("Could not complete request");
                  }
                });
    resp.join();

    // verify listDatasets contains column lineage
    List<ColumnLineage> columnLineage;

    columnLineage =
        client.listDatasets("namespace").stream()
            .filter(d -> d.getName().equals("dataset_b"))
            .findAny()
            .get()
            .getColumnLineage();
    assertThat(columnLineage).hasSize(1);
    assertThat(columnLineage.get(0).getInputFields()).hasSize(2);

    // verify getDataset returns non-empty column lineage
    columnLineage = client.getDataset("namespace", "dataset_b").getColumnLineage();
    assertThat(columnLineage).hasSize(1);
    assertThat(columnLineage.get(0).getInputFields()).hasSize(2);
  }

  @Test
  public void testApp_doesNotShowDeletedDatasetAfterDeleteNamespace() throws IOException {
    String namespace = "namespace";
    String name = "table";
    LineageEvent event =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job("namespace", "job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(namespace, name, LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    final CompletableFuture<Integer> resp = sendEvent(event);
    assertThat(resp.join()).isEqualTo(201);

    client.deleteNamespace(namespace);

    List<Dataset> datasets = client.listDatasets(namespace);
    assertThat(datasets).hasSize(0);
  }

  @Test
  public void testApp_doesNotShowDeletedDatasetAfterUndeleteNamespace() throws IOException {
    String namespaceName = "namespace";
    String name = "table";

    LineageEvent firstEvent =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job(namespaceName, "job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(
                        namespaceName, name, LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    LineageEvent secondEvent =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job(namespaceName, "second_job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(
                        namespaceName, name + "2", LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    CompletableFuture<Integer> resp = sendEvent(firstEvent);
    assertThat(resp.join()).isEqualTo(201);

    resp = sendEvent(secondEvent);
    assertThat(resp.join()).isEqualTo(201);

    List<Dataset> datasets = client.listDatasets(namespaceName);
    assertThat(datasets).hasSize(2);

    client.deleteNamespace(namespaceName);

    List<Namespace> namespaces = client.listNamespaces();
    assertThat(namespaces)
        .anySatisfy(
            namespace -> {
              assertThat(namespace.getIsHidden()).isTrue();
              assertThat(namespace.getName()).isEqualTo(namespaceName);
            });

    datasets = client.listDatasets(namespaceName);
    assertThat(datasets).hasSize(0);

    List<Job> jobs = client.listJobs(namespaceName);
    assertThat(jobs).hasSize(0);

    LineageEvent eventThatWillUndeleteNamespace =
        LineageEvent.builder()
            .eventType("COMPLETE")
            .eventTime(Instant.now().atZone(ZoneId.systemDefault()))
            .run(new LineageEvent.Run(UUID.randomUUID().toString(), null))
            .job(new LineageEvent.Job(namespaceName, "job_name", null))
            .inputs(
                List.of(
                    new LineageEvent.Dataset(
                        namespaceName, name, LineageTestUtils.newDatasetFacet())))
            .outputs(Collections.emptyList())
            .producer("the_producer")
            .build();

    resp = sendEvent(eventThatWillUndeleteNamespace);
    assertThat(resp.join()).isEqualTo(201);

    namespaces = client.listNamespaces();
    assertThat(namespaces)
        .anySatisfy(
            namespace -> {
              assertThat(namespace.getIsHidden()).isFalse();
              assertThat(namespace.getName()).isEqualTo(namespaceName);
            });

    datasets = client.listDatasets(namespaceName);
    assertThat(datasets).hasSize(1);

    jobs = client.listJobs(namespaceName);
    assertThat(jobs).hasSize(1);
  }

  @Test
  public void testApp_getTableVersionsWithSymlinks() {
    client.createDataset(NAMESPACE_NAME, DB_TABLE_NAME, DB_TABLE_META);

    ImmutableMap<String, Object> outputFacets =
        ImmutableMap.of("outputFacetKey", "outputFacetValue");
    ImmutableMap<String, Object> inputFacets = ImmutableMap.of("inputFacetKey", "inputFacetValue");

    final LineageEvent.DatasetFacets datasetFacets =
        LineageTestUtils.newDatasetFacet(
            outputFacets,
            LineageEvent.SchemaField.builder()
                .name("firstname")
                .type("string")
                .description("the first name")
                .build());
    datasetFacets
        .getDocumentation()
        .setDescription(DB_TABLE_META.getDescription().orElse("the dataset documentation"));
    datasetFacets.setSymlinks(
        new LineageEvent.DatasetSymlinkFacet(
            PRODUCER_URL,
            SCHEMA_URL,
            Collections.singletonList(
                new LineageEvent.SymlinkIdentifier("symlinkNamespace", "symlinkName", "type"))));
    final LineageEvent lineageEvent =
        LineageEvent.builder()
            .producer("testApp_getTableVersionsWithSymlinks")
            .eventType("COMPLETE")
            .run(
                new LineageEvent.Run(
                    UUID.randomUUID().toString(), LineageEvent.RunFacet.builder().build()))
            .job(LineageEvent.Job.builder().namespace(NAMESPACE_NAME).name(JOB_NAME).build())
            .eventTime(ZonedDateTime.now())
            .inputs(
                Collections.singletonList(
                    LineageEvent.Dataset.builder()
                        .namespace(NAMESPACE_NAME)
                        .name(DB_TABLE_NAME)
                        .facets(datasetFacets)
                        .build()))
            .outputs(Collections.emptyList())
            .build();
    final CompletableFuture<Integer> resp = sendEvent(lineageEvent);
    assertThat(resp.join()).isEqualTo(201);
    List<DatasetVersion> versions = client.listDatasetVersions(NAMESPACE_NAME, DB_TABLE_NAME);

    versions.forEach(
        datasetVersion -> {
          assertThat(datasetVersion.getName()).isNotEqualTo("symlinkName");
        });
  }
}
