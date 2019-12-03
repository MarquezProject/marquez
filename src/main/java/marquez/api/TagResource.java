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
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import marquez.api.mappers.Mapper;
import marquez.api.models.TagsResponse;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;

@Path("/api/v1")
public final class TagResource {
  private final TagService tagService;

  public TagResource(@NonNull final TagService tagService) {
    this.tagService = tagService;
  }

  @GET
  @Path("/tags")
  @Produces(APPLICATION_JSON)
  @ResponseMetered
  @ExceptionMetered
  @Timed
  public Response list(
      @QueryParam("limit") @DefaultValue("100") Integer limit,
      @QueryParam("offset") @DefaultValue("0") Integer offset)
      throws MarquezServiceException {
    final List<Tag> tags = tagService.getAll(limit, offset);
    final TagsResponse response = Mapper.toTagsResponse(tags);
    return Response.ok(response).build();
  }
}
