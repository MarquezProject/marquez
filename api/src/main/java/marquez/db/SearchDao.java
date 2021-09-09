package marquez.db;

import java.util.List;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.db.mappers.SearchResultMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

/** The DAO for {@code JobVersion}. */
@RegisterRowMapper(SearchResultMapper.class)
public interface SearchDao {
  /**
   * @param query
   * @param filter
   * @param sort
   * @param limit
   * @return
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
          + ") AS result\n"
          + "WHERE type = :filter OR CAST(:filter AS TEXT) IS NULL\n"
          + "ORDER BY :sort\n"
          + "LIMIT :limit")
  List<SearchResult> search(String query, SearchFilter filter, SearchSort sort, int limit);
}
