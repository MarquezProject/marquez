package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import marquez.api.CreateNamespaceRequest;
import marquez.api.CreateNamespaceResponse;
import marquez.api.ListNamespacesResponse;
import marquez.api.entities.ErrorResponse;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.mappers.GetNamespaceResponseMapper;
import marquez.core.models.Namespace;
import marquez.core.services.NamespaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/namespaces")
@Produces(APPLICATION_JSON)
public class NamespaceResource extends BaseResource {
  private static final Logger LOG = LoggerFactory.getLogger(NamespaceResource.class);

  private final NamespaceService namespaceService;
  private final GetNamespaceResponseMapper getNamespaceResponseMapper =
      new GetNamespaceResponseMapper();

  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  public NamespaceResource(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  // TODO: Something like this is used in almost every resource so it can be factored out in
  // the error-handling review
  private static Response UNEXPECTED_ERROR_RESPONSE =
      Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(new ErrorResponse("Unexpected error occurred"))
          .type(APPLICATION_JSON)
          .build();

  @PUT
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response create(
      @PathParam("namespace") String namespace, @Valid CreateNamespaceRequest request) {
    try {
      Namespace n =
          namespaceService.create(namespace, request.getOwner(), request.getDescription());
      return Response.status(Response.Status.OK)
          .entity(new CreateNamespaceResponse(namespaceMapper.map(n).get()))
          .type(APPLICATION_JSON)
          .build();
    } catch (Exception e) {
      LOG.error(
          "Could not successfully create namespace "
              + namespace
              + ". Exception: "
              + e.getLocalizedMessage());
      return UNEXPECTED_ERROR_RESPONSE;
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  @Path("/{namespace}")
  public Response get(@PathParam("namespace") String namespace) {
    try {
      Optional<Namespace> n = namespaceService.get(namespace);
      if (n.isPresent()) {
        Namespace returnedNamespace = n.get();
        return Response.status(Response.Status.OK)
            .entity(getNamespaceResponseMapper.map(returnedNamespace).get())
            .type(APPLICATION_JSON)
            .build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).type(APPLICATION_JSON).build();
      }
    } catch (Exception e) {
      LOG.error(
          "Could not successfully get namespace "
              + namespace
              + ". Exception: "
              + e.getLocalizedMessage());
      return UNEXPECTED_ERROR_RESPONSE;
    }
  }

  @GET
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response listNamespaces() {
    try {
      // TODO: Implement this using the NamespaceService
      List<Namespace> namespaceList = namespaceService.listNamespaces();
      return Response.status(Response.Status.OK)
          .entity(new ListNamespacesResponse(namespaceList))
          .build();
    } catch (Exception e) {
      LOG.error(
          "Could not successfully get a list of namespaces. Exception: " + e.getLocalizedMessage());
      return UNEXPECTED_ERROR_RESPONSE;
    }
  }
}
