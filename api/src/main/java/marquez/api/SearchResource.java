/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static marquez.common.Utils.toLocateDateOrNull;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
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
  private static final String YYYY_MM_DD = "^\\d{4}-\\d{2}-\\d{2}$";
  private static final String DEFAULT_SORT = "name";
  private static final String DEFAULT_LIMIT = "10";
  private static final int MIN_LIMIT = 0;

  private final SearchDao searchDao;
  private final ElasticsearchClient elasticsearchClient;

  public SearchResource(
      @NonNull final SearchDao searchDao, @Nullable final ElasticsearchClient elasticsearchClient) {
    this.searchDao = searchDao;
    this.elasticsearchClient = elasticsearchClient;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
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

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/jobs")
  public Response searchJobs(@QueryParam("q") @NotBlank String query) throws IOException {
    if (this.elasticsearchClient != null) {
      String[] fields = {
        "facets.sql.query",
        "facets.sourceCode.sourceCode",
        "facets.sourceCode.language",
        "runFacets.processing_engine.name",
        "run_id",
        "name",
        "namespace",
        "type"
      };
      SearchResponse<ObjectNode> response =
          this.elasticsearchClient.search(
              s -> {
                s.index("jobs")
                    .query(
                        q ->
                            q.multiMatch(
                                m ->
                                    m.query(query)
                                        .type(TextQueryType.PhrasePrefix)
                                        .fields(Arrays.stream(fields).toList())
                                        .operator(Operator.Or)));
                s.highlight(
                    hl -> {
                      for (String field : fields) {
                        hl.fields(field, f -> f.type("plain"));
                      }
                      return hl;
                    });
                return s;
              },
              ObjectNode.class);

      return formatEsResponse(response);
    } else {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/datasets")
  public Response searchDatasets(@QueryParam("q") @NotBlank String query) throws IOException {
    if (this.elasticsearchClient != null) {
      String[] fields = {
        "run_id",
        "name",
        "namespace",
        "facets.schema.fields.name",
        "facets.schema.fields.type",
        "facets.columnLineage.fields.*.inputFields.name",
        "facets.columnLineage.fields.*.inputFields.namespace",
        "facets.columnLineage.fields.*.inputFields.field",
        "facets.columnLineage.fields.*.transformationDescription",
        "facets.columnLineage.fields.*.transformationType"
      };
      SearchResponse<ObjectNode> response =
          this.elasticsearchClient.search(
              s ->
                  s.index("datasets")
                      .query(
                          q ->
                              q.multiMatch(
                                  m -> m.query(query).fields(Arrays.stream(fields).toList())))
                      .highlight(
                          hl -> {
                            for (String field : fields) {
                              hl.fields(field, f -> f.type("plain"));
                            }
                            return hl;
                          }),
              ObjectNode.class);

      return formatEsResponse(response);
    } else {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
  }

  private Response formatEsResponse(SearchResponse<ObjectNode> response) {
    List<ObjectNode> hits =
        response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    List<Map<String, List<String>>> highlights =
        response.hits().hits().stream().map(Hit::highlight).collect(Collectors.toList());

    return Response.ok(new EsResult(hits, highlights)).build();
  }

  @ToString
  public static final class EsResult {
    @Getter private final List<ObjectNode> hits;
    @Getter private final List<Map<String, List<String>>> highlights;

    @JsonCreator
    public EsResult(
        @NonNull List<ObjectNode> hits, @NonNull List<Map<String, List<String>>> highlights) {
      this.hits = hits;
      this.highlights = highlights;
    }
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
