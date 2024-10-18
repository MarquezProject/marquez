/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jersey.jsr310.ZonedDateTimeParam;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
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
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.SortDirection;
import marquez.common.models.RunId;
import marquez.db.OpenLineageDao;
import marquez.service.ServiceFactory;
import marquez.service.models.BaseEvent;
import marquez.service.models.DatasetEvent;
import marquez.service.models.JobEvent;
import marquez.service.models.LineageEvent;
import marquez.service.models.NodeId;

@Slf4j
@Path("/api/v1")
public class OpenLineageResource extends BaseResource {
  private static final String DEFAULT_DEPTH = "20";

  private final OpenLineageDao openLineageDao;

  public OpenLineageResource(
      @NonNull final ServiceFactory serviceFactory, @NonNull final OpenLineageDao openLineageDao) {
    super(serviceFactory);
    this.openLineageDao = openLineageDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Path("/lineage")
  public void create(@Valid @NotNull BaseEvent event, @Suspended final AsyncResponse asyncResponse)
      throws JsonProcessingException, SQLException {
    if (event instanceof LineageEvent) {
      if (serviceFactory.getSearchService().isEnabled()) {
        serviceFactory.getSearchService().indexEvent((LineageEvent) event);
      }
      openLineageService
          .createAsync((LineageEvent) event)
          .whenComplete((result, err) -> onComplete(result, err, asyncResponse));
    } else if (event instanceof DatasetEvent) {
      openLineageService
          .createAsync((DatasetEvent) event)
          .whenComplete((result, err) -> onComplete(result, err, asyncResponse));
    } else if (event instanceof JobEvent) {
      openLineageService
          .createAsync((JobEvent) event)
          .whenComplete((result, err) -> onComplete(result, err, asyncResponse));
    } else {
      log.warn("Unsupported event type {}. Skipping without error", event.getClass().getName());

      // return serialized event
      asyncResponse.resume(Response.status(200).entity(event).build());
    }
  }

  private void onComplete(Void result, Throwable err, AsyncResponse asyncResponse) {
    if (err != null) {
      log.error("Unexpected error while processing request", err);
      asyncResponse.resume(Response.status(determineStatusCode(err)).build());
    } else {
      asyncResponse.resume(Response.status(201).build());
    }
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
  @Path("/lineage")
  public Response getLineage(
      @QueryParam("nodeId") @NotNull NodeId nodeId,
      @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth) {
    throwIfNotExists(nodeId);
    return Response.ok(lineageService.lineage(nodeId, depth)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/events/lineage")
  @Produces(APPLICATION_JSON)
  public Response getLineageEvents(
      @QueryParam("before") @DefaultValue("2030-01-01T00:00:00+00:00") ZonedDateTimeParam before,
      @QueryParam("after") @DefaultValue("1970-01-01T00:00:00+00:00") ZonedDateTimeParam after,
      @QueryParam("sortDirection") @DefaultValue("desc") SortDirection sortDirection,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    List<LineageEvent> events = Collections.emptyList();
    switch (sortDirection) {
      case DESC ->
          events = openLineageDao.getAllLineageEventsDesc(before.get(), after.get(), limit, offset);
      case ASC ->
          events = openLineageDao.getAllLineageEventsAsc(before.get(), after.get(), limit, offset);
    }
    int totalCount = openLineageDao.getAllLineageTotalCount(before.get(), after.get());
    return Response.ok(new Events(events, totalCount)).build();
  }

  /**
   * Returns the upstream lineage for a given run. Recursively: run -> dataset version it read from
   * -> the run that produced it
   *
   * @param runId the run to get upstream lineage from
   * @param depth the maximum depth of the upstream lineage
   * @return the upstream lineage for that run up to `detph` levels
   */
  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Path("/runlineage/upstream")
  public Response getRunLineageUpstream(
      @QueryParam("runId") @NotNull RunId runId,
      @QueryParam("depth") @DefaultValue(DEFAULT_DEPTH) int depth) {
    throwIfNotExists(runId);
    return Response.ok(lineageService.upstream(runId, depth)).build();
  }

  @Value
  static class Events {
    @NonNull
    @JsonProperty("events")
    List<LineageEvent> value;

    int totalCount;
  }
}
