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
import jakarta.ws.rs.DELETE;
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
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.filter.exclusions.Exclusions;
import marquez.api.filter.exclusions.ExclusionsConfig;
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
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
  @Produces(MediaType.APPLICATION_JSON)
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
  @Produces(MediaType.APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {

    final List<Namespace> namespaces =
        Optional.ofNullable(Exclusions.namespaces())
            .map(ExclusionsConfig.NamespaceExclusions::getOnRead)
            .filter(ExclusionsConfig.OnRead::isEnabled)
            .map(ExclusionsConfig.OnRead::getPattern)
            .map(pattern -> namespaceService.findAllWithExclusion(pattern, limit, offset))
            .orElseGet(() -> namespaceService.findAll(limit, offset));

    return Response.ok(new Namespaces(namespaces)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @DELETE
  @Path("/namespaces/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@PathParam("namespace") NamespaceName name) {
    final Namespace namespace =
        namespaceService
            .findBy(name.getValue())
            .orElseThrow(() -> new NamespaceNotFoundException(name));
    datasetService.deleteByNamespaceName(namespace.getName().getValue());
    jobService.deleteByNamespaceName(namespace.getName().getValue());
    namespaceService.delete(namespace.getName().getValue());
    return Response.ok(namespace).build();
  }

  @Value
  static class Namespaces {
    @NonNull
    @JsonProperty("namespaces")
    List<Namespace> value;
  }
}
