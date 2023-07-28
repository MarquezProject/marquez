/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.LocalDate;
import java.util.List;
import javax.annotation.Nullable;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.db.mappers.SearchResultMapper;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/** The DAO for {@link SearchResult}. */
@RegisterRowMapper(SearchResultMapper.class)
public interface SearchDao extends SqlObject {

  default List<SearchResult> search(String query, SearchFilter filter, SearchSort sort, int limit) {
    return search(query, filter, sort, limit, null, null, null);
  }

  default List<SearchResult> search(
      String query, SearchFilter filter, SearchSort sort, int limit, String namespace) {
    return search(query, filter, sort, limit, namespace, null, null);
  }

  default List<SearchResult> search(
      String query, SearchFilter filter, SearchSort sort, int limit, LocalDate before) {
    return search(query, filter, sort, limit, null, before, null);
  }

  default List<SearchResult> search(
      String query,
      SearchFilter filter,
      SearchSort sort,
      int limit,
      LocalDate before,
      LocalDate after) {
    return search(query, filter, sort, limit, null, before, after);
  }

  /**
   * Returns all datasets and jobs that match the provided query; matching of datasets and jobs are
   * string based and case-insensitive.
   *
   * @param query Query containing pattern to match.
   * @param filter The filter to apply to the query result.
   * @param sort The sort to apply to the query result.
   * @param limit The limit to apply to the query result.
   * @param namespace Match jobs or datasets within the given namespace.
   * @param before Match jobs or datasets before YYYY-MM-DD.
   * @param after Match jobs or datasets after YYYY-MM-DD.
   * @return A {@link SearchResult} object.
   */
  @SqlQuery(
      """
          SELECT type, name, updated_at, namespace_name
                FROM (
                  SELECT 'DATASET' AS type, d.name, d.updated_at, d.namespace_name
                    FROM datasets_view AS d
                   WHERE (d.namespace_name = :namespace OR CAST(:namespace AS TEXT) IS NULL)
                     AND (d.updated_at < :before OR CAST(:before AS TEXT) IS NULL)
                     AND (d.updated_at > :after OR CAST(:after AS TEXT) IS NULL)
                     AND (d.name ILIKE '%' || :query || '%')
                   UNION
                  SELECT DISTINCT ON (j.namespace_name, j.name)
                    'JOB' AS type, j.name, j.updated_at, j.namespace_name
                    FROM (SELECT namespace_name, name, UNNEST(COALESCE(aliases, Array[NULL]::varchar[])) AS alias, updated_at
                           FROM jobs_view WHERE symlink_target_uuid IS NULL
                           ORDER BY updated_at DESC) AS j
                   WHERE (j.namespace_name = :namespace OR CAST(:namespace AS TEXT) IS NULL)
                     AND (j.updated_at < :before OR CAST(:before AS TEXT) IS NULL)
                     AND (j.updated_at > :after OR CAST(:after AS TEXT) IS NULL)
                     AND (j.name ILIKE '%' || :query || '%' OR j.alias ILIKE '%' || :query || '%')
                ) AS results
                  WHERE type = :filter OR CAST(:filter AS TEXT) IS NULL
                  ORDER BY :sort
                  LIMIT :limit""")
  List<SearchResult> search(
      String query,
      SearchFilter filter,
      SearchSort sort,
      int limit,
      @Nullable String namespace,
      @Nullable LocalDate before,
      @Nullable LocalDate after);
}
