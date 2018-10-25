package marquez.resources;

import static java.time.Instant.now;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.CreateNamespaceRequest;
import marquez.api.CreateNamespaceResponse;
import marquez.api.GetAllNamespacesResponse;
import marquez.api.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/namespaces")
@Produces(APPLICATION_JSON)
public class NamespaceResource extends BaseResource {

  private static final Logger LOG = LoggerFactory.getLogger(NamespaceResource.class);

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response create(@PathParam("namespace") String namespace, CreateNamespaceRequest request) {
    // TODO: Implement this using the NamespaceService
    CreateNamespaceResponse res =
        new CreateNamespaceResponse(
            request.getOwner(), request.getDescription(), Timestamp.from(now()));
    LOG.info("Trying to create about namespace " + namespace);
    try {
      String jsonRes = mapper.writeValueAsString(res);
      return Response.status(Response.Status.NOT_IMPLEMENTED)
          .entity(jsonRes)
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Entity.json("{'error' : 'an unexpected error occurred.'}"))
          .type(APPLICATION_JSON)
          .build();
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response get(@PathParam("namespace") String namespace) {
    LOG.info("Trying to get info about namespace " + namespace);

    // TODO: Implement this using the NamespaceService
    return Response.status(Response.Status.NOT_IMPLEMENTED).build();
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response getAllNamespaces() {
    try {
      // TODO: Implement this using the NamespaceService
      List<Namespace> namespaceList = new ArrayList<>();
      String jsonRes = mapper.writeValueAsString(new GetAllNamespacesResponse(namespaceList));
      return Response.status(Response.Status.NOT_IMPLEMENTED).entity(jsonRes).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Entity.json("{'error' : 'an unexpected error occurred.'}"))
          .type(APPLICATION_JSON)
          .build();
    }
  }
}
