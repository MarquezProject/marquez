/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.SQLException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.ServiceFactory;
import marquez.service.models.LineageEvent;
import marquez.service.models.NodeId;

@Slf4j
@Path("/api/v1/lineage")
public class OpenLineageResource extends BaseResource {
  private static final String DEFAULT_DEPTH = "20";

  public OpenLineageResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public void create(
      @Valid @NotNull LineageEvent event, @Suspended final AsyncResponse asyncResponse)
      throws JsonProcessingException, SQLException {
    openLineageService
        .createAsync(event)
        .whenComplete(
            (result, err) -> {
              if (err != null) {
                log.error("Unexpected error while processing request", err);
                asyncResponse.resume(Response.status(determineStatusCode(err)).build());
              } else {
                asyncResponse.resume(Response.status(201).build());
              }
            });
  }

  private int determineStatusCode(Throwable e) {
    if (e instanceof CompletionException) {
      return determineStatusCode(e.getCause());
    } else if (e instanceof IllegalArgumentException) {
      return BAD_REQUEST.getStatusCode();
    }
    return INTERNAL_SERVER_ERROR.getStatusCode();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response getLineage(
      @QueryParam("nodeId") @NotNull NodeId nodeId,
      @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth)
      throws ExecutionException, InterruptedException {
    return Response.ok(lineageService.lineage(nodeId, depth)).build();
  }
}
