/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.db.Columns;
import marquez.db.models.JobRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public final class JobRowMapper implements RowMapper<JobRow> {
  public static final ObjectMapper mapper = Utils.getMapper();

  @Override
  public JobRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new JobRow(
        uuidOrThrow(results, Columns.ROW_UUID),
        stringOrThrow(results, Columns.TYPE),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        stringOrThrow(results, Columns.NAMESPACE_NAME),
        stringOrThrow(results, Columns.NAME),
        stringOrThrow(results, Columns.SIMPLE_NAME),
        stringOrNull(results, Columns.PARENT_JOB_NAME),
        stringOrNull(results, Columns.DESCRIPTION),
        uuidOrNull(results, Columns.CURRENT_VERSION_UUID),
        uuidOrNull(results, "current_job_context_uuid"),
        stringOrNull(results, "current_location"),
        getDatasetFromJsonOrNull(results, "current_inputs"),
        uuidOrNull(results, Columns.SYMLINK_TARGET_UUID));
  }

  Set<DatasetId> getDatasetFromJsonOrNull(@NonNull ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    PGobject pgObject = (PGobject) results.getObject(column);
    try {
      return mapper.readValue(pgObject.getValue(), new TypeReference<Set<DatasetId>>() {});
    } catch (JsonProcessingException e) {
      log.error(String.format("Could not read dataset from job row %s", column), e);
      return new HashSet<>();
    }
  }
}
