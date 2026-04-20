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
import marquez.common.models.RunState;
import marquez.db.Columns;
import marquez.service.models.RunData;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

class RunDataMapperTest {

  private static ResultSet resultSet;
  private static TimeZone defaultTZ = TimeZone.getDefault();

  @BeforeAll
  public static void setUp() throws SQLException {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    resultSet = mock(ResultSet.class);
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    when(resultSet.getMetaData()).thenReturn(metaData);

    // Setup column metadata
    when(metaData.getColumnCount()).thenReturn(20);
    setupColumnNames(metaData);
  }

  @AfterAll
  public static void reset() {
    TimeZone.setDefault(defaultTZ);
  }

  @Test
  void testMapCompleteRunWithAllFields() throws SQLException {
    setupCompleteRunMocks();

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isNotNull();
    assertThat(result.getState()).isEqualTo(RunState.COMPLETED);
    assertThat(result.getJobUuid()).isNotNull();
    assertThat(result.getStartedAt()).isPresent();
    assertThat(result.getEndedAt()).isPresent();
    assertThat(result.getDepth()).isEqualTo(2);
    assertThat(result.getJobVersionId()).isNotNull();
    assertThat(result.getJobVersionId().getName().getValue()).isEqualTo("test-job");
    assertThat(result.getJobVersionId().getNamespace().getValue()).isEqualTo("test-namespace");
  }

