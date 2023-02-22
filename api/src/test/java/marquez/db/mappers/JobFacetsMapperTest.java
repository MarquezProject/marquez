/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;
import marquez.common.Utils;
import marquez.db.Columns;
import marquez.service.models.JobFacets;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.postgresql.util.PGobject;

class JobFacetsMapperTest {

  private static ResultSet resultSet;

  @BeforeAll
  public static void setUp() throws SQLException {
    resultSet = mock(ResultSet.class);
    when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
    when(resultSet.getObject("run_uuid"))
        .thenReturn(UUID.fromString("a32f2800-7782-3ce3-b77e-eeeeaded3cf3"));
    when(resultSet.getObject("run_uuid", UUID.class))
        .thenReturn(UUID.fromString("a32f2800-7782-3ce3-b77e-eeeeaded3cf3"));
    PGobject facets = new PGobject();
    String sql =
        """
          [{
            "documentation": {
              "description": "This is test description"
            },
            "sql": {
              "query": "SELECT 1 AS test"
            }
          }]
          """;
    facets.setValue(sql);
    when(resultSet.getObject("facets")).thenReturn(facets);
    when(resultSet.getString("facets")).thenReturn(facets.toString());
  }

  @Test
  public void shouldMapFullJobFacets() throws SQLException {
    JobFacetsMapper underTest = new JobFacetsMapper();

    try (MockedStatic<Columns> mocked =
        Mockito.mockStatic(Columns.class, Mockito.CALLS_REAL_METHODS)) {
      when(Columns.exists(resultSet, "facets")).thenReturn(true);

      JobFacets actualJobFacets = underTest.map(resultSet, mock(StatementContext.class));
      JobFacets expectedJobFacets =
          Utils.fromJson(
              this.getClass().getResourceAsStream("/mappers/job_facets_mapper.json"),
              new TypeReference<JobFacets>() {});

      assertEquals(expectedJobFacets, actualJobFacets);
    }
  }
}
