/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import marquez.common.models.ElasticResponse;
import marquez.db.SearchDao;

@Slf4j
@Path("/api/v1/search")
public class SearchResource {
  private static final String DEFAULT_SORT = "name";
  private static final String DEFAULT_LIMIT = "10";
  private static final int MIN_LIMIT = 0;

  private final SearchDao searchDao;

  private final ElasticsearchClient elasticsearchClient;

  public SearchResource(
      @NonNull final SearchDao searchDao, @Nullable ElasticsearchClient elasticsearchClient) {
    this.searchDao = searchDao;
    this.elasticsearchClient = elasticsearchClient;
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

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/elastic/{text}")
  public Response searchElastic(@PathParam("text") @NotNull String text) throws IOException {
    if (this.elasticsearchClient != null) {
      // datasets search
      SearchResponse<ObjectNode> datasetsResponse =
          this.elasticsearchClient.search(
              s ->
                  s.index("datasets")
                      .query(
                          q ->
                              q.bool(
                                  b ->
                                      b.should(sh -> sh.match(m -> m.field("name").query(text)))
                                          .should(
                                              sh1 ->
                                                  sh1.match(
                                                      m1 ->
                                                          m1.field("facets.schema.fields.name")
                                                              .query(text)))
                                          .should(
                                              sh2 ->
                                                  sh2.match(m2 -> m2.field("name").query(text))))),
              ObjectNode.class);

      // jobs search
      SearchResponse<ObjectNode> jobsResponse = this.elasticsearchClient.search(s -> s
                      .index("jobs")

                      .query(q -> q
                              .match(t -> t
                                      .field("job")
                                      .query(text)
                              )
                      ).query(q -> q
                              .match(t -> t
                                      .field("namespace")
                                      .query(text)
                              )
                      ),
              ObjectNode.class
      );


      List<Hit<ObjectNode>> datasetsHits = datasetsResponse.hits().hits();
      List<Hit<ObjectNode>> jobHits = jobsResponse.hits().hits();
      ElasticResponse elasticResponse = new ElasticResponse(datasetsHits.stream().map(Hit::source).collect(Collectors.toList()), jobHits.stream().map(Hit::source).collect(Collectors.toList()));
      return Response.ok(elasticResponse).build();
    }
    return Response.status(400).build();
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
