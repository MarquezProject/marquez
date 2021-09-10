/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
          + "  SELECT 'JOB' AS type, j.name, j.updated_at, j.namespace_name\n"
          + "    FROM jobs AS j\n"
          + "   WHERE  j.name ilike '%' || :query || '%'\n"
          + ") AS results\n"
          + "WHERE type = :filter OR CAST(:filter AS TEXT) IS NULL\n"
          + "ORDER BY :sort\n"
          + "LIMIT :limit")
  List<SearchResult> search(String query, SearchFilter filter, SearchSort sort, int limit);
}
