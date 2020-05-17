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
import com.google.common.collect.ImmutableList;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.Value;
import marquez.api.exceptions.SourceNotFoundException;
import marquez.common.models.SourceName;
import marquez.service.SourceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;

@Path("/api/v1/sources")
public class SourceResource {
  private final SourceService service;

  public SourceResource(@NonNull final SourceService service) {
    this.service = service;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("{source}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(@PathParam("source") SourceName name, @Valid SourceMeta meta)
      throws MarquezServiceException {
    final Source source = service.createOrUpdate(name, meta);
    return Response.ok(source).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{source}")
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("source") SourceName name) throws MarquezServiceException {
    final Source source = service.get(name).orElseThrow(() -> new SourceNotFoundException(name));
    return Response.ok(source).build();
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
    final ImmutableList<Source> sources = service.getAll(limit, offset);
    return Response.ok(new Sources(sources)).build();
  }

  @Value
  static class Sources {
    @NonNull
    @JsonProperty("sources")
    ImmutableList<Source> value;
  }
}
