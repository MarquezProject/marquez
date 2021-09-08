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
import marquez.api.models.SearchOrder;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.service.SearchService;

@Slf4j
@Path("/api/v1/search")
public class SearchResource {
  private final SearchService searchService;

  public SearchResource(@NonNull final SearchService searchService) {
    this.searchService = searchService;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response search(
      @QueryParam("q") @NotNull String query,
      @QueryParam("filter") @Nullable SearchFilter filter,
      @QueryParam("sort") @DefaultValue("name") SearchSort sort,
      @QueryParam("order") @DefaultValue("desc") SearchOrder order,
      @QueryParam("limit") @DefaultValue("10") @Min(value = 0)  int limit) {
    final List<SearchResult> results = searchService.search(query, filter, sort, order, limit);
    return Response.ok(new SearchResults(results)).build();
  }

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
