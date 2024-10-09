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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.Period;
import marquez.service.ServiceFactory;
import marquez.service.StatsService;

@Slf4j
@Path("/api/v1/stats")
public class StatsResource {

  private final StatsService StatsService;

  public StatsResource(@NonNull final ServiceFactory serviceFactory) {
    this.StatsService = serviceFactory.getStatsService();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/lineage-events")
  public Response getStats(
      @QueryParam("period") Period period, @QueryParam("timezone") String timezone) {

    // Check if the period is WEEK and timezone is missing
    if (Period.WEEK.equals(period) && (timezone == null || timezone.isEmpty())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Timezone must be specified for period 'WEEK'")
          .build();
    }

    return (Period.DAY.equals(period)
        ? Response.ok(StatsService.getLastDayLineageMetrics()).build()
        : Period.WEEK.equals(period)
            ? Response.ok(StatsService.getLastWeekLineageMetrics(timezone)).build()
            : Response.status(Response.Status.BAD_REQUEST).entity("Invalid period").build());
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/jobs")
  public Response getJobs(
      @QueryParam("period") Period period, @QueryParam("timezone") String timezone) {

    return (Period.DAY.equals(period)
        ? Response.ok(StatsService.getLastDayJobs()).build()
        : Period.WEEK.equals(period)
            ? Response.ok(StatsService.getLastWeekJobs(timezone)).build()
            : Response.status(Response.Status.BAD_REQUEST).entity("Invalid period").build());
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/datasets")
  public Response getDatasets(
      @QueryParam("period") Period period, @QueryParam("timezone") String timezone) {

    return (Period.DAY.equals(period)
        ? Response.ok(StatsService.getLastDayDatasets()).build()
        : Period.WEEK.equals(period)
            ? Response.ok(StatsService.getLastWeekDatasets(timezone)).build()
            : Response.status(Response.Status.BAD_REQUEST).entity("Invalid period").build());
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  @Path("/sources")
  public Response getSources(
      @QueryParam("period") Period period, @QueryParam("timezone") String timezone) {

    return (Period.DAY.equals(period)
        ? Response.ok(StatsService.getLastDaySources()).build()
        : Period.WEEK.equals(period)
            ? Response.ok(StatsService.getLastWeekSources(timezone)).build()
            : Response.status(Response.Status.BAD_REQUEST).entity("Invalid period").build());
  }
}
