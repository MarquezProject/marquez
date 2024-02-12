package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.openlineage.server.OpenLineage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.v2.MetadataDb;

// TODO:
// * Add support to deserialize 'OpenLineage.BaseEvent'

@Slf4j
@Path("/api/v1")
public class OpenLineageResource {
  private final MetadataDb metaDb;

  public OpenLineageResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/lineage")
  public Response collect(@NotNull OpenLineage.RunEvent olRunEvent) {
    metaDb.write(olRunEvent);
    return Response.ok().build();
  }
}
