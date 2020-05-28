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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.Value;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;

@Path("/api/v1/tags")
public class TagResource {
  private final TagService service;

  public TagResource(@NonNull final TagService service) {
    this.service = service;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    final ImmutableSet<Tag> tags = service.getAll(limit, offset);
    return Response.ok(new Tags(tags)).build();
  }

  @Value
  static class Tags {
    @NonNull
    @JsonProperty("tags")
    ImmutableSet<Tag> value;
  }
}
