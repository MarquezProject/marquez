/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;
import org.junit.jupiter.api.Test;

class DatasetVersionDataTest {

  @Test
  void testConstructWithValidDatasetVersion() {
    DbTableVersion datasetVersion = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(datasetVersion);

    assertThat(data.getDatasetVersion()).isEqualTo(datasetVersion);
    assertThat(data.getName()).isEqualTo(datasetVersion.getName());
    assertThat(data.getNamespace()).isEqualTo(datasetVersion.getNamespace());
  }

  @Test
  void testGetNamespace() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getNamespace()).isEqualTo(NamespaceName.of("test-namespace"));
  }

  @Test
  void testGetName() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getName()).isEqualTo(DatasetName.of("test-dataset"));
  }

  @Test
  void testGetPhysicalName() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getPhysicalName()).isEqualTo(DatasetName.of("physical_dataset"));
  }

  @Test
  void testGetSourceName() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getSourceName()).isEqualTo(SourceName.of("test-source"));
  }

  @Test
  void testGetFields() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    ImmutableList<Field> fields = data.getFields();
    assertThat(fields).hasSize(1);
    assertThat(fields.get(0).getName().getValue()).isEqualTo("id");
  }

  @Test
  void testGetTags() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    ImmutableSet<TagName> tags = data.getTags();
    assertThat(tags).hasSize(1);
    assertThat(tags).contains(TagName.of("test-tag"));
  }

  @Test
  void testGetDescriptionWhenPresent() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    Optional<String> description = data.getDescription();
    assertThat(description).isPresent();
    assertThat(description.get()).isEqualTo("Test dataset description");
  }

  @Test
  void testGetDescriptionWhenAbsent() {
    DbTableVersion version =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("ns"), DatasetName.of("ds")),
            DatasetName.of("ds"),
            DatasetName.of("physical"),
            Instant.now(),
            Version.of(UUID.randomUUID()),
            SourceName.of("source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null, // null description
            null,
            null,
            null,
            ImmutableMap.of());

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getDescription()).isEmpty();
  }

  @Test
  void testGetCurrentSchemaVersionWhenPresent() {
    UUID schemaVersionUuid = UUID.randomUUID();
    DbTableVersion version =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("ns"), DatasetName.of("ds")),
            DatasetName.of("ds"),
            DatasetName.of("physical"),
            Instant.now(),
            Version.of(UUID.randomUUID()),
            SourceName.of("source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            schemaVersionUuid,
            null,
            null,
            ImmutableMap.of());

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getCurrentSchemaVersion()).isPresent().contains(schemaVersionUuid);
  }

  @Test
  void testGetCurrentSchemaVersionWhenAbsent() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getCurrentSchemaVersion()).isEmpty();
  }

  @Test
  void testGetLifecycleStateWhenPresent() {
    DbTableVersion version =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("ns"), DatasetName.of("ds")),
            DatasetName.of("ds"),
            DatasetName.of("physical"),
            Instant.now(),
            Version.of(UUID.randomUUID()),
            SourceName.of("source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            null,
            "ACTIVE",
            null,
            ImmutableMap.of());

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getLifecycleState()).isEqualTo("ACTIVE");
  }

  @Test
  void testGetLifecycleStateWhenAbsent() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    assertThat(data.getLifecycleState()).isNull();
  }

  @Test
  void testGetCreatedByRunUuid() {
    UUID runUuid = UUID.randomUUID();
    DatasetVersion version = createTestDbTableVersion();
    version.setCreatedByRunUuid(runUuid);

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getCreatedByRunUuid()).isEqualTo(runUuid);
  }

  @Test
  void testGetCreatedByRunWhenPresent() {
    DatasetVersion version = createTestDbTableVersion();
    // Note: getCreatedByRun returns Optional based on the version's run
    DatasetVersionData data = new DatasetVersionData(version);

    // Since we don't set a Run object in our test version, it should be empty
    assertThat(data.getCreatedByRun()).isEmpty();
  }

  @Test
  void testSetAndGetUuid() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    UUID uuid = UUID.randomUUID();
    data.setUuid(uuid);

    assertThat(data.getUuid()).isEqualTo(uuid);
  }

  @Test
  void testSetAndGetCreatedByParentRunUuid() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    UUID parentRunUuid = UUID.randomUUID();
    data.setCreatedByParentRunUuid(parentRunUuid);

    assertThat(data.getCreatedByParentRunUuid()).isEqualTo(parentRunUuid);
  }

  @Test
  void testGetFacets() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    ImmutableMap<String, Object> facets = data.getFacets();
    assertThat(facets).containsEntry("testFacet", "testValue");
  }

  @Test
  void testGetVersion() {
    UUID versionUuid = UUID.randomUUID();
    DbTableVersion version =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("ns"), DatasetName.of("ds")),
            DatasetName.of("ds"),
            DatasetName.of("physical"),
            Instant.now(),
            Version.of(versionUuid),
            SourceName.of("source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            null,
            null,
            null,
            ImmutableMap.of());

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getVersion().getValue()).isEqualTo(versionUuid);
  }

  @Test
  void testGetCreatedAt() {
    Instant createdAt = Instant.now();
    DbTableVersion version =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("ns"), DatasetName.of("ds")),
            DatasetName.of("ds"),
            DatasetName.of("physical"),
            createdAt,
            Version.of(UUID.randomUUID()),
            SourceName.of("source"),
            ImmutableList.of(),
            ImmutableSet.of(),
            null,
            null,
            null,
            null,
            ImmutableMap.of());

    DatasetVersionData data = new DatasetVersionData(version);
    assertThat(data.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void testEqualsAndHashCode() {
    UUID sameVersionUuid = UUID.randomUUID();

    Field field = new Field(FieldName.of("id"), "INTEGER", ImmutableSet.of(), "ID field");
    DbTableVersion version1 =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("test-namespace"), DatasetName.of("test-dataset")),
            DatasetName.of("test-dataset"),
            DatasetName.of("physical_dataset"),
            Instant.now(),
            Version.of(sameVersionUuid),
            SourceName.of("test-source"),
            ImmutableList.of(field),
            ImmutableSet.of(TagName.of("test-tag")),
            "Test dataset description",
            null,
            null,
            null,
            ImmutableMap.of("testFacet", "testValue"));

    DbTableVersion version2 =
        new DbTableVersion(
            new DatasetId(NamespaceName.of("test-namespace"), DatasetName.of("test-dataset")),
            DatasetName.of("test-dataset"),
            DatasetName.of("physical_dataset"),
            version1.getCreatedAt(),
            Version.of(sameVersionUuid),
            SourceName.of("test-source"),
            ImmutableList.of(field),
            ImmutableSet.of(TagName.of("test-tag")),
            "Test dataset description",
            null,
            null,
            null,
            ImmutableMap.of("testFacet", "testValue"));

    DatasetVersionData data1 = new DatasetVersionData(version1);
    DatasetVersionData data2 = new DatasetVersionData(version2);

    // They should be equal if wrapping the same version
    assertThat(data1).isEqualTo(data2);
    assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
  }

  @Test
  void testToString() {
    DatasetVersion version = createTestDbTableVersion();
    DatasetVersionData data = new DatasetVersionData(version);

    String toString = data.toString();
    assertThat(toString).contains("DatasetVersionData");
    assertThat(toString).contains("version=");
  }

  private DbTableVersion createTestDbTableVersion() {
    Field field = new Field(FieldName.of("id"), "INTEGER", ImmutableSet.of(), "ID field");
    return new DbTableVersion(
        new DatasetId(NamespaceName.of("test-namespace"), DatasetName.of("test-dataset")),
        DatasetName.of("test-dataset"),
        DatasetName.of("physical_dataset"),
        Instant.now(),
        Version.of(UUID.randomUUID()),
        SourceName.of("test-source"),
        ImmutableList.of(field),
        ImmutableSet.of(TagName.of("test-tag")),
        "Test dataset description",
        null,
        null,
        null,
        ImmutableMap.of("testFacet", "testValue"));
  }
}
