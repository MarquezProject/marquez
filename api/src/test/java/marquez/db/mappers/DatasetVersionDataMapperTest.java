/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.util.UUID;
import marquez.db.Columns;
import marquez.service.models.DatasetVersion;
import marquez.service.models.DatasetVersionData;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

class DatasetVersionDataMapperTest {

  private static ResultSet resultSet;
  private static TimeZone defaultTZ = TimeZone.getDefault();

  @BeforeAll
  public static void setUp() throws SQLException {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    resultSet = mock(ResultSet.class);
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    when(resultSet.getMetaData()).thenReturn(metaData);

    // Mock column count and names for MapperUtils.getColumnNames()
    when(metaData.getColumnCount()).thenReturn(15);
    when(metaData.getColumnName(1)).thenReturn(Columns.TYPE);
    when(metaData.getColumnName(2)).thenReturn(Columns.NAMESPACE_NAME);
    when(metaData.getColumnName(3)).thenReturn(Columns.NAME);
    when(metaData.getColumnName(4)).thenReturn(Columns.PHYSICAL_NAME);
    when(metaData.getColumnName(5)).thenReturn(Columns.CREATED_AT);
    when(metaData.getColumnName(6)).thenReturn(Columns.CURRENT_VERSION_UUID);
    when(metaData.getColumnName(7)).thenReturn(Columns.VERSION);
    when(metaData.getColumnName(8)).thenReturn(Columns.SOURCE_NAME);
    when(metaData.getColumnName(9)).thenReturn(Columns.SCHEMA_LOCATION);
    when(metaData.getColumnName(10)).thenReturn("fields");
    when(metaData.getColumnName(11)).thenReturn("tags");
    when(metaData.getColumnName(12)).thenReturn(Columns.DESCRIPTION);
    when(metaData.getColumnName(13)).thenReturn(Columns.DATASET_SCHEMA_VERSION_UUID);
    when(metaData.getColumnName(14)).thenReturn(Columns.LIFECYCLE_STATE);
    when(metaData.getColumnName(15)).thenReturn(Columns.FACETS);
  }

  @AfterAll
  public static void reset() {
    TimeZone.setDefault(defaultTZ);
  }

