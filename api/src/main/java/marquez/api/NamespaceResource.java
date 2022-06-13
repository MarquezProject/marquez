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
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.common.models.NamespaceName;
import marquez.service.ServiceFactory;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;

@Path("/api/v1")
public class NamespaceResource extends BaseResource {
  public NamespaceResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/namespaces/{namespace}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(
      @PathParam("namespace") NamespaceName name, @Valid NamespaceMeta meta) {
    final Namespace namespace = namespaceService.createOrUpdate(name, meta);
    return Response.ok(namespace).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces/{namespace}")
  @Produces(APPLICATION_JSON)
  public Response get(@PathParam("namespace") NamespaceName name) {
    final Namespace namespace =
        namespaceService
            .findBy(name.getValue())
            .orElseThrow(() -> new NamespaceNotFoundException(name));
    return Response.ok(namespace).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/namespaces")
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    final List<Namespace> namespaces = namespaceService.findAll(limit, offset);
    return Response.ok(new Namespaces(namespaces)).build();
  }

  @Value
  static class Namespaces {
    @NonNull
    @JsonProperty("namespaces")
    List<Namespace> value;
  }
}
