package marquez.api.v2;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.dropwizard.jersey.jsr310.ZonedDateTimeParam;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.v2.MetadataDb;

@Slf4j
@Path("/api/v1/events")
public class EventsResource {
  private final MetadataDb metaDb;

  public EventsResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @GET
  @Path("/lineage")
  @Produces(APPLICATION_JSON)
  public void lineage(
      @Suspended AsyncResponse asyncResponse,
      @QueryParam("before") ZonedDateTimeParam before,
      @QueryParam("after") ZonedDateTimeParam after,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    metaDb
        .listLineageEvents(limit, offset)
        .whenComplete(
            (events, metaDbReadError) -> {
              if (metaDbReadError == null) {
                asyncResponse.resume(Response.ok(events).build());
              } else {
                log.error("Failed to fetch lineage events.", metaDbReadError);
                asyncResponse.resume(Response.serverError().build());
              }
            })
        .orTimeout(1, SECONDS);
  }

  @GET
  @Path("/lifecycle")
  @Produces(APPLICATION_JSON)
  public void lifecycle(
      @Suspended AsyncResponse asyncResponse,
      @QueryParam("before") ZonedDateTimeParam before,
      @QueryParam("after") ZonedDateTimeParam after,
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    metaDb
        .listLifecycleEvents(limit, offset)
        .whenComplete(
            (events, metaDbReadError) -> {
              if (metaDbReadError == null) {
                asyncResponse.resume(Response.ok(events).build());
              } else {
                log.error("Failed to fetch lifecycle events.", metaDbReadError);
                asyncResponse.resume(Response.serverError().build());
              }
            })
        .orTimeout(1, SECONDS);
  }
}
