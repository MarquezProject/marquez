/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static marquez.common.Utils.toLocateDateOrNull;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
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
  private static final String YYYY_MM_DD = "^\\d{4}-\\d{2}-\\d{2}$";
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
  @Produces(MediaType.APPLICATION_JSON)
  public Response search(
      @QueryParam("q") @NotBlank String query,
      @QueryParam("filter") @Nullable SearchFilter filter,
      @QueryParam("sort") @DefaultValue(DEFAULT_SORT) SearchSort sort,
      @QueryParam("limit") @DefaultValue(DEFAULT_LIMIT) @Min(MIN_LIMIT) int limit,
      @QueryParam("namespace") @Nullable String namespace,
      @QueryParam("before") @Valid @Pattern(regexp = YYYY_MM_DD) @Nullable String before,
      @QueryParam("after") @Valid @Pattern(regexp = YYYY_MM_DD) @Nullable String after) {
    final List<SearchResult> searchResults =
        searchDao.search(
            query,
            filter,
            sort,
            limit,
            namespace,
            toLocateDateOrNull(before),
            toLocateDateOrNull(after));
    return Response.ok(new SearchResults(searchResults)).build();
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
  }
}
