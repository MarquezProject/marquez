/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.util.UUID;
import marquez.common.Utils;
import marquez.db.Columns;
import marquez.service.models.Job;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

class JobMapperTest {

  private static ResultSet resultSet;
  private static TimeZone defaultTZ = TimeZone.getDefault();

  @BeforeAll
  public static void setUp() throws SQLException, MalformedURLException {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    resultSet = mock(ResultSet.class);
    when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
    when(resultSet.getString(Columns.NAMESPACE_NAME)).thenReturn("NAMESPACE");
    when(resultSet.getObject(Columns.NAMESPACE_NAME)).thenReturn("NAMESPACE");
    when(resultSet.getString(Columns.NAME)).thenReturn("NAME");
    when(resultSet.getObject(Columns.NAME)).thenReturn("NAME");
    when(resultSet.getString(Columns.SIMPLE_NAME)).thenReturn("SIMPLE_NAME");
    when(resultSet.getObject(Columns.SIMPLE_NAME)).thenReturn("SIMPLE_NAME");
    when(resultSet.getString(Columns.TYPE)).thenReturn("BATCH");
    when(resultSet.getObject(Columns.TYPE)).thenReturn("BATCH");
    when(resultSet.getString(Columns.DESCRIPTION)).thenReturn("DESCRIPTION");
    when(resultSet.getObject(Columns.DESCRIPTION)).thenReturn("DESCRIPTION");
    when(resultSet.getTimestamp(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2000-01-01 00:00:01"));
    when(resultSet.getObject(Columns.CREATED_AT))
        .thenReturn(Timestamp.valueOf("2000-01-01 00:00:01"));
    when(resultSet.getTimestamp(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2000-01-02 00:00:01"));
    when(resultSet.getObject(Columns.UPDATED_AT))
        .thenReturn(Timestamp.valueOf("2000-01-02 00:00:01"));
    when(resultSet.getObject(Columns.CURRENT_VERSION_UUID))
        .thenReturn(UUID.fromString("b1d626a2-6d3a-475e-9ecf-943176d4a8c6"));
    when(resultSet.getObject(Columns.CURRENT_VERSION_UUID, UUID.class))
        .thenReturn(UUID.fromString("b1d626a2-6d3a-475e-9ecf-943176d4a8c6"));
    when(resultSet.getString("current_location")).thenReturn("https://github.com/");
    when(resultSet.getObject("current_location")).thenReturn("https://github.com/");
    when(resultSet.getString(Columns.CONTEXT)).thenReturn("{ \"test\" : \"value\"}");
    when(resultSet.getObject(Columns.CONTEXT)).thenReturn("{ \"test\" : \"value\"}");
    when(resultSet.getString(Columns.FACETS)).thenReturn(null);
    when(resultSet.getObject(Columns.FACETS)).thenReturn(null);
    PGobject inputs = new PGobject();
    inputs.setValue(
        "[{\n"
            + "    \"namespace\": \"test-namespace\",\n"
            + "    \"name\": \"test-dataset\"\n"
            + "  }]");
    when(resultSet.getObject("current_inputs")).thenReturn(inputs);
  }

  @AfterAll
  public static void reset() {
    TimeZone.setDefault(defaultTZ);
  }

  @Test
  void shouldMapFullJob() throws SQLException {
    JobMapper underTest = new JobMapper();
    Job expected =
        Utils.fromJson(
            this.getClass().getResourceAsStream("/mappers/full_job_mapper.json"),
            new TypeReference<Job>() {});

    Job actual = underTest.map(resultSet, mock(StatementContext.class));
    assertThat(actual).isEqualTo(expected);
  }
}
