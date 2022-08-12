package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.Value;
import marquez.service.ServiceFactory;

@Path("/api/v1")
public class EventResource extends BaseResource {
  public EventResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/events")
  @Produces(APPLICATION_JSON)
  public Response get(
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    final List<JsonNode> events = eventService.getAll(limit, offset);
    return Response.ok(new Events(events)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/events/{namespace}")
  @Produces(APPLICATION_JSON)
  public Response getByNamespace(
      @PathParam("namespace") String namespace,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    final List<JsonNode> event = eventService.getByNamespace(namespace, limit, offset);
    return Response.ok(new Events(event)).build();
  }

  @Value
  static class Events {
    @NonNull
    @JsonProperty("events")
    List<JsonNode> value;
  }
}
