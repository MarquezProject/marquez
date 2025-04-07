/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.ServiceFactory;
import marquez.service.models.NodeId;

@Slf4j
@Path("/api/v1/column-lineage")
public class ColumnLineageResource extends BaseResource {

  private static final String DEFAULT_DEPTH = "20";

  public ColumnLineageResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getLineage(
      @QueryParam("nodeId") @NotNull NodeId nodeId,
      @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth,
      @QueryParam("withDownstream") @DefaultValue("false") boolean withDownstream)
      throws ExecutionException, InterruptedException {
    if (nodeId.hasVersion() && withDownstream) {
      return Response.status(400, "Node version cannot be specified when withDownstream is true")
          .build();
    }
    return Response.ok(columnLineageService.lineage(nodeId, depth, withDownstream)).build();
  }
}
