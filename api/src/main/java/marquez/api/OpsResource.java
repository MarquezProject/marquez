/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.OpsService;
import marquez.service.ServiceFactory;

@Slf4j
@Path("/api/v1/ops")
public class OpsResource {

  private final OpsService opsService;

  public OpsResource(@NonNull final ServiceFactory serviceFactory) {
    this.opsService = serviceFactory.getOpsService();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/lineage-metrics/day")
  @Produces(APPLICATION_JSON)
  public Response getLastDayLineageMetrics() {
    return Response.ok(opsService.getLastDayLineageMetrics()).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/lineage-metrics/week")
  @Produces(APPLICATION_JSON)
  public Response getLastWeekLineageMetrics() {
    return Response.ok(opsService.getLastWeekLineageMetrics()).build();
  }
}
