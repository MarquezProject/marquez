/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.util.List;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.db.mappers.SearchResultMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/** The DAO for {@link SearchResult}. */
@RegisterRowMapper(SearchResultMapper.class)
public interface SearchDao {
  /**
   * Returns all datasets and jobs that match the provided query; matching of datasets and jobs are
   * string based and case-insensitive.
   *
   * @param query Query containing pattern to match.
   * @param filter The filter to apply to the query result.
   * @param sort The sort to apply to the query result.
   * @param limit The limit to apply to the query result.
   * @return A {@link SearchResult} object.
   */
  @SqlQuery(
      "SELECT type, name, updated_at, namespace_name\n"
          + "FROM (\n"
          + "  SELECT 'DATASET' AS type, d.name, d.updated_at, d.namespace_name\n"
          + "    FROM datasets AS d\n"
          + "   WHERE  d.name ilike '%' || :query || '%'\n"
          + "   UNION\n"
          + "  SELECT DISTINCT ON (j.namespace_name, j.name) \n"
          + "    'JOB' AS type, j.name, j.updated_at, j.namespace_name\n"
          + "    FROM (SELECT namespace_name, name, unnest(COALESCE(aliases, Array[NULL]::varchar[])) AS alias, updated_at \n"
          + "           FROM jobs_view WHERE symlink_target_uuid IS NULL\n"
          + "           ORDER BY updated_at DESC) AS j\n"
          + "   WHERE  j.name ilike '%' || :query || '%'\n"
          + "   OR j.alias ilike '%' || :query || '%'\n"
          + ") AS results\n"
          + "WHERE type = :filter OR CAST(:filter AS TEXT) IS NULL\n"
          + "ORDER BY :sort\n"
          + "LIMIT :limit")
  List<SearchResult> search(String query, SearchFilter filter, SearchSort sort, int limit);
}
