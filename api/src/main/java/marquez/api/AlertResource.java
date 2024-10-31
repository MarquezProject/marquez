/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.Value;
import marquez.db.models.AlertRow;
import marquez.db.models.NotificationRow;
import marquez.service.ServiceFactory;

@Path("/api/v1/alerts")
public class AlertResource extends BaseResource {
  public AlertResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @NonNull @QueryParam("entityUuid") String entityUuid,
      @NonNull @QueryParam("entityType") String entityType) {
    return Response.ok(new Alerts(alertService.findAll(entityType, entityUuid)).getValue()).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(
      @NonNull @QueryParam("entityType") String entityType,
      @NonNull @QueryParam("entityUuid") String entityUuid,
      @NonNull @QueryParam("type") String type) {
    JsonNode jsonNode = null;
    return Response.ok(alertService.upsert(entityType, entityUuid, type, jsonNode)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @DELETE
  @Produces(APPLICATION_JSON)
  public Response delete(
      @NonNull @QueryParam("entityType") String entityType,
      @NonNull @QueryParam("entityUuid") String entityUuid,
      @NonNull @QueryParam("type") String type) {
    alertService.delete(entityType, entityUuid, type);
    return Response.noContent().build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @DELETE
  @Path("/notifications/archive")
  @Produces(APPLICATION_JSON)
  public Response archive(@NonNull @QueryParam("uuid") String uuid) {
    alertService.archiveNotification(UUID.fromString(uuid));
    return Response.noContent().build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @DELETE
  @Path("/notifications/archive-all")
  @Produces(APPLICATION_JSON)
  public Response archive() {
    alertService.archiveAllNotifications();
    return Response.noContent().build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/notifications")
  @Produces(APPLICATION_JSON)
  public Response listNotifications() {
    return Response.ok(
            new Notifications(alertService.listNotifications(), alertService.countNotifications())
                .getValue())
        .build();
  }

  @Value
  static class Alerts {
    @NonNull
    @JsonProperty("alerts")
    List<AlertRow> value;
  }

  @Value
  static class Notifications {
    @NonNull
    @JsonProperty("notifications")
    List<NotificationRow> value;

    @NonNull
    @JsonProperty("count")
    int count;
  }
}
