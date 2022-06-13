/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
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
import marquez.service.ServiceFactory;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;

@Path("/api/v1/sources")
public class SourceResource extends BaseResource {
  public SourceResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("{source}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(@PathParam("source") SourceName name, @Valid SourceMeta meta) {
    final Source source = sourceService.createOrUpdate(name, meta);
    return Response.ok(source).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{source}")
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("source") SourceName name) {
    final Source source =
        sourceService.findBy(name.getValue()).orElseThrow(() -> new SourceNotFoundException(name));
    return Response.ok(source).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    final List<Source> sources = sourceService.findAll(limit, offset);
    return Response.ok(new Sources(sources)).build();
  }

  @Value
  static class Sources {
    @NonNull
    @JsonProperty("sources")
    List<Source> value;
  }
}
