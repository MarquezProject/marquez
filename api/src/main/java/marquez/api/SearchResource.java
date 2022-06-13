/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.db.SearchDao;

@Slf4j
@Path("/api/v1/search")
public class SearchResource {
  private static final String DEFAULT_SORT = "name";
  private static final String DEFAULT_LIMIT = "10";
  private static final int MIN_LIMIT = 0;

  private final SearchDao searchDao;

  public SearchResource(@NonNull final SearchDao searchDao) {
    this.searchDao = searchDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response search(
      @QueryParam("q") @NotNull String query,
      @QueryParam("filter") @Nullable SearchFilter filter,
      @QueryParam("sort") @DefaultValue(DEFAULT_SORT) SearchSort sort,
      @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) @Min(MIN_LIMIT) int limit) {
    return Response.ok(
            isQueryBlank(query)
                ? SearchResults.EMPTY
                : searchWithNonBlankQuery(query, filter, sort, limit))
        .build();
  }

  private static boolean isQueryBlank(@NonNull String query) {
    return query.trim().isEmpty();
  }

  private SearchResults searchWithNonBlankQuery(
      String query, SearchFilter filter, SearchSort sort, int limit) {
    final List<SearchResult> results = searchDao.search(query, filter, sort, limit);
    return new SearchResults(results);
  }

  /** Wrapper for {@link SearchResult}s which also contains a {@code total count}. */
  @ToString
  public static final class SearchResults {
    @Getter private final int totalCount;
    @Getter private final List<SearchResult> results;

    @JsonCreator
    public SearchResults(@NonNull final List<SearchResult> results) {
      this.totalCount = results.size();
      this.results = results;
    }

    static final SearchResults EMPTY = new SearchResults(List.of());
  }
}
