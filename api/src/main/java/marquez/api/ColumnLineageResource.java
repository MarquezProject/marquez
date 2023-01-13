/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.concurrent.ExecutionException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
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
  @Produces(APPLICATION_JSON)
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
