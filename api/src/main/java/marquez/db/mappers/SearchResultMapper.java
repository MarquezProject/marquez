/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.mappers;

import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.api.models.SearchResult;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/** Convert a search results to a {@link SearchResult}. */
public final class SearchResultMapper implements RowMapper<SearchResult> {
  @Override
  public SearchResult map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    final SearchResult.ResultType type =
        SearchResult.ResultType.valueOf(stringOrThrow(results, Columns.TYPE));
    switch (type) {
      case DATASET:
        return SearchResult.newDatasetResult(
            DatasetName.of(stringOrThrow(results, Columns.NAME)),
            timestampOrThrow(results, Columns.UPDATED_AT),
            NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)));
      case JOB:
        return SearchResult.newJobResult(
            JobName.of(stringOrThrow(results, Columns.NAME)),
            timestampOrThrow(results, Columns.UPDATED_AT),
            NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)));
      default:
        throw new IllegalArgumentException(String.format("search type '%s' not supported", type));
    }
  }
}
