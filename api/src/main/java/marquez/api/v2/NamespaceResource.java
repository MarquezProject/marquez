package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.concurrent.CompletableFuture;
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
import marquez.common.models.NamespaceName;
import marquez.db.v2.MetadataDb;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;

@Path("/api/v1")
public class NamespaceResource {
  private final MetadataDb metaDb;

  public NamespaceResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @PUT
  @Path("/namespaces/{namespace}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response put(
      @Suspended AsyncResponse asyncResponse,
      @PathParam("namespace") NamespaceName name,
      @Valid NamespaceMeta meta) {
    final CompletableFuture<Namespace> namespaceCompletableFuture = metaDb.putNamespace(name, meta);
    asyncResponse.resume(Response.ok(namespace).build());
  }
}
