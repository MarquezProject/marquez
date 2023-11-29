/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.Columns;
import marquez.db.LineageDao.DatasetSummary;
import marquez.db.LineageDao.JobSummary;
import marquez.db.LineageDao.RunSummary;
import marquez.db.LineageDao.UpstreamRunRow;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/** Maps the upstream query result set to a UpstreamRunRow */
public final class UpstreamRunRowMapper implements RowMapper<UpstreamRunRow> {
  @Override
  public UpstreamRunRow map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new UpstreamRunRow(
        new JobSummary(
            new NamespaceName(stringOrThrow(results, "job_namespace")),
            new JobName(stringOrThrow(results, "job_name")),
            Optional.ofNullable(stringOrNull(results, "job_version_uuid"))
                .map(UUID::fromString)
                .orElse(null)),
        new RunSummary(
            new RunId(uuidOrThrow(results, "r_uuid")),
            timestampOrNull(results, Columns.STARTED_AT),
            timestampOrNull(results, Columns.ENDED_AT),
            stringOrThrow(results, Columns.STATE)),
        results.getObject("dataset_name") == null
            ? null
            : new DatasetSummary(
                new NamespaceName(stringOrThrow(results, "dataset_namespace")),
                new DatasetName(stringOrThrow(results, "dataset_name")),
                UUID.fromString(stringOrThrow(results, "dataset_version_uuid")),
                new RunId(UUID.fromString(stringOrThrow(results, "u_r_uuid")))));
  }
}
