/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jersey.jsr310.ZonedDateTimeParam;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
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
import marquez.db.OpenLineageDao;
import marquez.service.OpenLineageService;
import marquez.service.ServiceFactory;
import marquez.service.models.BaseEvent;
import marquez.service.models.LineageEvent;
import marquez.service.models.NodeId;

@Slf4j
@Path("/api/v1")
public class OpenLineageResource extends BaseResource {
  private static final String DEFAULT_DEPTH = "20";

  private final OpenLineageDao openLineageDao;

  private final ElasticsearchClient elasticsearchClient;

  public OpenLineageResource(
      @NonNull final ServiceFactory serviceFactory,
      @NonNull final OpenLineageDao openLineageDao,
      @Nullable final ElasticsearchClient elasticsearchClient) {
    super(serviceFactory);
    this.openLineageDao = openLineageDao;
    this.elasticsearchClient = elasticsearchClient;
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
      openLineageService
          .createAsync((LineageEvent) event)
          .whenComplete(
              (result, err) -> {
                if (err != null) {
                  log.error("Unexpected error while processing request", err);
                  asyncResponse.resume(Response.status(determineStatusCode(err)).build());
                } else {
                  asyncResponse.resume(Response.status(201).build());
                }
              });
      indexEvent((LineageEvent) event);
    } else {
      log.warn("Unsupported event type {}. Skipping without error", event.getClass().getName());

      // return serialized event
      asyncResponse.resume(Response.status(200).entity(event).build());
    }
  }

  private UUID runUuidFromEvent(LineageEvent.Run run) {
    UUID runUuid;
    try {
      runUuid = UUID.fromString(run.getRunId());
    } catch (Exception e) {
      runUuid = UUID.nameUUIDFromBytes(run.getRunId().getBytes(StandardCharsets.UTF_8));
    }
    return runUuid;
  }

  private void indexEvent(@Valid @NotNull LineageEvent event) {
    if (this.elasticsearchClient != null) {
      UUID runUuid = runUuidFromEvent(event.getRun());
      log.info("Indexing event {}", event);
      IndexRequest<BaseEvent> request =
          IndexRequest.of(
              i ->
                  i.index("events")
                      .id(runUuid.toString())
                      .document(event));
      try {
        this.elasticsearchClient.index(request);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
    return Response.ok(lineageService.lineage(nodeId, depth, true)).build();
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
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit) {
    List<LineageEvent> events = Collections.emptyList();
    switch (sortDirection) {
      case DESC -> events =
          openLineageDao.getAllLineageEventsDesc(before.get(), after.get(), limit);
      case ASC -> events = openLineageDao.getAllLineageEventsAsc(before.get(), after.get(), limit);
    }
    return Response.ok(new Events(events)).build();
  }

  @Value
  static class Events {
    @NonNull
    @JsonProperty("events")
    List<LineageEvent> value;
  }
}
