/*
 * Copyright 2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */
package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.LineageDao.SimpleLineageEdge;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class SimpleLineageEdgeMapper implements RowMapper<SimpleLineageEdge> {
  @Override
  public SimpleLineageEdge map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    JobId job1 =
        JobId.of(
            NamespaceName.of(stringOrThrow(results, "job_namespace")),
            JobName.of(stringOrThrow(results, "job_name")));
    String io1 = stringOrNull(results, "io1");
    String ds_namespace = stringOrNull(results, "ds_namespace");
    DatasetId ds =
        ds_namespace == null
            ? null
            : new DatasetId(
                NamespaceName.of(ds_namespace), DatasetName.of(stringOrNull(results, "ds_name")));
    String io2 = stringOrNull(results, "io2");
    String job2_namespace = stringOrNull(results, "job2_namespace");
    JobId job2 =
        job2_namespace == null
            ? null
            : JobId.of(
                NamespaceName.of(job2_namespace), JobName.of(stringOrThrow(results, "job2_name")));
    String job2parent_namespace = stringOrNull(results, "job2_parent_namespace");
    JobId job2parent =
        job2parent_namespace == null
            ? null
            : JobId.of(
                NamespaceName.of(job2parent_namespace),
                JobName.of(stringOrThrow(results, "job2_parent_name")));
    return new SimpleLineageEdge(job1, io1, ds, io2, job2, job2parent);
  }
}
