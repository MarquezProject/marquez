/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createOrUpdate(@PathParam("source") SourceName name, @Valid SourceMeta meta) {
    final Source source = sourceService.createOrUpdate(name, meta);
    return Response.ok(source).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{source}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("source") SourceName name) {
    final Source source =
        sourceService.findBy(name.getValue()).orElseThrow(() -> new SourceNotFoundException(name));
    return Response.ok(source).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(MediaType.APPLICATION_JSON)
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
