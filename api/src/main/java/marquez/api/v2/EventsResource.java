package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.openlineage.server.OpenLineage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.v2.MetadataDb;

@Slf4j
@Path("/api/v1")
public class EventsResource {
  private final MetadataDb metaDb;

  public EventsResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @GET
  @Path("/events/lineage")
  @Produces(APPLICATION_JSON)
  public Response lineage(@NotNull OpenLineage.RunEvent event) {
    return Response.ok().build();
  }

  @GET
  @Path("/events/lifecycle")
  @Produces(APPLICATION_JSON)
  public Response lifecycle(@NotNull OpenLineage.RunEvent event) {
    return Response.ok().build();
  }
}