  @Test
  void testMapRunWithNullOptionalFields() throws SQLException {
    setupMinimalRunMocks();

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getStartedAt()).isEmpty();
    assertThat(result.getEndedAt()).isEmpty();
    assertThat(result.getJobVersionId()).isNull();
    assertThat(result.getFacets()).isEmpty(); // Returns empty map, not null
  }

  @Test
  void testMapRunWithEmptyInputOutputDatasets() throws SQLException {
    setupCompleteRunMocks();

    // Override with empty arrays
    Array emptyArray = mock(Array.class);
    when(emptyArray.getArray()).thenReturn(new UUID[0]);
    when(resultSet.getArray("input_uuids")).thenReturn(emptyArray);
    when(resultSet.getObject("input_uuids")).thenReturn(emptyArray);
    when(resultSet.getArray("output_uuids")).thenReturn(emptyArray);
    when(resultSet.getObject("output_uuids")).thenReturn(emptyArray);

    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getString(Columns.OUTPUT_VERSIONS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getInputUuids()).isEmpty();
    assertThat(result.getOutputUuids()).isEmpty();
    assertThat(result.getInputDatasetVersions()).isEmpty();
    assertThat(result.getOutputDatasetVersions()).isEmpty();
  }

  @Test
  void testMapRunWithFacetsPresent() throws SQLException {
    setupCompleteRunMocks();

    // Facets must be an array of JSON objects, not a single object
    PGobject facets = new PGobject();
    facets.setValue("[{\"testFacet\": \"test\"}]");
    when(resultSet.getObject(Columns.FACETS)).thenReturn(facets);
    when(resultSet.getString(Columns.FACETS)).thenReturn(facets.getValue());

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getFacets()).isNotNull();
    assertThat(result.getFacets()).isNotEmpty();
    assertThat(result.getFacets()).containsKey("testFacet");
  }

  @Test
  void testMapRunWithNullFacets() throws SQLException {
    setupCompleteRunMocks();
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getFacets()).isEmpty(); // Returns empty map, not null
  }

  @Test
  void testMapRunWithChildAndParentRunIds() throws SQLException {
    setupCompleteRunMocks();

    UUID childRunId = UUID.randomUUID();
    UUID parentRunId = UUID.randomUUID();

    Array childArray = mock(Array.class);
    when(childArray.getArray()).thenReturn(new UUID[] {childRunId});
    when(resultSet.getArray("child_run_id")).thenReturn(childArray);
    when(resultSet.getObject("child_run_id")).thenReturn(childArray);

    Array parentArray = mock(Array.class);
    when(parentArray.getArray()).thenReturn(new UUID[] {parentRunId});
    when(resultSet.getArray("parent_run_id")).thenReturn(parentArray);
    when(resultSet.getObject("parent_run_id")).thenReturn(parentArray);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getChildRunIds()).containsExactly(childRunId);
    assertThat(result.getParentRunIds()).containsExactly(parentRunId);
  }

  @Test
  void testMapRunWithInputDatasetVersions() throws SQLException {
    setupCompleteRunMocks();

    UUID datasetVersionUuid = UUID.randomUUID();
    String inputVersions =
        "[{\"namespace\": \"test-ns\", \"name\": \"test-dataset\", \"version\": \""
            + datasetVersionUuid
            + "\", \"dataset_version_uuid\": \""
            + datasetVersionUuid
            + "\"}]";
    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(inputVersions);

    // Empty dataset facets
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getInputDatasetVersions()).isNotEmpty();
    assertThat(result.getInputDatasetVersions()).hasSize(1);
    assertThat(result.getInputDatasetVersions().get(0).getDatasetVersionId().getName().getValue())
        .isEqualTo("test-dataset");
  }

  @Test
  void testMapRunWithOutputDatasetVersions() throws SQLException {
    setupCompleteRunMocks();

    UUID datasetVersionUuid = UUID.randomUUID();
    String outputVersions =
        "[{\"namespace\": \"test-ns\", \"name\": \"test-output\", \"version\": \""
            + datasetVersionUuid
            + "\", \"dataset_version_uuid\": \""
            + datasetVersionUuid
            + "\"}]";
    when(resultSet.getString(Columns.OUTPUT_VERSIONS)).thenReturn(outputVersions);

    // Empty dataset facets
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getOutputDatasetVersions()).isNotEmpty();
    assertThat(result.getOutputDatasetVersions()).hasSize(1);
    assertThat(result.getOutputDatasetVersions().get(0).getDatasetVersionId().getName().getValue())
        .isEqualTo("test-output");
  }

  @Test
  void testMapRunWithDatasetFacets() throws SQLException {
    setupCompleteRunMocks();

    UUID datasetVersionUuid = UUID.randomUUID();
    String inputVersions =
        "[{\"namespace\": \"test-ns\", \"name\": \"test-dataset\", \"version\": \""
            + datasetVersionUuid
            + "\", \"dataset_version_uuid\": \""
            + datasetVersionUuid
            + "\"}]";
    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(inputVersions);

    // Setup dataset facets
    String datasetFacets =
        "[{\"dataset_version_uuid\": \""
            + datasetVersionUuid
            + "\", \"name\": \"schema\", \"type\": \"input\", \"facet\": {\"schema\": {\"fields\": []}}}]";
    PGobject facetsObj = new PGobject();
    facetsObj.setValue(datasetFacets);
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(facetsObj);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getInputDatasetVersions()).isNotEmpty();
    assertThat(result.getInputDatasetVersions().get(0).getFacets()).isNotEmpty();
  }

  @Test
  void testMapRunWithMalformedJsonInDatasetVersions() throws SQLException {
    setupCompleteRunMocks();

    // Malformed JSON - should be handled gracefully
    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    // When input_versions is null, should return empty list
    assertThat(result.getInputDatasetVersions()).isEmpty();
  }

  @Test
  void testToJobVersionIdWithNullVersion() throws SQLException {
    UUID runUuid = UUID.randomUUID();
    UUID jobUuid = UUID.randomUUID();

    when(resultSet.getObject(Columns.ROW_UUID)).thenReturn(runUuid);
    when(resultSet.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(runUuid);
    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
    when(resultSet.getTimestamp(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));
    when(resultSet.getObject(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));
    when(resultSet.getTimestamp(Columns.STARTED_AT)).thenReturn(null);
    when(resultSet.getObject(Columns.STARTED_AT)).thenReturn(null);
    when(resultSet.getTimestamp(Columns.ENDED_AT)).thenReturn(null);
    when(resultSet.getObject(Columns.ENDED_AT)).thenReturn(null);
    when(resultSet.getString(Columns.STATE)).thenReturn("NEW");
    when(resultSet.getObject(Columns.STATE)).thenReturn("NEW");
    when(resultSet.getObject(Columns.JOB_UUID)).thenReturn(jobUuid);
    when(resultSet.getObject(Columns.JOB_UUID, UUID.class)).thenReturn(jobUuid);

    // Job version UUID is null - this is the key test case
    when(resultSet.getObject(Columns.JOB_VERSION_UUID)).thenReturn(null);
    when(resultSet.getObject(Columns.JOB_VERSION_UUID, UUID.class)).thenReturn(null);

    // But namespace and job name are present (this won't cause exception since version is checked
    // first)
    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getString(Columns.JOB_NAME)).thenReturn("test-job");
    when(resultSet.getObject(Columns.JOB_NAME)).thenReturn("test-job");

    Array emptyArray = mock(Array.class);
    when(emptyArray.getArray()).thenReturn(new UUID[0]);
    when(resultSet.getArray("input_uuids")).thenReturn(emptyArray);
    when(resultSet.getArray("output_uuids")).thenReturn(emptyArray);
    when(resultSet.getArray("child_run_id")).thenReturn(emptyArray);
    when(resultSet.getArray("parent_run_id")).thenReturn(emptyArray);
    when(resultSet.getInt("depth")).thenReturn(0);
    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getString(Columns.OUTPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(null);
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getJobVersionId()).isNull();
  }

  @Test
  void testToJobVersionIdWithNullNamespace() throws SQLException {
    // This test verifies minimal run data without job version information
    setupMinimalRunMocks();

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    // When namespace is null, jobVersionId should be null
    assertThat(result.getJobVersionId()).isNull();
  }

  @Test
  void testToJobVersionIdWithNullJobName() throws SQLException {
    // This test verifies minimal run data without job version information
    setupMinimalRunMocks();

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    // When job name is null, jobVersionId should be null
    assertThat(result.getJobVersionId()).isNull();
  }

  @Test
  void testMapRunWithAllRunStates() throws SQLException {
    for (RunState state : RunState.values()) {
      setupCompleteRunMocks();
      when(resultSet.getString(Columns.STATE)).thenReturn(state.name());
      when(resultSet.getObject(Columns.STATE)).thenReturn(state.name());

      RunDataMapper mapper = new RunDataMapper();
      RunData result = mapper.map(resultSet, mock(StatementContext.class));

      assertThat(result.getState()).isEqualTo(state);
    }
  }

  @Test
  void testMapRunWithoutInputVersionsColumn() throws SQLException {
    // Setup a ResultSet without INPUT_VERSIONS column
    ResultSet noInputVersionsRS = mock(ResultSet.class);
    ResultSetMetaData metaData = mock(ResultSetMetaData.class);
    when(noInputVersionsRS.getMetaData()).thenReturn(metaData);

    when(metaData.getColumnCount()).thenReturn(16);
    setupColumnNamesWithoutInputVersions(metaData);

    copyBasicMocksToResultSet(noInputVersionsRS);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(noInputVersionsRS, mock(StatementContext.class));

    assertThat(result).isNotNull();
    assertThat(result.getInputDatasetVersions()).isEmpty();
  }

  @Test
  void testMapRunWithDepthZero() throws SQLException {
    setupCompleteRunMocks();
    when(resultSet.getInt("depth")).thenReturn(0);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getDepth()).isEqualTo(0);
  }

  @Test
  void testMapRunWithHighDepth() throws SQLException {
    setupCompleteRunMocks();
    when(resultSet.getInt("depth")).thenReturn(10);

    RunDataMapper mapper = new RunDataMapper();
    RunData result = mapper.map(resultSet, mock(StatementContext.class));

    assertThat(result.getDepth()).isEqualTo(10);
  }

  private static void setupCompleteRunMocks() throws SQLException {
    UUID runUuid = UUID.randomUUID();
    UUID jobUuid = UUID.randomUUID();
    UUID jobVersionUuid = UUID.randomUUID();

    when(resultSet.getObject(Columns.ROW_UUID)).thenReturn(runUuid);
    when(resultSet.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(runUuid);

    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));

    when(resultSet.getTimestamp(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));
    when(resultSet.getObject(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));

    when(resultSet.getTimestamp(Columns.STARTED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:05:00"));
    when(resultSet.getObject(Columns.STARTED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:05:00"));

    when(resultSet.getTimestamp(Columns.ENDED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:30:00"));
    when(resultSet.getObject(Columns.ENDED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:30:00"));

    when(resultSet.getString(Columns.STATE)).thenReturn("COMPLETED");
    when(resultSet.getObject(Columns.STATE)).thenReturn("COMPLETED");

    when(resultSet.getObject(Columns.JOB_UUID)).thenReturn(jobUuid);
    when(resultSet.getObject(Columns.JOB_UUID, UUID.class)).thenReturn(jobUuid);

    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(resultSet.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");

    when(resultSet.getString(Columns.JOB_NAME)).thenReturn("test-job");
    when(resultSet.getObject(Columns.JOB_NAME)).thenReturn("test-job");

    when(resultSet.getObject(Columns.JOB_VERSION_UUID)).thenReturn(jobVersionUuid);
    when(resultSet.getObject(Columns.JOB_VERSION_UUID, UUID.class)).thenReturn(jobVersionUuid);

    Array inputUuids = mock(Array.class);
    when(inputUuids.getArray()).thenReturn(new UUID[] {UUID.randomUUID()});
    when(resultSet.getArray("input_uuids")).thenReturn(inputUuids);
    when(resultSet.getObject("input_uuids")).thenReturn(inputUuids);

    Array outputUuids = mock(Array.class);
    when(outputUuids.getArray()).thenReturn(new UUID[] {UUID.randomUUID()});
    when(resultSet.getArray("output_uuids")).thenReturn(outputUuids);
    when(resultSet.getObject("output_uuids")).thenReturn(outputUuids);

    when(resultSet.getInt("depth")).thenReturn(2);

    Array childRunIds = mock(Array.class);
    when(childRunIds.getArray()).thenReturn(new UUID[0]);
    when(resultSet.getArray("child_run_id")).thenReturn(childRunIds);
    when(resultSet.getObject("child_run_id")).thenReturn(childRunIds);

    Array parentRunIds = mock(Array.class);
    when(parentRunIds.getArray()).thenReturn(new UUID[0]);
    when(resultSet.getArray("parent_run_id")).thenReturn(parentRunIds);
    when(resultSet.getObject("parent_run_id")).thenReturn(parentRunIds);

    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getString(Columns.OUTPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(null);
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);
  }

  private static void setupMinimalRunMocks() throws SQLException {
    UUID runUuid = UUID.randomUUID();
    UUID jobUuid = UUID.randomUUID();

    when(resultSet.getObject(Columns.ROW_UUID)).thenReturn(runUuid);
    when(resultSet.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(runUuid);

    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));

    when(resultSet.getTimestamp(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));
    when(resultSet.getObject(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));

    when(resultSet.getTimestamp(Columns.STARTED_AT)).thenReturn(null);
    when(resultSet.getObject(Columns.STARTED_AT)).thenReturn(null);

    when(resultSet.getTimestamp(Columns.ENDED_AT)).thenReturn(null);
    when(resultSet.getObject(Columns.ENDED_AT)).thenReturn(null);

    when(resultSet.getString(Columns.STATE)).thenReturn("NEW");
    when(resultSet.getObject(Columns.STATE)).thenReturn("NEW");

    when(resultSet.getObject(Columns.JOB_UUID)).thenReturn(jobUuid);
    when(resultSet.getObject(Columns.JOB_UUID, UUID.class)).thenReturn(jobUuid);

    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn(null);
    when(resultSet.getString(Columns.JOB_NAME)).thenReturn(null);
    when(resultSet.getObject(Columns.JOB_VERSION_UUID)).thenReturn(null);

    Array emptyArray = mock(Array.class);
    when(emptyArray.getArray()).thenReturn(new UUID[0]);
    when(resultSet.getArray("input_uuids")).thenReturn(emptyArray);
    when(resultSet.getArray("output_uuids")).thenReturn(emptyArray);
    when(resultSet.getArray("child_run_id")).thenReturn(emptyArray);
    when(resultSet.getArray("parent_run_id")).thenReturn(emptyArray);

    when(resultSet.getInt("depth")).thenReturn(0);

    when(resultSet.getString(Columns.INPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getString(Columns.OUTPUT_VERSIONS)).thenReturn(null);
    when(resultSet.getObject(Columns.DATASET_FACETS)).thenReturn(null);
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);
  }

  private static void setupColumnNames(ResultSetMetaData metaData) throws SQLException {
    when(metaData.getColumnName(1)).thenReturn(Columns.ROW_UUID);
    when(metaData.getColumnName(2)).thenReturn(Columns.CREATED_AT);
    when(metaData.getColumnName(3)).thenReturn(Columns.UPDATED_AT);
    when(metaData.getColumnName(4)).thenReturn(Columns.STARTED_AT);
    when(metaData.getColumnName(5)).thenReturn(Columns.ENDED_AT);
    when(metaData.getColumnName(6)).thenReturn(Columns.STATE);
    when(metaData.getColumnName(7)).thenReturn(Columns.JOB_UUID);
    when(metaData.getColumnName(8)).thenReturn(Columns.NAMESPACE_NAME);
    when(metaData.getColumnName(9)).thenReturn(Columns.JOB_NAME);
    when(metaData.getColumnName(10)).thenReturn(Columns.JOB_VERSION_UUID);
    when(metaData.getColumnName(11)).thenReturn("input_uuids");
    when(metaData.getColumnName(12)).thenReturn("output_uuids");
    when(metaData.getColumnName(13)).thenReturn("depth");
    when(metaData.getColumnName(14)).thenReturn(Columns.INPUT_VERSIONS);
    when(metaData.getColumnName(15)).thenReturn(Columns.OUTPUT_VERSIONS);
    when(metaData.getColumnName(16)).thenReturn("child_run_id");
    when(metaData.getColumnName(17)).thenReturn("parent_run_id");
    when(metaData.getColumnName(18)).thenReturn(Columns.DATASET_FACETS);
    when(metaData.getColumnName(19)).thenReturn(Columns.FACETS);
    when(metaData.getColumnName(20)).thenReturn("extra");
  }

  private static void setupColumnNamesWithoutInputVersions(ResultSetMetaData metaData)
      throws SQLException {
    when(metaData.getColumnName(1)).thenReturn(Columns.ROW_UUID);
    when(metaData.getColumnName(2)).thenReturn(Columns.CREATED_AT);
    when(metaData.getColumnName(3)).thenReturn(Columns.UPDATED_AT);
    when(metaData.getColumnName(4)).thenReturn(Columns.STARTED_AT);
    when(metaData.getColumnName(5)).thenReturn(Columns.ENDED_AT);
    when(metaData.getColumnName(6)).thenReturn(Columns.STATE);
    when(metaData.getColumnName(7)).thenReturn(Columns.JOB_UUID);
    when(metaData.getColumnName(8)).thenReturn(Columns.NAMESPACE_NAME);
    when(metaData.getColumnName(9)).thenReturn(Columns.JOB_NAME);
    when(metaData.getColumnName(10)).thenReturn(Columns.JOB_VERSION_UUID);
    when(metaData.getColumnName(11)).thenReturn("input_uuids");
    when(metaData.getColumnName(12)).thenReturn("output_uuids");
    when(metaData.getColumnName(13)).thenReturn("depth");
    when(metaData.getColumnName(14)).thenReturn("child_run_id");
    when(metaData.getColumnName(15)).thenReturn("parent_run_id");
    when(metaData.getColumnName(16)).thenReturn("uuid");
  }

  private static void copyBasicMocksToResultSet(ResultSet target) throws SQLException {
    UUID runUuid = UUID.randomUUID();
    UUID jobUuid = UUID.randomUUID();

    when(target.getObject(Columns.ROW_UUID)).thenReturn(runUuid);
    when(target.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(runUuid);

    when(target.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));
    when(target.getObject(Columns.CREATED_AT)).thenReturn(Timestamp.valueOf("2024-01-01 10:00:00"));

    when(target.getTimestamp(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));
    when(target.getObject(Columns.UPDATED_AT)).thenReturn(Timestamp.valueOf("2024-01-01 11:00:00"));

    when(target.getTimestamp(Columns.STARTED_AT)).thenReturn(null);
    when(target.getObject(Columns.STARTED_AT)).thenReturn(null);

    when(target.getTimestamp(Columns.ENDED_AT)).thenReturn(null);
    when(target.getObject(Columns.ENDED_AT)).thenReturn(null);

    when(target.getString(Columns.STATE)).thenReturn("NEW");
    when(target.getObject(Columns.STATE)).thenReturn("NEW");

    when(target.getObject(Columns.JOB_UUID)).thenReturn(jobUuid);
    when(target.getObject(Columns.JOB_UUID, UUID.class)).thenReturn(jobUuid);

    when(target.getString(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(target.getObject(Columns.NAMESPACE_NAME)).thenReturn("test-namespace");
    when(target.getString(Columns.JOB_NAME)).thenReturn("test-job");
    when(target.getObject(Columns.JOB_NAME)).thenReturn("test-job");
    when(target.getObject(Columns.JOB_VERSION_UUID)).thenReturn(null);
    when(target.getObject(Columns.JOB_VERSION_UUID, UUID.class)).thenReturn(null);

    Array emptyArray = mock(Array.class);
    when(emptyArray.getArray()).thenReturn(new UUID[0]);
    when(target.getArray("input_uuids")).thenReturn(emptyArray);
    when(target.getObject("input_uuids")).thenReturn(emptyArray);
    when(target.getArray("output_uuids")).thenReturn(emptyArray);
    when(target.getObject("output_uuids")).thenReturn(emptyArray);
    when(target.getArray("child_run_id")).thenReturn(emptyArray);
    when(target.getObject("child_run_id")).thenReturn(emptyArray);
    when(target.getArray("parent_run_id")).thenReturn(emptyArray);
    when(target.getObject("parent_run_id")).thenReturn(emptyArray);

    when(target.getInt("depth")).thenReturn(0);

    // Add uuid column mock
    UUID uuid = UUID.randomUUID();
    when(target.getObject("uuid")).thenReturn(uuid);
    when(target.getObject("uuid", UUID.class)).thenReturn(uuid);
  }
}
