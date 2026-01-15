/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.v2beta;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.service.SearchService;
import marquez.service.ServiceFactory;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

@Slf4j
@Path("/api/v2beta/search")
public class SearchResource {

  private final SearchService searchService;

  public SearchResource(@NonNull final ServiceFactory serviceFactory) {
    this.searchService = serviceFactory.getSearchService();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("jobs")
  public Response searchJobs(@QueryParam("q") @NotBlank String query) throws IOException {
    if (!searchService.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
    return formatOpenSearchResponse(this.searchService.searchJobs(query));
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("datasets")
  public Response searchDatasets(@QueryParam("q") @NotBlank String query) throws IOException {
    if (!searchService.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
    return formatOpenSearchResponse(this.searchService.searchDatasets(query));
  }

  private Response formatOpenSearchResponse(SearchResponse<ObjectNode> response) {
    List<ObjectNode> hits =
        response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    List<Map<String, List<String>>> highlights =
        response.hits().hits().stream().map(Hit::highlight).collect(Collectors.toList());

    return Response.ok(new OpenSearchResult(hits, highlights)).build();
  }

  @ToString
  public static final class OpenSearchResult {
    @Getter private final List<ObjectNode> hits;
    @Getter private final List<Map<String, List<String>>> highlights;

    @JsonCreator
    public OpenSearchResult(
        @NonNull List<ObjectNode> hits, @NonNull List<Map<String, List<String>>> highlights) {
      this.hits = hits;
      this.highlights = highlights;
    }
  }
}
