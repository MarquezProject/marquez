/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.mapOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.urlOrNull;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.type.TypeReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.JobVersion;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;
import marquez.db.Columns;
import marquez.service.models.Run;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Convert a database row to a {@link JobVersion}. For the {@link JobVersion#latestRun}, we delegate
 * to the {@link RunMapper} with a specified prefix of {@value #RUN_COLUMN_PREFIX}, meaning all
 * run-related columns should be prefixed in the SQL query. This avoids conflicts between common
 * column names, such as created_at, uuid, and context.
 */
@Slf4j
public class JobVersionMapper implements RowMapper<JobVersion> {

  public static final String RUN_COLUMN_PREFIX = "run_";
  private final RunMapper runMapper = new RunMapper(RUN_COLUMN_PREFIX);

  @Override
  public JobVersion map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());
    Run latestRun = null;
    if (columnNames.contains(Columns.RUN_UUID) && results.getString(Columns.RUN_UUID) != null) {
      latestRun = runMapper.map(results, context);
    }
    return new JobVersion(
        new JobVersionId(
            NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
            JobName.of(stringOrThrow(results, Columns.JOB_NAME)),
            uuidOrThrow(results, Columns.VERSION)),
        JobName.of(stringOrThrow(results, Columns.JOB_NAME)),
        timestampOrThrow(results, Columns.CREATED_AT),
        Version.of(uuidOrThrow(results, Columns.VERSION)),
        urlOrNull(results, Columns.LOCATION),
        mapOrNull(results, Columns.CONTEXT),
        toDatasetIdsList(results, Columns.INPUT_DATASETS),
        toDatasetIdsList(results, Columns.OUTPUT_DATASETS),
        latestRun);
  }

  private List<DatasetId> toDatasetIdsList(ResultSet rs, String column) throws SQLException {
    if (rs.getString(column) == null) {
      return Collections.emptyList();
    }
    return Utils.fromJson(rs.getString(column), new TypeReference<List<DatasetId>>() {});
  }
}
