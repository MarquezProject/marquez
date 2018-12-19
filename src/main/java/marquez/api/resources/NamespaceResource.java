package marquez.api.resources;

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
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.CreateNamespaceRequest;
import marquez.api.models.ListNamespacesResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.CoreNamespaceToApiNamespaceMapper;
import marquez.core.mappers.GetNamespaceResponseMapper;
import marquez.core.mappers.NamespaceApiMapper;
import marquez.core.models.Namespace;
import marquez.core.services.NamespaceService;
import org.hibernate.validator.constraints.NotBlank;

@Slf4j
@Path("/api/v1")
public class NamespaceResource extends BaseResource {

  private final NamespaceService namespaceService;
  private final GetNamespaceResponseMapper getNamespaceResponseMapper =
      new GetNamespaceResponseMapper();

  private final CoreNamespaceToApiNamespaceMapper namespaceMapper =
      new CoreNamespaceToApiNamespaceMapper();

  private final NamespaceApiMapper namespaceAPIMapper = new NamespaceApiMapper();

  public NamespaceResource(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  @PUT
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces/{namespace}")
  public Response create(
      @PathParam("namespace") @NotBlank String namespace, @Valid CreateNamespaceRequest request)
      throws ResourceException {
    try {
      marquez.core.models.Namespace n =
          namespaceService.create(namespaceAPIMapper.of(namespace, request));
      return Response.status(Response.Status.OK)
          .entity(namespaceMapper.map(n))
          .type(APPLICATION_JSON)
          .build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces/{namespace}")
  public Response get(@PathParam("namespace") String namespace) throws ResourceException {
    try {
      Optional<Namespace> n = namespaceService.get(namespace);
      if (n.isPresent()) {
        Namespace returnedNamespace = n.get();
        return Response.status(Response.Status.OK)
            .entity(getNamespaceResponseMapper.map(returnedNamespace))
            .type(APPLICATION_JSON)
            .build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).type(APPLICATION_JSON).build();
      }
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }

  @GET
  @Produces(APPLICATION_JSON)
  @Timed
  @Path("/namespaces")
  public Response listNamespaces() throws ResourceException {
    try {
      List<marquez.core.models.Namespace> namespaceCoreModels = namespaceService.listNamespaces();
      List<marquez.api.models.Namespace> namespaceList =
          new CoreNamespaceToApiNamespaceMapper().map(namespaceCoreModels);
      return Response.status(Response.Status.OK)
          .entity(new ListNamespacesResponse(namespaceList))
          .build();
    } catch (UnexpectedException e) {
      log.error(e.getMessage(), e);
      throw new ResourceException();
    }
  }
}
