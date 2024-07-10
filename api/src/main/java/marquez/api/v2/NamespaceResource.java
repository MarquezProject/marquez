package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.db.v2.MetadataDb;
import marquez.service.models.NamespaceMeta;

@Slf4j
@Path("/api/betav2")
public class NamespaceResource {
  private final MetadataDb metaDb;

  public NamespaceResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @PUT
  @Path("/namespaces/{namespace}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public void put(
      @Suspended AsyncResponse asyncResponse,
      @PathParam("namespace") NamespaceName name,
      @Valid NamespaceMeta meta) {
    metaDb
        .write(name, meta)
        .whenComplete(
            (namespace, metaDbWriteError) -> {
              if (metaDbWriteError == null) {
                asyncResponse.resume(Response.ok(namespace).build());
              } else {
                log.debug("Caught db error write error.", metaDbWriteError);
                asyncResponse.resume(Response.serverError().build());
              }
            })
        .orTimeout(1, null);
  }
}
