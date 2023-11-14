/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.uuidOrNull;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.service.models.JobFacets;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class JobFacetsMapper implements RowMapper<JobFacets> {

  @Override
  public JobFacets map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobFacets(uuidOrNull(results, "run_uuid"), toFacetsOrNull(results, "facets"));
  }
}