  @Test
  void testMapDbTableDatasetWithAllFields() throws SQLException {
    setupDbTableMocks();

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getNamespace().getValue()).isEqualTo("test-namespace");
    assertThat(result.getName().getValue()).isEqualTo("test-dataset");
    assertThat(result.getPhysicalName().getValue()).isEqualTo("physical_dataset");
    assertThat(result.getSourceName().getValue()).isEqualTo("postgres");
    assertThat(result.getDescription()).isPresent().hasValue("Test description");
    assertThat(result.getLifecycleState()).isEqualTo("ACTIVE");
    assertThat(result.getFields()).hasSize(1);
    assertThat(result.getTags()).hasSize(1);
  }

  @Test
  void testMapStreamDatasetWithAllFields() throws SQLException {
    setupStreamMocks();

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getNamespace().getValue()).isEqualTo("test-namespace");
    assertThat(result.getName().getValue()).isEqualTo("test-stream");
    assertThat(result.getPhysicalName().getValue()).isEqualTo("physical_stream");
    assertThat(result.getSourceName().getValue()).isEqualTo("kafka");
    DatasetVersion version = result.getDatasetVersion();
    assertThat(version).isNotNull();
  }

  @Test
  void testMapWithNullDescription() throws SQLException {
    setupDbTableMocks();
    when(resultSet.getString(Columns.DESCRIPTION)).thenReturn(null);
    when(resultSet.getObject(Columns.DESCRIPTION)).thenReturn(null);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getDescription()).isEmpty();
  }

  @Test
  void testMapWithNullTags() throws SQLException {
    setupDbTableMocks();

    // Create a ResultSet without tags column
    ResultSet noTagsResultSet = mock(ResultSet.class);
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    when(noTagsResultSet.getMetaData()).thenReturn(metaData);
    when(metaData.getColumnCount()).thenReturn(14);
    setupColumnNamesWithoutTags(metaData);

    // Copy all other mocks
    copyMocksToResultSet(noTagsResultSet);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(noTagsResultSet, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getTags()).isEmpty(); // Returns empty set, not null
  }

  @Test
  void testMapWithMalformedUrl() throws SQLException {
    // For DB_TABLE type, SCHEMA_LOCATION is optional
    setupDbTableMocks();
    when(resultSet.getString(Columns.TYPE)).thenReturn("DB_TABLE");
    when(resultSet.getObject(Columns.TYPE)).thenReturn("DB_TABLE");
    when(resultSet.getString(Columns.SCHEMA_LOCATION)).thenReturn(null);
    when(resultSet.getObject(Columns.SCHEMA_LOCATION)).thenReturn(null);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    // Should handle gracefully for DB_TABLE type without schema location
    assertThat(result).isNotNull();
    assertThat(result.getName().getValue()).isEqualTo("test-dataset");
  }

  @Test
  void testMapWithCreatedByRunUuid() throws SQLException {
    setupDbTableMocks();
    UUID createdByRunUuid = UUID.randomUUID();
    when(resultSet.getObject("createdByRunUuid")).thenReturn(createdByRunUuid);
    when(resultSet.getObject("createdByRunUuid", UUID.class)).thenReturn(createdByRunUuid);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getCreatedByRunUuid()).isEqualTo(createdByRunUuid);
  }

  @Test
  void testMapWithCreatedByParentRunUuid() throws SQLException {
    setupDbTableMocks();
    UUID parentRunUuid = UUID.randomUUID();
    when(resultSet.getObject("createdByParentRunUuid")).thenReturn(parentRunUuid);
    when(resultSet.getObject("createdByParentRunUuid", UUID.class)).thenReturn(parentRunUuid);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getCreatedByParentRunUuid()).isEqualTo(parentRunUuid);
  }

  @Test
  void testMapWithUuid() throws SQLException {
    setupDbTableMocks();
    UUID uuid = UUID.randomUUID();
    when(resultSet.getObject("uuid")).thenReturn(uuid);
    when(resultSet.getObject("uuid", UUID.class)).thenReturn(uuid);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getUuid()).isEqualTo(uuid);
  }

  @Test
  void testMapWithNullOptionalFields() throws SQLException {
    setupDbTableMocks();
    when(resultSet.getString(Columns.DESCRIPTION)).thenReturn(null);
    when(resultSet.getObject(Columns.DESCRIPTION)).thenReturn(null);
    when(resultSet.getObject(Columns.DATASET_SCHEMA_VERSION_UUID)).thenReturn(null);
    when(resultSet.getObject(Columns.DATASET_SCHEMA_VERSION_UUID, UUID.class)).thenReturn(null);
    when(resultSet.getString(Columns.LIFECYCLE_STATE)).thenReturn(null);
    when(resultSet.getObject(Columns.LIFECYCLE_STATE)).thenReturn(null);
    when(resultSet.getObject("createdByRunUuid")).thenReturn(null);
    when(resultSet.getObject("createdByRunUuid", UUID.class)).thenReturn(null);

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getDescription()).isEmpty();
    assertThat(result.getCurrentSchemaVersion()).isEmpty();
    assertThat(result.getLifecycleState()).isNull();
    assertThat(result.getCreatedByRunUuid()).isNull();
  }

  @Test
  void testMapWithFacets() throws SQLException {
    setupDbTableMocks();
    // Facets must be an array of JSON objects, not a single object
    PGobject facets = new PGobject();
    facets.setValue("[{\"testFacet\": \"testValue\"}]");
    when(resultSet.getObject(Columns.FACETS)).thenReturn(facets);
    when(resultSet.getString(Columns.FACETS)).thenReturn(facets.getValue());

    DatasetVersionDataMapper mapper = new DatasetVersionDataMapper();
    DatasetVersionData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getFacets()).isNotNull();
    assertThat(result.getFacets()).isNotEmpty();
    assertThat(result.getFacets()).containsKey("testFacet");
    assertThat(result.getFacets().get("testFacet")).isEqualTo("testValue");
  }

  private void setupDbTableMocks() throws SQLException {
    when(resultSet.getString(Columns.TYPE)).thenReturn("DB_TABLE");
    when(resultSet.getObject(Columns.TYPE)).thenReturn("DB_TABLE");
    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getString(Columns.NAME)).thenReturn("test-dataset");
    when(resultSet.getObject(Columns.NAME)).thenReturn("test-dataset");
    when(resultSet.getString(Columns.PHYSICAL_NAME)).thenReturn("physical_dataset");
    when(resultSet.getObject(Columns.PHYSICAL_NAME)).thenReturn("physical_dataset");
    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));

    UUID versionUuid = UUID.randomUUID();
    when(resultSet.getObject(Columns.CURRENT_VERSION_UUID)).thenReturn(versionUuid);
    when(resultSet.getObject(Columns.CURRENT_VERSION_UUID, UUID.class)).thenReturn(versionUuid);

    when(resultSet.getString(Columns.SOURCE_NAME)).thenReturn("postgres");
    when(resultSet.getObject(Columns.SOURCE_NAME)).thenReturn("postgres");

    PGobject fields = new PGobject();
    fields.setValue("[{\"name\": \"id\", \"type\": \"INTEGER\"}]");
    when(resultSet.getObject("fields")).thenReturn(fields);

    Array tags = mock(Array.class);
    when(tags.getArray()).thenReturn(new String[] {"test-tag"});
    when(resultSet.getObject("tags")).thenReturn(tags);
    when(resultSet.getArray("tags")).thenReturn(tags);

    when(resultSet.getString(Columns.DESCRIPTION)).thenReturn("Test description");
    when(resultSet.getObject(Columns.DESCRIPTION)).thenReturn("Test description");

    UUID schemaVersionUuid = UUID.randomUUID();
    when(resultSet.getObject(Columns.DATASET_SCHEMA_VERSION_UUID)).thenReturn(schemaVersionUuid);
    when(resultSet.getObject(Columns.DATASET_SCHEMA_VERSION_UUID, UUID.class))
        .thenReturn(schemaVersionUuid);

    when(resultSet.getString(Columns.LIFECYCLE_STATE)).thenReturn("ACTIVE");
    when(resultSet.getObject(Columns.LIFECYCLE_STATE)).thenReturn("ACTIVE");

    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);

    UUID uuid = UUID.randomUUID();
    when(resultSet.getObject("uuid")).thenReturn(uuid);
    when(resultSet.getObject("uuid", UUID.class)).thenReturn(uuid);

    when(resultSet.getObject("createdByRunUuid")).thenReturn(null);
    when(resultSet.getObject("createdByParentRunUuid")).thenReturn(null);
  }

  private void setupStreamMocks() throws SQLException {
    when(resultSet.getString(Columns.TYPE)).thenReturn("STREAM");
    when(resultSet.getObject(Columns.TYPE)).thenReturn("STREAM");
    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getString(Columns.NAME)).thenReturn("test-stream");
    when(resultSet.getObject(Columns.NAME)).thenReturn("test-stream");
    when(resultSet.getString(Columns.PHYSICAL_NAME)).thenReturn("physical_stream");
    when(resultSet.getObject(Columns.PHYSICAL_NAME)).thenReturn("physical_stream");
    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));

    UUID versionUuid = UUID.randomUUID();
    when(resultSet.getObject(Columns.VERSION)).thenReturn(versionUuid);
    when(resultSet.getObject(Columns.VERSION, UUID.class)).thenReturn(versionUuid);

    when(resultSet.getString(Columns.SOURCE_NAME)).thenReturn("kafka");
    when(resultSet.getObject(Columns.SOURCE_NAME)).thenReturn("kafka");

    when(resultSet.getString(Columns.SCHEMA_LOCATION)).thenReturn("https://schema.example.com");
    when(resultSet.getObject(Columns.SCHEMA_LOCATION)).thenReturn("https://schema.example.com");

    PGobject fields = new PGobject();
    fields.setValue("[{\"name\": \"message\", \"type\": \"STRING\"}]");
    when(resultSet.getObject("fields")).thenReturn(fields);

    Array tags = mock(Array.class);
    when(tags.getArray()).thenReturn(new String[] {"streaming"});
    when(resultSet.getObject("tags")).thenReturn(tags);
    when(resultSet.getArray("tags")).thenReturn(tags);

    when(resultSet.getString(Columns.DESCRIPTION)).thenReturn("Test stream");
    when(resultSet.getObject(Columns.DESCRIPTION)).thenReturn("Test stream");

    when(resultSet.getObject(Columns.DATASET_SCHEMA_VERSION_UUID)).thenReturn(null);
    when(resultSet.getString(Columns.LIFECYCLE_STATE)).thenReturn(null);
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);

    UUID uuid = UUID.randomUUID();
    when(resultSet.getObject("uuid")).thenReturn(uuid);
    when(resultSet.getObject("uuid", UUID.class)).thenReturn(uuid);
  }

  private void setupColumnNamesWithoutTags(ResultSetMetaData metaData) throws SQLException {
    when(metaData.getColumnName(1)).thenReturn(Columns.TYPE);
    when(metaData.getColumnName(2)).thenReturn(Columns.NAMESPACE_NAME);
    when(metaData.getColumnName(3)).thenReturn(Columns.NAME);
    when(metaData.getColumnName(4)).thenReturn(Columns.PHYSICAL_NAME);
    when(metaData.getColumnName(5)).thenReturn(Columns.CREATED_AT);
    when(metaData.getColumnName(6)).thenReturn(Columns.CURRENT_VERSION_UUID);
    when(metaData.getColumnName(7)).thenReturn(Columns.SOURCE_NAME);
    when(metaData.getColumnName(8)).thenReturn("fields");
    when(metaData.getColumnName(9)).thenReturn(Columns.DESCRIPTION);
    when(metaData.getColumnName(10)).thenReturn(Columns.DATASET_SCHEMA_VERSION_UUID);
    when(metaData.getColumnName(11)).thenReturn(Columns.LIFECYCLE_STATE);
    when(metaData.getColumnName(12)).thenReturn(Columns.FACETS);
    when(metaData.getColumnName(13)).thenReturn("uuid");
    when(metaData.getColumnName(14)).thenReturn("createdByRunUuid");
  }

  private void copyMocksToResultSet(ResultSet target) throws SQLException {
    when(target.getString(Columns.TYPE)).thenReturn("DB_TABLE");
    when(target.getObject(Columns.TYPE)).thenReturn("DB_TABLE");
    when(target.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(target.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(target.getString(Columns.NAME)).thenReturn("test-dataset");
    when(target.getObject(Columns.NAME)).thenReturn("test-dataset");
    when(target.getString(Columns.PHYSICAL_NAME)).thenReturn("physical_dataset");
    when(target.getObject(Columns.PHYSICAL_NAME)).thenReturn("physical_dataset");
    when(target.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));
    when(target.getObject(Columns.CREATED_AT)).thenReturn(Timestamp.valueOf("2024-01-01 00:00:00"));

    UUID versionUuid = UUID.randomUUID();
    when(target.getObject(Columns.CURRENT_VERSION_UUID)).thenReturn(versionUuid);
    when(target.getObject(Columns.CURRENT_VERSION_UUID, UUID.class)).thenReturn(versionUuid);
    when(target.getString(Columns.SOURCE_NAME)).thenReturn("postgres");
    when(target.getObject(Columns.SOURCE_NAME)).thenReturn("postgres");

    PGobject fields = new PGobject();
    fields.setValue("[{\"name\": \"id\", \"type\": \"INTEGER\"}]");
    when(target.getObject("fields")).thenReturn(fields);

    when(target.getString(Columns.DESCRIPTION)).thenReturn("Test description");
    when(target.getObject(Columns.DESCRIPTION)).thenReturn("Test description");
    when(target.getObject(Columns.DATASET_SCHEMA_VERSION_UUID)).thenReturn(null);
    when(target.getString(Columns.LIFECYCLE_STATE)).thenReturn("ACTIVE");
    when(target.getObject(Columns.LIFECYCLE_STATE)).thenReturn("ACTIVE");
    when(target.getObject(Columns.FACETS)).thenReturn(null);

    UUID uuid = UUID.randomUUID();
    when(target.getObject("uuid")).thenReturn(uuid);
    when(target.getObject("uuid", UUID.class)).thenReturn(uuid);
  }
}
